package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.UNABLE_TO_PARSE_SAF;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trustoverip.ctwg.toolkit.mrg.connectors.FileContent;
import org.trustoverip.ctwg.toolkit.mrg.connectors.GithubReader;
import org.trustoverip.ctwg.toolkit.mrg.model.SAFModel;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;

/**
 * @author sih
 */
@ExtendWith(MockitoExtension.class)
class ModelWranglerTest {

  static final Path CURATED_TERM_TERM = Paths.get("./src/test/resources/terms/term.md");
  static final Path CURATED_TERM_SCOPE = Paths.get("./src/test/resources/terms/scope.md");
  private static final String MRGTEST_VERSION = "mrgtest";
  private static final Path INVALID_SAF = Paths.get("./src/test/resources/invalid-saf.yaml");
  private static final Path VALID_SAF = Paths.get("./src/test/resources/saf-sample-1.yaml");
  private static final String REPO = "https://github.com/essif-lab/framework/tree/master/docs/tev2";
  private static final String OWNER_REPO = "essif-lab/framework";
  private static final String VALID_SAF_NAME = "valid.saf";
  private static final String INVALID_SAF_NAME = "invalid.saf";
  private static final String VALID_SAF_TRIGGER = String.join("", "docs/tev2/", VALID_SAF_NAME);
  private static final String INVALID_SAF_TRIGGER = String.join("", "docs/tev2/", INVALID_SAF_NAME);
  private static final String ROOT_DIR = "docs/tev2";
  private static final String CURATED_DIR_NAME = "terms";
  private static final String SCOPETAG = "tev2";
  private static final String CURATED_DIR_PATH = String.join("/", ROOT_DIR, CURATED_DIR_NAME);
  @Mock private GithubReader mockReader;
  private YamlWrangler yamlWrangler = new YamlWrangler();
  private ModelWrangler wrangler;
  private String invalidSafContent;
  private String validSafContent;
  private FileContent termStringTerm;
  private FileContent termStringScope;

  @BeforeEach
  void set_up() throws Exception {
    wrangler = new ModelWrangler(yamlWrangler, mockReader);
    invalidSafContent = new String(Files.readAllBytes(INVALID_SAF));
    validSafContent = new String(Files.readAllBytes(VALID_SAF));
    termStringTerm =
        new FileContent(
            "terms/term.md", new String(Files.readAllBytes(CURATED_TERM_TERM)), new ArrayList<>());
    termStringScope =
        new FileContent(
            "terms/scope.md",
            new String(Files.readAllBytes(CURATED_TERM_SCOPE)),
            new ArrayList<>());
  }

  @Test
  void given_invalid_saf_when_get_saf_should_throw_exception() {
    when(mockReader.getContent(OWNER_REPO, INVALID_SAF_TRIGGER)).thenReturn(invalidSafContent);
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> wrangler.getSaf(REPO, INVALID_SAF_NAME))
        .withMessage(UNABLE_TO_PARSE_SAF);
  }

  @Test
  void given_valid_saf_when_get_saf_should_populate_key_fields() throws Exception {
    when(mockReader.getContent(OWNER_REPO, VALID_SAF_TRIGGER)).thenReturn(validSafContent);
    SAFModel saf = wrangler.getSaf(REPO, VALID_SAF_NAME);
    String expectedScopetag = "tev2";
    int expectedScopesCount = 2;
    int expectedVersionsCount = 3;
    assertThat(saf.getScope()).isNotNull();
    assertThat(saf.getScope().getScopetag()).isEqualTo(expectedScopetag);
    assertThat(saf.getScopes()).hasSize(expectedScopesCount);
    assertThat(saf.getVersions()).hasSize(expectedVersionsCount);
  }

  @Test
  void given_valid_saf_when_build_context_map_then_return_populated_map() throws Exception {
    when(mockReader.getContent(OWNER_REPO, VALID_SAF_TRIGGER)).thenReturn(validSafContent);
    String expectedScopetag = "tev2";
    SAFModel saf = wrangler.getSaf(REPO, VALID_SAF_NAME);
    Map<String, GeneratorContext> contextMap = wrangler.buildContextMap(saf, MRGTEST_VERSION);
    Assertions.assertThat(contextMap).isNotEmpty();
    // check local context
    Assertions.assertThat(contextMap).containsKey(expectedScopetag);
    GeneratorContext localContext = contextMap.get(expectedScopetag);
    assertThat(localContext.getOwnerRepo()).isEqualTo("essif-lab/framework");
    assertThat(localContext.getRootDirPath()).isEqualTo("docs/tev2");
    assertThat(localContext.getSafFilepath()).isEqualTo("docs/tev2/saf.yaml");
    // check external scopes
    Assertions.assertThat(contextMap)
        .containsOnlyKeys(expectedScopetag, "essiflab", "essif-lab", "ctwg", "toip-ctwg");
    // essiflab
    GeneratorContext essiflabContext = contextMap.get("essiflab");
    assertThat(essiflabContext.getOwnerRepo()).isEqualTo("essif-lab/framework");
    assertThat(essiflabContext.getRootDirPath()).isEqualTo("docs");
    // essif-lab
    GeneratorContext essifLabContext = contextMap.get("essif-lab");
    assertThat(essifLabContext.getOwnerRepo()).isEqualTo("essif-lab/framework");
    assertThat(essifLabContext.getRootDirPath()).isEqualTo("docs");
    // ctwg
    GeneratorContext ctwgContext = contextMap.get("ctwg");
    assertThat(ctwgContext.getOwnerRepo()).isEqualTo("trustoverip/ctwg");
    assertThat(ctwgContext.getRootDirPath()).isEqualTo("/");
    // toip-ctwg
    GeneratorContext toipCtwgContext = contextMap.get("toip-ctwg");
    assertThat(toipCtwgContext.getOwnerRepo()).isEqualTo("trustoverip/ctwg");
    assertThat(toipCtwgContext.getRootDirPath()).isEqualTo("/");
  }

  @DisplayName("Given valid terms in the directory when fetch terms then return the right terms")
  @Test
  void testFetchTermsValid() throws Exception {
    int expectedSize = 2;
    when(mockReader.getDirectoryContent(OWNER_REPO, CURATED_DIR_PATH))
        .thenReturn(List.of(termStringTerm, termStringScope));
    GeneratorContext context =
        new GeneratorContext(OWNER_REPO, ROOT_DIR, MRGTEST_VERSION, CURATED_DIR_NAME);
    List<Term> terms = wrangler.fetchTerms(context, SCOPETAG);
    assertThat(terms).hasSize(expectedSize);
  }
}
