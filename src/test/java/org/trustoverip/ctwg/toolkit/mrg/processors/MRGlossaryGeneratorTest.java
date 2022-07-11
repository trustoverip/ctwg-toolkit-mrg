package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.NO_SUCH_VERSION;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGModel;
import org.trustoverip.ctwg.toolkit.mrg.model.SAFModel;
import org.trustoverip.ctwg.toolkit.mrg.model.ScopeRef;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;
import org.trustoverip.ctwg.toolkit.mrg.model.Terminology;

/**
 * Many of the requirements are taken from
 *
 * @link <a
 *     href="https://essif-lab.github.io/framework/docs/tev2/tev2-toolbox#creating-an-mrg">...</a>
 * @author sih
 */
@ExtendWith(MockitoExtension.class)
class MRGlossaryGeneratorTest {

  private static final String SCOPEDIR =
      "https://github.com/essif-lab/framework/tree/master/docs/tev2";
  private static final String OWNER_REPO = "essif-lab/framework";
  private static final String ROOT_DIR_PATH = "docs";
  private static final String CURATED_DIR = "terms";
  private static final String VERSION_TAG = "mrgtest";
  private static final String FILTER_TERM = "tev2";
  private static final Path NO_GLOSSARY_SAF_PATH =
      Paths.get("./src/test/resources/no-glossary-saf.yaml");
  private static final Path VALID_SAF_PATH = Paths.get("./src/test/resources/saf-sample-1.yaml");
  private static final Path BASIC_TERM_FILE = Paths.get("./src/test/resources/basic-term.yaml");
  @Mock private ModelWrangler mockWrangler;
  private MRGlossaryGenerator generator;
  private String scopedir;
  private String safFilename;
  private String version;
  private SAFModel noGlossarySaf;
  private SAFModel validSaf;
  private GeneratorContext context;

  private List<Term> matchingTerms;

  private Term termScope;
  private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

  @BeforeEach
  void set_up() throws Exception {
    YamlWrangler parser = new YamlWrangler();
    scopedir = "https://github.com/essif-lab/framework/tree/master/docs/tev2";
    safFilename = "saf.yaml";
    version = "version";
    generator = new MRGlossaryGenerator(mockWrangler);
    validSaf = parser.parseSaf(new String(Files.readAllBytes(VALID_SAF_PATH)));
    noGlossarySaf = parser.parseSaf(new String(Files.readAllBytes(NO_GLOSSARY_SAF_PATH)));
    context = new GeneratorContext(OWNER_REPO, ROOT_DIR_PATH, VERSION_TAG, CURATED_DIR);
    String termStringTerm = new String(Files.readAllBytes(BASIC_TERM_FILE));
    Term termTerm = yamlMapper.readValue(termStringTerm, Term.class);
    matchingTerms = List.of(termTerm);
  }

  @Test
  @DisplayName("Should throw an exception when no glossary dir")
  void given_saf_with_no_glossary_dir_when_generate_then_throw_MRGException() {
    when(mockWrangler.getSaf(scopedir, safFilename)).thenReturn(noGlossarySaf);
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> generator.generate(scopedir, safFilename, version))
        .withMessage(MRGGenerationException.NO_GLOSSARY_DIR);
  }

  @Test
  @DisplayName("Should throw an exception when no glossary dir")
  void given_saf_with_no_such_version_tag_when_generate_then_throw_MRGException() {
    when(mockWrangler.getSaf(scopedir, safFilename)).thenReturn(validSaf);
    String badVersion = "moo";
    String expectedNoVersionMessage = String.format(NO_SUCH_VERSION, badVersion);
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> generator.generate(scopedir, safFilename, badVersion))
        .withMessage(expectedNoVersionMessage);
  }

  @Test
  @DisplayName("Given valid input generate should create MRG")
  void given_valid_input_generate_should_create_mrg() {
    when(mockWrangler.getSaf(scopedir, safFilename)).thenReturn(validSaf);
    when(mockWrangler.buildContextMap(SCOPEDIR, validSaf, VERSION_TAG))
        .thenReturn(Map.of(validSaf.getScope().getScopetag(), context));
    when(mockWrangler.fetchTerms(context, FILTER_TERM)).thenReturn(matchingTerms);
    MRGModel generatedMrg = generator.generate(scopedir, safFilename, VERSION_TAG);
    assertThat(generatedMrg).isNotNull();
    assertThat(generatedMrg.terminology())
        .isEqualTo(
            new Terminology(validSaf.getScope().getScopetag(), validSaf.getScope().getScopedir()));
    ScopeRef[] expectedScopesInOrder = validSaf.getScopes().toArray(new ScopeRef[0]);
    assertThat(generatedMrg.scopes()).containsExactly(expectedScopesInOrder);
    // TODO entries
  }

  /*
   *
   */
  @DisplayName(
      "Given an MRG with an existing id; When generating from a term file with the same id; Then the existing glossary entry should be replaced by the newer")
  @Test
  void testMatchingIdsAreReplaced() {
    // TODO impelment me
  }

  @DisplayName(
      "Given more than one formphrases field; When generating an MRG; Then the formfields must be mutually exclusive wrt elements")
  @Test
  void testFormfieldsAreMutuallyExclusiveByElement() {}
}
