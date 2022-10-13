package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.CANNOT_CREATE_GLOSSARY_DIR;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.NO_SAF;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGlossaryGenerator.DEFAULT_MRG_FILENAME;
import static org.trustoverip.ctwg.toolkit.mrg.processors.TermsFilter.ALL_TAGS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.trustoverip.ctwg.toolkit.mrg.connectors.FileContent;
import org.trustoverip.ctwg.toolkit.mrg.connectors.LocalFSConnector;
import org.trustoverip.ctwg.toolkit.mrg.connectors.MRGConnector;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;
import org.trustoverip.ctwg.toolkit.mrg.model.SAFModel;
import org.trustoverip.ctwg.toolkit.mrg.model.ScopeRef;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;
import org.trustoverip.ctwg.toolkit.mrg.model.Version;
import org.trustoverip.ctwg.toolkit.mrg.processors.TermsFilter.TermsFilterType;

/**
 * This class works with scope files and turns them in to a Java object model for easier
 * manipulation.
 *
 * @author sih
 */
@Slf4j
@Service
class ModelWrangler {

  private static final String TERM_HORIZONTAL_RULE = "---";
  private static final String MARKDOWN_HEADING = "#";
  private static final String HTTPS = "https://";
  private static final String TREE = "tree";
  private static final int OWNER_PART_INDEX = 1;
  private static final int REPO_PART_INDEX = 2;
  private static final String MULTIPLE_USE_FIELDS = "multiple-use fields";
  private static final String GENERIC_FRONT_MATTER = "generic front-matter";

  private static final String GLOSSARY_VIRTUAL_PATH = "/glossaries";

  /*
    private static final Pattern TERM_EXPRESSION_MATCHER =
        Pattern.compile("(-?)(tags|terms|\\*)\\[?([\\w, -@]*)]?@?(\\w+-?\\w*)?:?([A-Za-z0-9.-_]+)?");
  */
  private static final Pattern TERM_EXPRESSION_MATCHER =
      Pattern.compile("(-?)(tags|terms|\\*)\\[?([\\w, -@]*)]?@?(\\w+-?\\w*)?:?([A-Za-z0-9.-_]+)?");

  private static final int MATCH_REMOVE_SYNTAX_GROUP = 1;
  private static final int MATCH_FILTER_TYPE_GROUP = 2;
  private static final int MATCH_VALS_GROUP = 3;
  private static final int MATCH_SCOPETAG_GROUP = 4;
  private static final int MATCH_VERSION_GROUP = 5;
  private final YamlWrangler yamlWrangler;
  @Getter @Setter private MRGConnector connector;

  @Setter(AccessLevel.NONE) // as we derive this from what type of connector has been passed
  private boolean local;

  ModelWrangler(YamlWrangler yamlWrangler, MRGConnector connector) {
    this.yamlWrangler = yamlWrangler;
    this.connector = connector;
    local = (connector instanceof LocalFSConnector);
  }

  SAFModel getSaf(String scopedir, String safFilename) throws MRGGenerationException {
    String safAsString = this.getSafAsString(scopedir, safFilename);
    if (null == safAsString) {
      throw new MRGGenerationException(String.format(NO_SAF, scopedir));
    }
    return yamlWrangler.parseSaf(safAsString);
  }

  String getSafAsString(String scopedir, String safFilename) throws MRGGenerationException {
    String ownerRepo = getOwnerRepo(scopedir);
    String saf = (null == safFilename) ? MRGlossaryGenerator.DEFAULT_SAF_FILENAME : safFilename;
    String safFilepath = String.join("/", getRootPath(scopedir), saf);
    try {
      return connector.getContent(ownerRepo, safFilepath);
    } catch (Throwable t) {
      throw new MRGGenerationException(
          String.format(MRGGenerationException.NOT_FOUND, String.join("/", scopedir, safFilename)));
    }
  }

  /**
   * @param scopedir The scopedir of the (local) scope we are building the MRG for
   * @param saf The SAF model of the local scope
   * @param versionTag The version of the local scope we want to use
   * @return A structure containing all the local and remote directories and filters we need to
   *     construct the MRG. This structure is a proxy for and summarized version of the SAF. It is a
   *     Map (dictionary) keyed by the scopetag of each local and remote scope in the SAF with a
   *     value holding the key directory locations and filters for each of the scopes
   */
  Map<String, GeneratorContext> buildContextMap(String scopedir, SAFModel saf, String versionTag) {
    Map<String, GeneratorContext> contextMap = new HashMap<>();
    // create context for local scope
    String localScope = saf.getScope().getScopetag();
    GeneratorContext localContext =
        createSkeletonContext(scopedir, saf.getScope().getCuratedir(), versionTag);
    contextMap.put(localScope, localContext);
    // get local version we are building MRG for
    Optional<Version> optionalVersion =
        saf.getVersions().stream().filter(v -> v.getVsntag().equals(versionTag)).findFirst();
    // will contain the versions for each of the remote scopes
    Map<String, String> versionsByScopetag = new HashMap<>();
    // will contain the filters (term selection criteria) for each of the remote scopes
    Map<String, List<Predicate<Term>>> addFiltersByScopetag = new HashMap<>();
    Map<String, List<Predicate<Term>>> removeFiltersByScopetag = new HashMap<>();
    if (optionalVersion.isPresent()) {
      Version versionOfInterest = optionalVersion.get();
      List<String> termExpressions = versionOfInterest.getTermselcrit();
      for (String expression : termExpressions) {
        Matcher m = TERM_EXPRESSION_MATCHER.matcher(expression);
        if (m.matches()) {
          boolean remove = StringUtils.isNotEmpty(m.group(MATCH_REMOVE_SYNTAX_GROUP));
          String scopetag =
              StringUtils.isNotEmpty(m.group(MATCH_SCOPETAG_GROUP))
                  ? m.group(MATCH_SCOPETAG_GROUP)
                  : localScope;
          versionsByScopetag.put(scopetag, m.group(MATCH_VERSION_GROUP));
          String filterTypeGroup = m.group(MATCH_FILTER_TYPE_GROUP);
          String matchValsGroup = m.group(MATCH_VALS_GROUP);
          if (remove) {
            removeFiltersByScopetag
                .computeIfAbsent(scopetag, k -> new ArrayList<>())
                .add(termsFilter(filterTypeGroup, matchValsGroup));
          } else {
            addFiltersByScopetag
                .computeIfAbsent(scopetag, k -> new ArrayList<>())
                .add(termsFilter(filterTypeGroup, matchValsGroup));
          }

        } else {
          log.warn(
              "The  expression: {} in the version.termselcrit element could not be parsed",
              expression);
        }
      }
      // add filters for local scope
      localContext.setAddFilters(addFiltersByScopetag.getOrDefault(localScope, new ArrayList<>()));
      localContext.setRemoveFilters(
          removeFiltersByScopetag.getOrDefault(localScope, new ArrayList<>()));
    }
    // create external scopes
    List<ScopeRef> externalScopes = saf.getScopes();
    for (ScopeRef externalScope : externalScopes) {
      List<String> scopetags = externalScope.getScopetags();
      for (String scopetag : scopetags) {
        GeneratorContext generatorContext =
            createExternalContext(
                externalScope,
                scopetag,
                versionsByScopetag,
                addFiltersByScopetag,
                removeFiltersByScopetag);
        contextMap.put(scopetag, generatorContext);
      }
    }


    return contextMap;
  }

  private GeneratorContext createExternalContext(
      ScopeRef externalScope,
      String scopetag,
      Map<String, String> versionsByScopetag,
      Map<String, List<Predicate<Term>>> addFiltersByScopetag,
      Map<String, List<Predicate<Term>>> removeFiltersByScopetag) {
    GeneratorContext generatorContext =
        createSkeletonContext(
            externalScope.getScopedir(),
            StringUtils.EMPTY,
            StringUtils.EMPTY); // will find dirs later
    generatorContext.setVersionTag(versionsByScopetag.getOrDefault(scopetag, StringUtils.EMPTY));
    generatorContext.setAddFilters(addFiltersByScopetag.getOrDefault(scopetag, new ArrayList<>()));
    generatorContext.setRemoveFilters(
        removeFiltersByScopetag.getOrDefault(scopetag, new ArrayList<>()));

    return generatorContext;
  }

  private TermsFilter termsFilter(String typeString, String vals) {
    TermsFilterType type;
    if (typeString.equals(ALL_TAGS)) {
      return TermsFilter.all();
    } else {
      type = TermsFilterType.valueOf(typeString);
    }
    return TermsFilter.of(type, vals);
  }

  /*
   Grouptag and scopetag are mandatory but version is optional
   [grouptag]@scopetag:version
  */

  MRGModel getMrg(
      GeneratorContext context, String glossaryDir, List<String> alternativeVersionTags) {
    String mrgPath = constructMrgFilepath(glossaryDir, context.getVersionTag());
    String mrgAsYaml = connector.getContent(context.getOwnerRepo(), mrgPath);
    int indexToAlts = 0;
    // if no match and alternative version tags exist then try them
    if (null == mrgAsYaml && alternativeVersionTags != null) {
      for (String nextAlternative : alternativeVersionTags) {
        mrgPath = constructMrgFilepath(glossaryDir, nextAlternative);
        mrgAsYaml = connector.getContent(context.getOwnerRepo(), mrgPath);
      }
    }
    return (null == mrgAsYaml) ? null : yamlWrangler.parseMrg(mrgAsYaml);
  }

  String writeMrgToFile(MRGModel mrg, String glossaryDir, String versionTag)
      throws MRGGenerationException {
    String pathStr = constructMrgFilepath(GLOSSARY_VIRTUAL_PATH, versionTag);
    log.debug(String.format("MRG filename to be generated is: %s", pathStr));
    Path glossaryPath = Paths.get(glossaryDir);
    try {
      Files.createDirectories(glossaryPath);
    } catch (IOException ioe) {
      throw new MRGGenerationException(
          String.format(CANNOT_CREATE_GLOSSARY_DIR, glossaryPath.toAbsolutePath()));
    }
    Path mrgFilepath = Path.of(pathStr);
    yamlWrangler.writeMrg(mrgFilepath, mrg);
    return mrgFilepath.toString();
  }

  List<Term> fetchTerms(
      GeneratorContext currentContext,
      List<Predicate<Term>> addFilters,
      List<Predicate<Term>> removeFilters) {
    Predicate<Term> consolidatedAddFilter = consolidateAdd(addFilters);
    Predicate<Term> consolidateRemoveFilter = consolidateRemove(removeFilters);
    List<Term> terms = new ArrayList<>();
    String curatedPath =
        String.join("/", currentContext.getSafDirectory(), currentContext.getCuratedDir());
    List<FileContent> directoryContent =
        connector.getDirectoryContent(currentContext.getOwnerRepo(), curatedPath);
    if (!directoryContent.isEmpty()) {
      terms =
          directoryContent.stream()
              .map(this::cleanTermFile)
              .map(this::toYaml)
              .filter(Objects::nonNull)
              .filter(consolidatedAddFilter)
              .filter(consolidateRemoveFilter)
              .collect(Collectors.toList());
    }
    return terms;
  }

  private Predicate<Term> consolidateAdd(List<Predicate<Term>> addFilters) {
    if (null == addFilters || addFilters.isEmpty()) {
      return TermsFilter.all();
    } else {
      return addFilters.stream().reduce(Predicate::or).get();
    }
  }

  private Predicate<Term> consolidateRemove(List<Predicate<Term>> removeFilters) {
    if (null == removeFilters || removeFilters.isEmpty()) {
      return TermsFilter.all();
    } else {
      return removeFilters.stream().reduce(Predicate::and).get().negate();
    }
  }

  private FileContent cleanTermFile(FileContent dirtyContent) {
    StringBuilder cleanYaml = new StringBuilder();
    String[] parts = dirtyContent.content().split("---");
    String partWithYaml = parts[1];
    String[] lines = partWithYaml.split("\n");
    for (String line : lines) {
      if (isClean(line)) {
        log.debug(
            "Found something of interest in file: {}\nline: {}", dirtyContent.filename(), line);
        cleanYaml.append(line);
        cleanYaml.append("\n");
      }
    }
    return new FileContent(dirtyContent.filename(), cleanYaml.toString(), dirtyContent.headings());
  }

  private Term toYaml(FileContent fileContent) {
    Term term = null;
    try {
      term = yamlWrangler.parseTerm(fileContent.content());
      term.setFilename(fileContent.filename());
      term.setHeadings(fileContent.headings());
      log.info("... Creating entry from term with id = {} ...", term.getTerm());
    } catch (Throwable t) {
      log.error("Couldn't read or parse the following term file: {}", fileContent.filename());
    }
    return term;
  }

  /*
    Used to filter out headings and other human-style clutter
  */
  private boolean isClean(String line) {
    return !StringUtils.isEmpty(line)
        && !line.startsWith(TERM_HORIZONTAL_RULE)
        && !line.startsWith(MARKDOWN_HEADING);
  }

  private GeneratorContext createSkeletonContext(
      String scopedir, String curatedDir, String versionTag) {
    String ownerRepo = getOwnerRepo(scopedir);
    String rootPath = getRootPath(scopedir);
    return new GeneratorContext(ownerRepo, scopedir, rootPath, versionTag, curatedDir);
  }

  private String getOwnerRepo(String scopedir) {
    String ownerRepo;
    if (local) {
      ownerRepo =
          scopedir; // no real concept of ownerRepo when it's local so just make it the scopedir
    } else {
      int httpIndex = scopedir.indexOf(HTTPS) + HTTPS.length();
      String[] parts = scopedir.substring(httpIndex).split("/");
      ownerRepo = String.join("/", parts[OWNER_PART_INDEX], parts[REPO_PART_INDEX]);
    }
    return ownerRepo;
  }

  private String getRootPath(String scopedir) {
    String rootPath;
    if (local) {
      rootPath = scopedir; // for local the rootPath and scopedir are identical
    } else {
      int treeIndex = scopedir.indexOf(TREE);
      if (treeIndex == -1) { // no tree found => root dir is empty
        return StringUtils.EMPTY;
      }
      treeIndex = treeIndex + TREE.length() + 1; // step past "tree" itself
      String branchDir = scopedir.substring(treeIndex);
      String[] branchDirParts = branchDir.split("/");
      String[] dirParts = Arrays.copyOfRange(branchDirParts, 1, branchDirParts.length);
      StringBuilder path = new StringBuilder();
      for (String dirPart : dirParts) {
        path.append(dirPart).append("/");
      }
      rootPath = path.deleteCharAt(path.length() - 1).toString();
    }
    return rootPath;
  }

  private String constructMrgFilepath(String glossaryDir, String versionTag) {
    String mrgFilename =
        (StringUtils.isEmpty(versionTag))
            ? String.join(".", DEFAULT_MRG_FILENAME, "yaml")
            : String.join(".", DEFAULT_MRG_FILENAME, versionTag, "yaml");
    return String.join("/", glossaryDir, mrgFilename);
  }
}
