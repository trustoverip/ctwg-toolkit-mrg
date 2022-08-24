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
  private final String absoluteRepo;
  private final String safDirectory;
  private final String safFilepath;

  private final String curatedDir;

  @Setter private String versionTag;

  @Setter private List<Predicate<Term>> addFilters;
  @Setter private List<Predicate<Term>> removeFilters;

  public GeneratorContext(
      String ownerRepo,
      String absoluteRepo,
      String safDirectory,
      String versionTag,
      String curatedDir) {
    this.ownerRepo = ownerRepo;
    this.absoluteRepo = absoluteRepo;
    this.safDirectory = safDirectory;
    this.curatedDir = curatedDir;
    this.versionTag = versionTag;
    this.safFilepath = String.join("/", safDirectory, MRGlossaryGenerator.DEFAULT_SAF_FILENAME);
    this.addFilters = new ArrayList<>();
    this.removeFilters = new ArrayList<>();
  }
}
