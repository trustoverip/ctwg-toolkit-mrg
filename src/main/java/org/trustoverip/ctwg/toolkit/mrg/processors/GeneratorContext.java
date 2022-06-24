package org.trustoverip.ctwg.toolkit.mrg.processors;

import lombok.Getter;

/**
 * @author sih
 */
@Getter
public final class GeneratorContext {
  private final String ownerRepo;
  private final String rootDirPath;
  private final String safFilepath;

  private final String curatedDir;

  private final String versionTag;

  public GeneratorContext(
      String ownerRepo, String rootDirPath, String versionTag, String curatedDir) {
    this.ownerRepo = ownerRepo;
    this.rootDirPath = rootDirPath;
    this.curatedDir = curatedDir;
    this.versionTag = versionTag;
    this.safFilepath = String.join("/", rootDirPath, MRGlossaryGenerator.DEFAULT_SAF_FILENAME);
  }
}
