package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.CANNOT_CREATE_GLOSSARY_DIR;
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
import java.util.Locale;
import java.util.Map;
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
class ModelWrangler {

  private static final String TERM_HORIZONTAL_RULE = "---";
  private static final String MARKDOWN_HEADING = "#";
  private static final String HTTPS = "https://";
  private static final String TREE = "tree";
  private static final String MRG_FILE_EXTENSION = "yaml";
  private static final int OWNER_PART_INDEX = 1;
  private static final int REPO_PART_INDEX = 2;
  private static final String MULTIPLE_USE_FIELDS = "multiple-use fields";
  private static final String GENERIC_FRONT_MATTER = "generic front-matter";

  private static final Pattern TERM_EXPRESSION_MATCHER =
      Pattern.compile("(tags|termids|\\*)\\[?([\\w, ]*)]?@?(\\w+-?\\w*)?:?([A-Za-z0-9.-_]+)?");
  private static final int MATCH_FILTER_TYPE_GROUP = 1;
  private static final int MATCH_VALS_GROUP = 2;
  private static final int MATCH_SCOPETAG_GROUP = 3;
  private static final int MATCH_VERSION_GROUP = 4;
  public static final String DEFAULT_BRANCH = "master";
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
    Map<String, String> versionsByScopetag = new HashMap<>();
    Map<String, List<Predicate<Term>>> filtersByScopetag = new HashMap<>();
    if (optionalVersion.isPresent()) {
      Version versionOfInterest = optionalVersion.get();
      List<String> termExpressions = versionOfInterest.getTermselcrit();
      for (String expression : termExpressions) {
        Matcher m = TERM_EXPRESSION_MATCHER.matcher(expression);
        if (m.matches()) {
          String scopetag = m.group(MATCH_SCOPETAG_GROUP);
          versionsByScopetag.put(scopetag, m.group(MATCH_VERSION_GROUP));
          filtersByScopetag
              .computeIfAbsent(scopetag, k -> new ArrayList<>())
              .add(termsFilter(m.group(MATCH_FILTER_TYPE_GROUP), m.group(MATCH_VALS_GROUP)));
        } else {
          log.warn(
              "The  expression: {} in the version.termselcrit element could not be parsed",
              expression);
        }
      }
    }
    // create external scopes
    List<ScopeRef> externalScopes = saf.getScopes();
    for (ScopeRef externalScope : externalScopes) {
      List<String> scopetags = externalScope.getScopetags();
      for (String scopetag : scopetags) {
        GeneratorContext generatorContext =
            createSkeletonContext(
                externalScope.getScopedir(),
                StringUtils.EMPTY,
                StringUtils.EMPTY); // will find dirs later
        generatorContext.setVersionTag(
            versionsByScopetag.getOrDefault(scopetag, StringUtils.EMPTY));
        generatorContext.setFilters(filtersByScopetag.getOrDefault(scopetag, new ArrayList<>()));
        contextMap.put(scopetag, generatorContext);
      }
    }


    return contextMap;
  }

  private TermsFilter termsFilter(String typeString, String vals) {
    TermsFilterType type;
    if (typeString.equals(ALL_TAGS)) {
      type = TermsFilterType.all;
    } else {
      type = TermsFilterType.valueOf(typeString);
    }
    return TermsFilter.of(type, vals);
  }

  /*
   Grouptag and scopetag are mandatory but version is optional
   [grouptag]@scopetag:version
  */

  MRGModel getMrg(GeneratorContext context, String glossaryDir) {
    String mrgFilename = constructFilename(context.getVersionTag());
    String mrgPath = String.join("/", glossaryDir, mrgFilename);
    String mrgAsYaml = connector.getContent(context.getRootDirPath(), mrgPath);
    return (null == mrgAsYaml) ? null : yamlWrangler.parseMrg(mrgAsYaml);
  }

  String writeMrgToFile(MRGModel mrg, String glossaryDir, String versionTag)
      throws MRGGenerationException {
    String mrgFilename = constructFilename(versionTag);
    log.debug(String.format("MRG filename to be generated is: %s", mrgFilename));
    Path glossaryPath = Paths.get(glossaryDir);
    try {
      Files.createDirectories(glossaryPath);
    } catch (IOException ioe) {
      throw new MRGGenerationException(
          String.format(CANNOT_CREATE_GLOSSARY_DIR, glossaryPath.toAbsolutePath()));
    }
    Path mrgFilepath = Path.of(glossaryDir, mrgFilename);
    yamlWrangler.writeMrg(mrgFilepath, mrg);
    return mrgFilepath.toString();
  }

  List<Term> fetchTerms(GeneratorContext currentContext, List<Predicate<Term>> filters) {
    Predicate<Term> consolidatedFilter;
    if (null == filters || filters.isEmpty()) {
      consolidatedFilter = TermsFilter.all();
    } else {
      consolidatedFilter = filters.stream().reduce(Predicate::or).get();
    }
    List<Term> terms = new ArrayList<>();
    String curatedPath =
        String.join("/", currentContext.getRootDirPath(), currentContext.getCuratedDir());
    List<FileContent> directoryContent =
        connector.getDirectoryContent(currentContext.getOwnerRepo(), curatedPath);
    if (!directoryContent.isEmpty()) {
      terms =
          directoryContent.stream()
              .map(this::cleanTermFile)
              .map(this::toYaml)
              .filter(consolidatedFilter)
              .collect(Collectors.toList());
    }
    return terms;
  }

  private FileContent cleanTermFile(FileContent dirtyContent) {
    StringBuilder cleanYaml = new StringBuilder();
    String[] lines = dirtyContent.content().split("\n");
    boolean sectionOfInterest = false;
    for (String line : lines) {
      if (line.startsWith(MARKDOWN_HEADING)) {
        sectionOfInterest = isSectionOfInterest(line);
      }
      if (isClean(line) && sectionOfInterest) {
        log.debug(
            "Found something of interest in file: {}\nline: {}", dirtyContent.filename(), line);
        cleanYaml.append(line);
        cleanYaml.append("\n");
      }
    }
    return new FileContent(dirtyContent.filename(), cleanYaml.toString(), dirtyContent.headings());
  }

  private Term toYaml(FileContent fileContent) {
    Term t = null;
    try {
      t = yamlWrangler.parseTerm(fileContent.content());
      t.setFilename(fileContent.filename());
      t.setHeadings(fileContent.headings());
      log.info("... Creating entry from term with id = {} ...", t.getId());
    } catch (Exception e) {
      log.error("Couldn't read or parse the following term file: {}", fileContent.filename());
    }
    return t;
  }

  /*
    Used to filter out items in the file that aren't of interest to the MRG but more oriented
    towards human-readable content
  */
  private boolean isSectionOfInterest(String line) {
    return !StringUtils.isEmpty(line)
        && (line.toLowerCase(Locale.ROOT).contains(MULTIPLE_USE_FIELDS)
            || line.toLowerCase(Locale.ROOT).contains(GENERIC_FRONT_MATTER));
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
      if (treeIndex == -1) { // no tree found => root dir is /
        return String.join("/", TREE, DEFAULT_BRANCH); // TODO get this from github
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

  private String constructFilename(String versionTag) {
    String vsntag = (null == versionTag) ? StringUtils.EMPTY : versionTag;
    return String.join(".", DEFAULT_MRG_FILENAME, vsntag, "yaml");
  }
}
