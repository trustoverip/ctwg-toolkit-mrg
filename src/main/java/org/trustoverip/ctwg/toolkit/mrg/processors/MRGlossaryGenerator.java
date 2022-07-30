package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.NO_GLOSSARY_DIR;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.NO_SUCH_VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.trustoverip.ctwg.toolkit.mrg.connectors.GithubConnector;
import org.trustoverip.ctwg.toolkit.mrg.connectors.LocalFSConnector;
import org.trustoverip.ctwg.toolkit.mrg.connectors.MRGConnector;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGEntry;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;
import org.trustoverip.ctwg.toolkit.mrg.model.SAFModel;
import org.trustoverip.ctwg.toolkit.mrg.model.ScopeRef;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;
import org.trustoverip.ctwg.toolkit.mrg.model.Terminology;
import org.trustoverip.ctwg.toolkit.mrg.model.Version;

/**
 * @author sih
 */
@Slf4j
public class MRGlossaryGenerator {

  public static final String DEFAULT_MRG_FILENAME = "mrg";
  public static final String DEFAULT_SAF_FILENAME = "saf.yaml";

  private final ModelWrangler wrangler;

  private static final int PARAMS_EXPECTED = 2;

  @Setter(AccessLevel.PRIVATE)
  private Map<String, GeneratorContext> contextMap;

  public MRGlossaryGenerator() {
    this(true);
  }
  private static final int LOCAL_PARAMS_EXPECTED = 3;
  private static final int SCOPEDIR_INDEX = 0;

  public MRGlossaryGenerator(boolean runLocal) {
    MRGConnector connector = runLocal ? new LocalFSConnector() : new GithubConnector();
    wrangler = new ModelWrangler(new YamlWrangler(), connector);
    remoteErrorCollector = new ArrayList<>();
  }

  private Version getVersion(SAFModel saf, String versionTag) throws MRGGenerationException {
    List<Version> versions = saf.getVersions();
    Optional<Version> version = Optional.empty();
    for (Version v : versions) {
      if (v.getVsntag().equals(versionTag)) {
        version = Optional.of(v);
        break;
      }
    }
    return version.orElseThrow(
        () -> new MRGGenerationException(String.format(NO_SUCH_VERSION, versionTag)));
  }

  private static final int VERSIONTAG_INDEX = 1;
  private static final String INVALID_INPUT = """
      Invalid input: Some of the fields required to run the generator are missing. There should be
      either 2 or 4 inputs depending on whether running from a local scope administration file (SAF),
      or connecting to a github repository to read the SAF file from there.
       
      To run locally: mrg-generator <scopedir> <version tag>
        e.g. mrg-generator ./workspace/tev2 mrgtest
        
      To run remotely: mrg-generator <scopedir> <version tag> -DGH_NAME=<github username> -DGH_TOKEN=<github access token>
        e.g. mrg-generator https://github.com/essif-lab/framework/tree/master/docs/tev2 mrgtest foo abc123
      
      """;
  private static final String UNEXPECTED_ERROR = """
      There was an unexpected error when generating the Glossary.
      It'd be appreciated if you could cut and paste the output below to the CTWG Toolkit development
      team so we can fix the bug. You can find us at Slack (https://trustoverip.slack.com/archives/C03LGMGNZGX)
      or alternatively you can raise an Issue on Github (https://github.com/trustoverip/ctwg-mrg-gen/issues/new/choose)
      
      %s
      """;
  private final List<String> remoteErrorCollector; // collect errors rather than fail fast

  public static void main(String[] args) {
    if (args.length != PARAMS_EXPECTED && args.length != LOCAL_PARAMS_EXPECTED) {
      log.error(INVALID_INPUT);
      System.exit(1);
    }
    boolean isLocal = (args.length == LOCAL_PARAMS_EXPECTED && args[LOCAL_PARAMS_EXPECTED-1].equalsIgnoreCase("local"));
    String scopedir = args[SCOPEDIR_INDEX];
    String versionTag = args[VERSIONTAG_INDEX];
    MRGlossaryGenerator generator = new MRGlossaryGenerator(isLocal);
    log.info("***** Starting generation *****");
    log.info("Creating an MRG from scopedir {} and version tag {}", scopedir, versionTag);
    try {
      generator.generate(scopedir, DEFAULT_SAF_FILENAME, versionTag);
      log.info("***** Completed: Successfully generated MRG *****");
    } catch (MRGGenerationException mrge) {
      log.error(mrge.getMessage());
    } catch (Exception e) {
      log.error(String.format(UNEXPECTED_ERROR, e.getMessage()));
    }

  }
  public MRGlossaryGenerator(ModelWrangler wrangler) {
    this.wrangler = wrangler;
    remoteErrorCollector = new ArrayList<>();
  }

  /*
   Local entries are selected from the curatedDir
  */
  private List<MRGEntry> currentTerms(GeneratorContext generatorContext, Version currentVersion) {
    List<Term> currentTerms = wrangler.fetchTerms(generatorContext, generatorContext.getFilters());
    return currentTerms.stream().map(MRGEntry::new).toList();
  }

  /*
   Remote entries are selected from the mrg
  */
  private List<MRGEntry> remoteTerms(String scopetag, GeneratorContext remoteContext) {
    log.info("... Fetching terms for scopetag {} from scopedir {} with version {}", scopetag, remoteContext.getRootDirPath(), remoteContext.getVersionTag());
    List<MRGEntry> remoteEntries = new ArrayList<>();
    /*
      1. Get and parse remote SAF
      - Fail WARN if no SAF exists
      2. Find MRG dir from SAF
      3. Construct mrg.<remoteContext.getVersionTag()>.mrg
      4. Get remote content
      - Fail WARN if cannot get content
      5. Match terms
      - WARN if no matches found in remote MRG
     */
    // remote SAFs are always remote - i.e. obtained via GitHub
    if (wrangler.getConnector() instanceof LocalFSConnector) {
      wrangler.setConnector(new GithubConnector());
    }
    // 1. Get remote SAF
    SAFModel remoteSaf = wrangler.getSaf(remoteContext.getRootDirPath(), DEFAULT_SAF_FILENAME);
    if (remoteSaf != null) {
      String glossaryDir = remoteSaf.getScope().getGlossarydir();
      MRGModel remoteMrg = wrangler.getMrg(remoteContext, remoteSaf.getScope().getGlossarydir());
      if (remoteMrg != null) {
        List<MRGEntry> mrgEntries = remoteMrg.entries();
      } else {
        log.warn("No MRG found in glossary directory {} of remote dir {}",remoteContext.getRootDirPath(), glossaryDir);
      }

    } else {
      this.remoteErrorCollector.add(String.format("There was an error with remote scopetag %s. Could not find the %s at %s", scopetag, DEFAULT_SAF_FILENAME, remoteContext.getRootDirPath()));
    }
    return remoteEntries;
  }

  public MRGModel generate(final String scopedir, final String safFilename, final String versionTag)
      throws MRGGenerationException {
    log.info("Step 1/6: Parsing Scope Administration File (SAF) from location {}", safFilename);
    SAFModel saf = wrangler.getSaf(scopedir, safFilename);
    log.info("Step 2/6: Resolving local and remote scopes defined in the SAF");
    this.setContextMap(wrangler.buildContextMap(scopedir, saf, versionTag));
    String glossaryDir = saf.getScope().getGlossarydir();
    if (StringUtils.isEmpty(glossaryDir)) {
      throw new MRGGenerationException(NO_GLOSSARY_DIR);
    }
    Version localVersion = getVersion(saf, versionTag);
    // construct the parts of the MRG Model
    log.info("Step 3/6: Creating the <terminology> section of the MRG");
    Terminology terminology =
        new Terminology(saf.getScope().getScopetag(), saf.getScope().getScopedir());
    List<ScopeRef> scopes = new ArrayList<>(saf.getScopes());
    log.info("Step 4/6: Parsing local terms (terms in this scopedir) to create MRG entries:");
    List<MRGEntry> entries =
        currentTerms(contextMap.get(saf.getScope().getScopetag()), localVersion);
    log.info("Step 5/6: Parsing remote terms (terms from the scopedirs in the scopes section) to create MRG entries:");
    Set<Entry<String, GeneratorContext>> contextsByScopetag = this.contextMap.entrySet();
    for (Entry<String, GeneratorContext> e : contextsByScopetag ) {
      ListUtils.union(entries, remoteTerms(e.getKey(), e.getValue()));
    }
    MRGModel mrg = new MRGModel(terminology, scopes, entries);
    String mrgFilename = wrangler.writeMrgToFile(mrg, saf.getScope().getGlossarydir(), versionTag);
    log.info("Step 6/6: Written generated MRG to file: {}", mrgFilename);

    return mrg;
  }
}
