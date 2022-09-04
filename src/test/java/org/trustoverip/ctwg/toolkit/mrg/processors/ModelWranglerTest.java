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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.trustoverip.ctwg.toolkit.mrg.connectors.FileContent;
import org.trustoverip.ctwg.toolkit.mrg.connectors.GithubConnector;
import org.trustoverip.ctwg.toolkit.mrg.model.SAFModel;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;
import org.trustoverip.ctwg.toolkit.mrg.processors.TermsFilter.TermsFilterType;

/**
 * @author sih
 */
@SpringBootTest
class ModelWranglerTest {
  static final Path CURATED_TERM_TERM = Paths.get("./src/test/resources/terms/term.md");
  static final Path CURATED_TERM_SCOPE = Paths.get("./src/test/resources/terms/scope.md");
  private static final String MRGTEST_VERSION = "mrgtest";
  private static final Path INVALID_SAF = Paths.get("./src/test/resources/invalid-saf.yaml");
  private static final Path VALID_SAF = Paths.get("./src/test/resources/saf-sample-1.yaml");
  private static final String SCOPEDIR =
      "https://github.com/essif-lab/framework/tree/master/docs/tev2";
  private static final String OWNER_REPO = "essif-lab/framework";
  private static final String VALID_SAF_NAME = "valid.saf";
  private static final String INVALID_SAF_NAME = "invalid.saf";
  private static final String ROOT_DIR = "docs/tev2";
  private static final String VALID_SAF_TRIGGER = String.join("/", ROOT_DIR, VALID_SAF_NAME);
  private static final String INVALID_SAF_TRIGGER = String.join("/", ROOT_DIR, INVALID_SAF_NAME);
  private static final String CURATED_DIR_NAME = "terms";
  private static final String CURATED_DIR_PATH = String.join("/", ROOT_DIR, CURATED_DIR_NAME);
  @MockBean
  private GithubConnector mockReader;
  @Autowired private YamlWrangler yamlWrangler;
  @Autowired private ModelWrangler wrangler;
  private String invalidSafContent;
  private String validSafContent;
  private FileContent termStringTerm;
  private FileContent termStringScope;

  @BeforeEach
  void set_up() throws Exception {
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
        .isThrownBy(() -> wrangler.getSaf(SCOPEDIR, INVALID_SAF_NAME))
        .withMessage(UNABLE_TO_PARSE_SAF);
  }

  @Test
  void given_valid_saf_when_get_saf_should_populate_key_fields() {
    when(mockReader.getContent(OWNER_REPO, VALID_SAF_TRIGGER)).thenReturn(validSafContent);
    SAFModel saf = wrangler.getSaf(SCOPEDIR, VALID_SAF_NAME);
    String expectedScopetag = "tev2";
    int expectedScopesCount = 2;
    int expectedVersionsCount = 3;
    assertThat(saf.getScope()).isNotNull();
    assertThat(saf.getScope().getScopetag()).isEqualTo(expectedScopetag);
    assertThat(saf.getScopes()).hasSize(expectedScopesCount);
    assertThat(saf.getVersions()).hasSize(expectedVersionsCount);
  }

  @Test
  void given_valid_saf_when_build_context_map_then_return_populated_map() {
    when(mockReader.getContent(OWNER_REPO, VALID_SAF_TRIGGER)).thenReturn(validSafContent);
    String expectedScopetag = "tev2";
    SAFModel saf = wrangler.getSaf(SCOPEDIR, VALID_SAF_NAME);
    Map<String, GeneratorContext> contextMap =
        wrangler.buildContextMap(SCOPEDIR, saf, MRGTEST_VERSION);
    Assertions.assertThat(contextMap).isNotEmpty();
    // check local context
    Assertions.assertThat(contextMap).containsKey(expectedScopetag);
    GeneratorContext localContext = contextMap.get(expectedScopetag);
    assertThat(localContext.getOwnerRepo()).isEqualTo("essif-lab/framework");
    assertThat(localContext.getSafDirectory()).isEqualTo("docs/tev2");
    assertThat(localContext.getSafFilepath()).isEqualTo("docs/tev2/saf.yaml");
    // check external scopes
    Assertions.assertThat(contextMap)
        .containsOnlyKeys(expectedScopetag, "essiflab", "essif-lab", "ctwg", "toip-ctwg");
    // essiflab
    GeneratorContext essiflabContext = contextMap.get("essiflab");
    assertThat(essiflabContext.getOwnerRepo()).isEqualTo("essif-lab/framework");
    assertThat(essiflabContext.getSafDirectory()).isEqualTo("docs");
    // essif-lab
    GeneratorContext essifLabContext = contextMap.get("essif-lab");
    assertThat(essifLabContext.getOwnerRepo()).isEqualTo("essif-lab/framework");
    assertThat(essifLabContext.getSafDirectory()).isEqualTo("docs");
    // ctwg
    GeneratorContext ctwgContext = contextMap.get("ctwg");
    assertThat(ctwgContext.getOwnerRepo()).isEqualTo("trustoverip/ctwg");
    assertThat(ctwgContext.getSafDirectory()).isEmpty();
    // toip-ctwg
    GeneratorContext toipCtwgContext = contextMap.get("toip-ctwg");
    assertThat(toipCtwgContext.getOwnerRepo()).isEqualTo("trustoverip/ctwg");
    assertThat(toipCtwgContext.getSafDirectory()).isEmpty();
  }

  @DisplayName("""
      Given valid SAF and version with terms to be added and removed
      When build context map
      Then both added and remove tags should be saved in the appopriate list
      """)
  @Test
  void testAddAndRemoveFilterTypes() {
    when(mockReader.getContent(OWNER_REPO, VALID_SAF_TRIGGER)).thenReturn(validSafContent);
    String expectedScopetag = "tev2";
    SAFModel saf = wrangler.getSaf(SCOPEDIR, VALID_SAF_NAME);
    Map<String, GeneratorContext> contextMap =
        wrangler.buildContextMap(SCOPEDIR, saf, MRGTEST_VERSION);
    GeneratorContext localContext = contextMap.get(expectedScopetag);
    assertThat(localContext.getAddFilters()).containsExactly(TermsFilter.all());
    assertThat(localContext.getRemoveFilters()).containsExactly(TermsFilter.of(TermsFilterType.terms, "@, curated-text-body"));
  }



  @DisplayName("""
        Given valid SAF and multiple terms of interest and versions from multiple scopes
        When build context map
        Then capture all terms of interest and their appropriate versions
      """)
  @Test
  void testMulitpleTermsOfInterest() {
    // uses the 0x921456 version in the test SAF file (see ./src/test/resources/saf.yaml)
    when(mockReader.getContent(OWNER_REPO, VALID_SAF_TRIGGER)).thenReturn(validSafContent);
    SAFModel saf = wrangler.getSaf(SCOPEDIR, VALID_SAF_NAME);
    Map<String, GeneratorContext> contextMap =
        wrangler.buildContextMap(SCOPEDIR, saf, "0x921456");
    Assertions.assertThat(contextMap)
        .containsOnlyKeys("tev2", "essiflab", "essif-lab", "ctwg", "toip-ctwg");
    // essiflab scopetag
    GeneratorContext essiflabContext = contextMap.get("essiflab");
    assertThat(essiflabContext.getAddFilters()).isEmpty();
    assertThat(essiflabContext.getVersionTag()).isEmpty();
    // essif-lab scopetag
    GeneratorContext essifLabContext = contextMap.get("essif-lab");
    TermsFilter expectedPartyFilter = TermsFilter.of(TermsFilterType.terms, "party");
    TermsFilter expectedManagementFilter = TermsFilter.of(TermsFilterType.tags, "management");
    TermsFilter expectedCommunityFilter = TermsFilter.of(TermsFilterType.tags, "community");
    assertThat(essifLabContext.getAddFilters()).containsExactlyInAnyOrder(expectedCommunityFilter, expectedManagementFilter, expectedPartyFilter);
    assertThat(essifLabContext.getVersionTag()).isEqualTo("0.9.4");
    // ctwg scopetag
    GeneratorContext ctwg = contextMap.get("ctwg");
    assertThat(ctwg.getAddFilters()).isEmpty();
    assertThat(ctwg.getVersionTag()).isEmpty();
    // toip scopetag
    GeneratorContext toipCtwg = contextMap.get("toip-ctwg");
    assertThat(toipCtwg.getAddFilters()).isEmpty();
    assertThat(toipCtwg.getVersionTag()).isEmpty();
  }

  @DisplayName("Given valid terms in the directory when fetch terms then return the right terms")
  @Test
  void testFetchTermsValid() {
    int expectedSize = 2;
    when(mockReader.getDirectoryContent(OWNER_REPO, CURATED_DIR_PATH))
        .thenReturn(List.of(termStringTerm, termStringScope));
    GeneratorContext context =
        new GeneratorContext(OWNER_REPO, SCOPEDIR, ROOT_DIR, MRGTEST_VERSION, CURATED_DIR_NAME);
    List<Term> terms = wrangler.fetchTerms(context, List.of(TermsFilter.of(TermsFilterType.terms, "term"), TermsFilter.of(TermsFilterType.terms, "scope")), new ArrayList<>());
    assertThat(terms).hasSize(expectedSize);
    // specify both terms as a comma seprated list in a single filter
    terms = wrangler.fetchTerms(context, List.of(TermsFilter.of(TermsFilterType.terms, "term, scope")), new ArrayList<>());
    assertThat(terms).hasSize(expectedSize);
    // now only ask for the term term
    expectedSize = 1;
    terms = wrangler.fetchTerms(context, List.of(TermsFilter.of(TermsFilterType.terms, "term")), new ArrayList<>());
    assertThat(terms).hasSize(expectedSize);
    // ask for both terms but for some reason (in this case to test features) exclude one of them
    expectedSize = 1;
    terms = wrangler.fetchTerms(context, List.of(TermsFilter.of(TermsFilterType.terms, "term, scope")), List.of(TermsFilter.of(TermsFilterType.terms, "term")));
    assertThat(terms).hasSize(expectedSize);
    // ask for both terms and exclude both of them
    expectedSize = 0;
    terms = wrangler.fetchTerms(context, List.of(TermsFilter.of(TermsFilterType.terms, "term, scope")), List.of(TermsFilter.of(TermsFilterType.terms, "scope, term")));
    assertThat(terms).hasSize(expectedSize);
  }
}
