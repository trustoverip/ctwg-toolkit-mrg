package org.trustoverip.ctwg.toolkit.mrg.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;

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

  @Setter private List<Predicate<Term>> filters;

  public GeneratorContext(
      String ownerRepo, String rootDirPath, String versionTag, String curatedDir) {
    this.ownerRepo = ownerRepo;
    this.rootDirPath = rootDirPath;
    this.curatedDir = curatedDir;
    this.versionTag = versionTag;
    this.safFilepath = String.join("/", rootDirPath, MRGlossaryGenerator.DEFAULT_SAF_FILENAME);
    this.filters = new ArrayList<>();
  }
}
