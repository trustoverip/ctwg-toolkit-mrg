package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGlossaryGenerator.DEFAULT_SAF_FILENAME;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trustoverip.ctwg.toolkit.mrg.connectors.GithubReader;

/**
 * @author sih
 */
class ModelWranglerIntegrationTest {

  private static final String TEV2_SCOPEDIR =
      "https://github.com/essif-lab/framework/tree/master/docs/tev2";
  private static final String PRIVATE_SCOPEDIR = "https://github.com/sih/scratch";
  private static final String SAF_FILENAME = "saf.yaml";
  private static final String MRGTEST_VERSION = "mrgtest";
  private String expectedOwnerRepo = "essif-lab/framework";
  private String expectedRootDirPath = "docs/tev2";
  private String expectedScopetag = "tev2";
  private String expectedSafFilepath = "docs/tev2/saf.yaml";

  private ModelWrangler wrangler;

  @BeforeEach
  void set_up() {
    wrangler = new ModelWrangler(new YamlWrangler(), new GithubReader());
  }

  @Test
  void given_private_scopedir_when_get_saf_as_string_then_return_saf_exception() throws Exception {
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> wrangler.getSafAsString(PRIVATE_SCOPEDIR, SAF_FILENAME))
        .withMessage(
            String.format(
                MRGGenerationException.NOT_FOUND,
                String.join("", PRIVATE_SCOPEDIR, "/", DEFAULT_SAF_FILENAME)));
  }

  @Test
  void given_non_existent_saf_when_get_saf_as_string_then_return_saf_exception() throws Exception {
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> wrangler.getSafAsString(TEV2_SCOPEDIR, "foo"))
        .withMessage(
            String.format(
                MRGGenerationException.NOT_FOUND, String.join("", TEV2_SCOPEDIR, "/", "foo")));
  }

  @Test
  void given_saf_that_exists_when_get_saf_as_string_then_return_valid_content() throws Exception {
    String safString = wrangler.getSafAsString(TEV2_SCOPEDIR, SAF_FILENAME);
    assertThat(safString).isNotNull();
  }
}
