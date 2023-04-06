package org.trustoverip.ctwg.toolkit.mrg.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author sih
 */
class MRGEntryTest {

  private static final String TERM_TERM_FILE_PATH = "./src/test/resources/basic-term.yaml";
  private static ObjectMapper yamlMapper;
  private Term termTerm;
  // common attributes
  private String expectedId;
  private String expectedScope;
  private String expectedGlossaryText;
  private String expectedTermtype;
  private String expectedTerm;
  private String expectedFormphrases;
  private String expectedGrouptags;
  private String expectedStatus;
  private String expectedCreated;
  private String expectedUpdated;
  private String expectedVsntag;
  // private String expectedCommit; // this is expected as null
  private String expectedContributors;
  // mrgentry specific
  private String expectedLocator;
  private String expectedHeadingidsEntry;
  private String expectedFilename;

  @BeforeAll
  static void setUpClas() throws Exception {
    yamlMapper = new ObjectMapper(new YAMLFactory());
  }

  @BeforeEach
  void setUp() throws Exception {
    String termAsString = new String(Files.readAllBytes(Paths.get(TERM_TERM_FILE_PATH)));
    termTerm = yamlMapper.readValue(termAsString, Term.class);
    termTerm.setFilename("basic-term.yaml");
    termTerm.setHeadings(new ArrayList<>());
    setUpExpectations();
    assertCommonTermAttributes(termTerm);
  }

  @DisplayName("Should be able to create a valid MRGEntry from a term")
  @Test
  void testConstructMrgEntryFromTerm() {
    MRGEntry mrgEntry = new MRGEntry(termTerm);
    assertCommonTermAttributes(mrgEntry);
    assertMrgEntrySpecificAttributes(mrgEntry);
  }

  private void setUpExpectations() {
    expectedId = "curated-text";
    expectedScope = "tev2";
    expectedTermtype = "concept";
    expectedTerm = "term";
    expectedFormphrases = "term{ss}, word{ss}, phrase{ss}";
    expectedGrouptags = "";
    expectedStatus = "proposed";
    expectedGlossaryText = "foo bar";
    expectedCreated = "2022-06-06";
    expectedUpdated = "2022-06-06";
    expectedVsntag = "v0.1";
    // note commit is expected to be null
    expectedContributors = "RieksJ";
    expectedFilename = "basic-term.yaml";
  }

  void assertCommonTermAttributes(Term t) {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(t.getId()).as("Check id").isEqualTo(expectedId);
    softly.assertThat(t.getScope()).as("Check scope").isEqualTo(expectedScope);
    softly.assertThat(t.getTermtype()).as("Check termtype").isEqualTo(expectedTermtype);
    softly.assertThat(t.getTerm()).as("Check term").isEqualTo(expectedTerm);
    softly.assertThat(t.getFormphrases()).as("Check formphrases").isEqualTo(expectedFormphrases);
    softly.assertThat(t.getGrouptags()).as("Check grouptags").isNull();
    softly.assertThat(t.getCommit()).as("Check commit").isNull();
    softly.assertThat(t.getStatus()).as("Check status").isEqualTo(expectedStatus);
    softly.assertThat(t.getCreated()).as("Check created").isEqualTo(expectedCreated);
    softly.assertThat(t.getUpdated()).as("Check updated").isEqualTo(expectedUpdated);
    softly.assertThat(t.getVsntag()).as("Check version tag").isEqualTo(expectedVsntag);
    softly.assertThat(t.getContributors()).as("Check contributors").isEqualTo(expectedContributors);
    softly.assertAll();
  }

  void assertMrgEntrySpecificAttributes(MRGEntry me) {
    assertThat(me.getLocator()).isEqualTo(expectedFilename);
    // TODO implement this
    assertThat(me.getHeadingids()).isEmpty();
  }
}
