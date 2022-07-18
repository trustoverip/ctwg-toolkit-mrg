package org.trustoverip.ctwg.toolkit.mrg.processors;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sih
 */
@Getter
public final class GeneratorContext {
  private final String ownerRepo;
  private final String rootDirPath;
  private final String safFilepath;

  private final String curatedDir;

  @Setter private String versionTag;

  @Setter private List<String> termsOfInterest;

  public GeneratorContext(
      String ownerRepo, String rootDirPath, String versionTag, String curatedDir) {
    this.ownerRepo = ownerRepo;
    this.rootDirPath = rootDirPath;
    this.curatedDir = curatedDir;
    this.versionTag = versionTag;
    this.safFilepath = String.join("/", rootDirPath, MRGlossaryGenerator.DEFAULT_SAF_FILENAME);
    this.termsOfInterest = new ArrayList<>();
  }
}
