package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.CANNOT_CREATE_GLOSSARY_DIR;

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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.trustoverip.ctwg.toolkit.mrg.connectors.FileContent;
import org.trustoverip.ctwg.toolkit.mrg.connectors.LocalFSConnector;
import org.trustoverip.ctwg.toolkit.mrg.connectors.MRGConnector;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;
import org.trustoverip.ctwg.toolkit.mrg.model.SAFModel;
import org.trustoverip.ctwg.toolkit.mrg.model.ScopeRef;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;
import org.trustoverip.ctwg.toolkit.mrg.model.Version;

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

  private static final Pattern TERM_DIRECTIVE_PATTERN =
      Pattern.compile("\\[(\\w+)]\\(?@(\\w+-?\\w*):?([A-Za-z0-9.-_]+)?\\)?");
  private final YamlWrangler yamlWrangler;
  private final MRGConnector connector;

  @Setter(AccessLevel.NONE) // as we derive this from what type of connector has been passed
  private boolean local;

  ModelWrangler(YamlWrangler yamlWrangler, MRGConnector connector) {
    this.yamlWrangler = yamlWrangler;
    this.connector = connector;
    local = (connector instanceof LocalFSConnector);
  }

  // TODO getAllTerms and create a filter for the terms

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
    // create skeleton external scopes
    List<ScopeRef> externalScopes = saf.getScopes();
    for (ScopeRef externalScope : externalScopes) {
      List<String> scopetags = externalScope.getScopetags();
      for (String scopetag : scopetags) {
        // note that will set the version below
        GeneratorContext generatorContext =
            createSkeletonContext(
                externalScope.getScopedir(), saf.getScope().getCuratedir(), StringUtils.EMPTY);
        contextMap.put(scopetag, generatorContext);
      }
    }

    /*
     0. Find the version of interest (i.e. the input version) in the version elements list
     1. Extract term directives from SAF string as a Triple of <grouptag, scopetag, vsnTag>
     2. Then create a map by scopetag with Pair of: <List of terms, vsnTag>
     3. Then for each scopetag in the context map add the terms of interest and the vsn tag
    */
    Map<String, Pair<List<String>, String>> termsAndVersionByScopetag =
        getTermsAndVersionOfInterestByScopetag(saf, versionTag);
    // 3. add terms and versions of interest to context
    for (Entry<String, Pair<List<String>, String>> entry : termsAndVersionByScopetag.entrySet()) {
      GeneratorContext context = contextMap.get(entry.getKey());
      Pair<List<String>, String> termsAndVersion = entry.getValue();
      List<String> termsOfInterest = termsAndVersion.getLeft();
      String vsnTag = termsAndVersion.getRight();
      context.setTermsOfInterest(termsOfInterest);
      context.setVersionTag(vsnTag);
    }

    return contextMap;
  }

  /*
     0. Find the version of interest (i.e. the input version) in the version elements list
     1. Extract term directives from SAF string as a Triple of <grouptag, scopetag, vsnTag>
     2. Then create a map by scopetag with Pair of: <List of terms, vsnTag>
  */
  private Map<String, Pair<List<String>, String>> getTermsAndVersionOfInterestByScopetag(
      SAFModel saf, String versionTag) {
    Map<String, Pair<List<String>, String>> termsAndVersionByScopetag = new HashMap<>();
    // 0 Find the version of interest in the SAF
    Optional<Version> maybeVersion =
        saf.getVersions().stream().filter(v -> v.getVsntag().equals(versionTag)).findFirst();
    if (maybeVersion.isPresent()) {
      List<String> termDirectives = maybeVersion.get().getTerms();
      for (String td : termDirectives) {
        // 1 Extract terms each term and the version from the input string, e.g. [tev2](@tev2:0.1.7)
        Triple<String, String, String> termDirective = parse(td);
        String term = termDirective.getLeft();
        String grouptag = termDirective.getMiddle();
        String vsntag = termDirective.getRight();
        // 2 Create a map keyed by scopetag with a Pair of terms of interest(L) and version (R) as
        // the value
        Pair<List<String>, String> termsAndVersion =
            termsAndVersionByScopetag.getOrDefault(
                grouptag, Pair.of(new ArrayList<>(), StringUtils.EMPTY));
        List<String> terms = termsAndVersion.getLeft();
        terms.add(term); // add the term to the list
        termsAndVersionByScopetag.put(grouptag, Pair.of(terms, vsntag));
      }
    }
    return termsAndVersionByScopetag;
  }

  /*
   Grouptag and scopetag are mandatory but version is optional
   [grouptag]@scopetag:version
  */
  private Triple<String, String, String> parse(String td) {
    Triple<String, String, String> directive;
    Matcher m = TERM_DIRECTIVE_PATTERN.matcher(td);
    if (m.matches()) {
      String grouptag = m.group(1);
      String scopetag = m.group(2);
      String vsntag = (m.groupCount() == 3) ? m.group(3) : StringUtils.EMPTY;
      directive = Triple.of(grouptag, scopetag, vsntag);
    } else {
      directive = Triple.of(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    }
    return directive;
  }



  void writeMrgToFile(MRGModel mrg, String glossaryDir, String mrgFilename)
      throws MRGGenerationException {
    Path glossaryPath = Paths.get(glossaryDir);
    try {
      Files.createDirectories(glossaryPath);
    } catch (IOException ioe) {
      throw new MRGGenerationException(
          String.format(CANNOT_CREATE_GLOSSARY_DIR, glossaryPath.toAbsolutePath()));
    }
    Path mrgFilepath = Path.of(glossaryDir, mrgFilename);
    yamlWrangler.writeMrg(mrgFilepath, mrg);
  }

  List<Term> fetchTerms(GeneratorContext currentContext, String termFilter) {
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
              .filter(term -> term.getScope().equals(termFilter))
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
    return new GeneratorContext(ownerRepo, rootPath, versionTag, curatedDir);
  }

  private String getOwnerRepo(String scopedir) {
    String ownerRepo;
    if (local) {
      ownerRepo =
          scopedir; // no real concept of ownerRepo when it's local so just make it the scopedir
    } else {
      int httpIndex = scopedir.indexOf(HTTPS) + HTTPS.length();
      int treeIndex = scopedir.indexOf(TREE) + TREE.length();
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
        return "/";
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
}
