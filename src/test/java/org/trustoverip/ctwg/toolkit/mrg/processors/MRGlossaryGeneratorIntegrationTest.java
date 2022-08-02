package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;

/**
 * @author sih
 */
class MRGlossaryGeneratorIntegrationTest {

  private static final String GITHUB_SCOPEDIR =
      "https://github.com/datasoc-ltd/framework/tree/master/docs/tev2";

  private static final String LOCAL_SCOPEDIR = "./src/test/resources/essif-lab-skeleton/tev2";

  private static final String VERSION_TAG = "mrgtest";
  private MRGlossaryGenerator generator;

  @BeforeEach
  void setUp() {

  }

  @Test
  @DisplayName("Should generate an MRG from the datasoc-ltd/framework sample on GitHub")
  void testGenerateTev2Remote() {
    generator = new MRGlossaryGenerator(false);
    int expectedNumberOfEntries = 30;
    MRGModel model =
        generator.generate(GITHUB_SCOPEDIR, MRGlossaryGenerator.DEFAULT_SAF_FILENAME, VERSION_TAG);
    assertThat(model).isNotNull();
    assertThat(model.terminology()).isNotNull();
    assertThat(model.scopes()).isNotNull();
    assertThat(model.entries()).hasSize(expectedNumberOfEntries);
    File expectedMrg = new File(String.join(".", "mrg", VERSION_TAG, "yaml"));
    assertThat(expectedMrg).exists();
  }

  @Test
  @DisplayName("Should generate an MRG from a local copy of the essif-lab/framework repo")
  //
  void testGenerateTev2Local() {
    generator = new MRGlossaryGenerator(true);
    MRGModel model =
        generator.generate(LOCAL_SCOPEDIR, MRGlossaryGenerator.DEFAULT_SAF_FILENAME, VERSION_TAG);
    assertThat(model).isNotNull();
    assertThat(model.terminology()).isNotNull();
    assertThat(model.scopes()).isNotNull();
    assertThat(model.entries()).isNotEmpty();
    File expectedMrg = new File(String.join(".", "mrg", VERSION_TAG, "yaml"));
    assertThat(expectedMrg).exists();
  }
}
