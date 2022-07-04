package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.NO_GLOSSARY_DIR;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.NO_SUCH_VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.trustoverip.ctwg.toolkit.mrg.connectors.GithubReader;
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

  @Setter(AccessLevel.PRIVATE)
  private Map<String, GeneratorContext> contextMap;

  public MRGlossaryGenerator() {
    this(true);
  }

  public MRGlossaryGenerator(boolean runLocal) {
    MRGConnector connector;
    if (runLocal) {
      connector = new LocalFSConnector();
    } else {
      connector = new GithubReader();
    }
    wrangler = new ModelWrangler(new YamlWrangler(), connector);
  }

  public MRGlossaryGenerator(ModelWrangler wrangler) {
    this.wrangler = wrangler;
  }

  public MRGModel generate(final String scopedir, final String safFilename, final String versionTag)
      throws MRGGenerationException {
    SAFModel saf = wrangler.getSaf(scopedir, safFilename);
    this.setContextMap(wrangler.buildContextMap(scopedir, saf, versionTag));
    String glossaryDir = saf.getScope().getGlossarydir();
    if (StringUtils.isEmpty(glossaryDir)) {
      throw new MRGGenerationException(NO_GLOSSARY_DIR);
    }
    Version localVersion = getVersion(saf, versionTag);
    String mrgFilename = constructFilename(localVersion);
    log.debug(String.format("MRG filename to be generated is: %s", mrgFilename));
    // construct the parts of the MRG Model
    Terminology terminology =
        new Terminology(saf.getScope().getScopetag(), saf.getScope().getScopedir());
    List<ScopeRef> scopes = new ArrayList<>(saf.getScopes());
    List<MRGEntry> entries =
        constructLocalEntries(contextMap.get(saf.getScope().getScopetag()), localVersion);
    // TODO
    //    entries.addAll(constructRemoteEntries(saf, localVersion));
    MRGModel mrg = new MRGModel(terminology, scopes, entries);
    wrangler.writeMrgToFile(mrg, mrgFilename);

    return mrg;
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

  private String constructFilename(Version localVersion) {
    // mrg.<versionTag>.yaml
    return String.join(".", DEFAULT_MRG_FILENAME, localVersion.getVsntag(), "yaml");
  }

  /*
   Local entries are selected from the curatedDir
  */
  private List<MRGEntry> constructLocalEntries(
      GeneratorContext localContext, Version localVersion) {
    // TODO don't just get the first
    // e.g. [tev2]@tev2"
    // TODO use regex instead
    String unfilteredFilter = localVersion.getTerms().get(0).replace("[", "").replace("]", "");
    String filterTerm = unfilteredFilter.split("@")[0];
    List<Term> localTerms = wrangler.fetchTerms(localContext, filterTerm);
    return localTerms.stream().map(MRGEntry::new).toList();
  }

  /*
   Remote entries are selected from the mrg
  */
  private List<MRGEntry> constructRemoteEntries(SAFModel saf, Version localVersion) {
    return new ArrayList<>();
  }

  private static final int LOCAL_PARAMS_EXPECTED = 2;
  private static final int REMOTE_PARAMS_EXPECTED = 4;
  private static final int SCOPEDIR_INDEX = 0;
  private static final int VERSIONTAG_INDEX = 1;
  private static final int GITHUB_USER_INDEX = 1;
  private static final int GITHUB_TOKEN_INDEX = 1;
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

  public static void main(String[] args) {
    if (args.length != LOCAL_PARAMS_EXPECTED && args.length != REMOTE_PARAMS_EXPECTED) {
      System.out.println(INVALID_INPUT);
    }
    boolean local = args.length == LOCAL_PARAMS_EXPECTED;
    String scopedir = args[SCOPEDIR_INDEX];
    String versionTag = args[VERSIONTAG_INDEX];
    MRGlossaryGenerator generator = new MRGlossaryGenerator(local);
    try {
      generator.generate(scopedir, DEFAULT_SAF_FILENAME, versionTag);
    } catch (MRGGenerationException mrge) {
      System.out.println(mrge.getMessage());
    } catch (Exception e) {
      System.out.println(String.format(UNEXPECTED_ERROR, e.getMessage()));
    }

  }
}
