package org.trustoverip.ctwg.toolkit.mrg.processors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AccessLevel;
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

/**
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
  private YamlWrangler yamlWrangler;
  private MRGConnector connector;

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
    SAFModel safModel = yamlWrangler.parseSaf(safAsString);
    return safModel;
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

  Map<String, GeneratorContext> buildContextMap(SAFModel saf, String versionTag) {
    Map<String, GeneratorContext> contextMap = new HashMap<>();
    // do local scope
    String localScope = saf.getScope().getScopetag();
    GeneratorContext localContext =
        createSkeletonContext(
            saf.getScope().getScopedir(), saf.getScope().getCuratedir(), versionTag);
    contextMap.put(localScope, localContext);
    // create skeleton external scopes
    List<ScopeRef> externalScopes = saf.getScopes();
    for (ScopeRef externalScope : externalScopes) {
      GeneratorContext generatorContext =
          createSkeletonContext(
              externalScope.getScopedir(), saf.getScope().getCuratedir(), versionTag);
      List<String> scopetags = externalScope.getScopetags();
      for (String scopetag : scopetags) {
        contextMap.put(scopetag, generatorContext);
      }
    }

    return contextMap;
  }

  void writeMrgToFile(MRGModel mrg, String mrgFilename) throws MRGGenerationException {
    Path mrgFilepath = Path.of(mrgFilename);
    yamlWrangler.writeMrg(mrgFilepath, mrg);
  }

  // TODO accept multiple term filters
  List<Term> fetchTerms(GeneratorContext localContext, String termFilter) {
    List<Term> terms = new ArrayList<>();
    String curatedPath =
        String.join("/", localContext.getRootDirPath(), localContext.getCuratedDir());
    List<FileContent> directoryContent =
        connector.getDirectoryContent(localContext.getOwnerRepo(), curatedPath);
    if (!directoryContent.isEmpty()) {
      terms =
          directoryContent.stream()
              .map(dirty -> this.cleanTermFile(dirty))
              .map(clean -> toYaml(clean))
              .filter(term -> term.getScope().equals(termFilter))
              .toList();
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
      log.info("Created term with id {}", t.getId());
    } catch (Exception e) {
      log.error("Got exception for file {}", fileContent.filename());
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
      treeIndex = treeIndex + +TREE.length() + 1; // step past "tree" itself
      String branchDir = scopedir.substring(treeIndex);
      String[] branchDirParts = branchDir.split("/");
      String[] dirParts = Arrays.copyOfRange(branchDirParts, 1, branchDirParts.length);
      StringBuilder path = new StringBuilder();
      for (int i = 0; i < dirParts.length; i++) {
        path.append(dirParts[i]).append("/");
      }
      rootPath = path.deleteCharAt(path.length() - 1).toString();
    }
    return rootPath;
  }
}
