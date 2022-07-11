package org.trustoverip.ctwg.toolkit.mrg.connectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.trustoverip.ctwg.toolkit.mrg.processors.GeneratorContext;
import org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException;

/**
 * @author sih
 */
class LocalFSConnectorTest {

  private static final String WORKING_DIRECTORY_KEY = "user.dir";
  public static final String VALID_CONTENT_NAME = "./src/test/resources/saf-sample-1.yaml";
  public static final String VALID_DIRECTORY_NAME = "./src/test/resources/terms";
  private GeneratorContext context;
  private MRGConnector connector;
  private String termAsString;
  private String scopeAsString;

  @BeforeEach
  void setUp() throws Exception {
    String osSeparator = FileSystems.getDefault().getSeparator();
    // will use the SAF file in the ./src/test/resources directory so set this dir as the scopedir
    String scopedir = "./src/test/resources";
    context = new GeneratorContext(scopedir, scopedir, "mrgtest", "terms");
    connector = new LocalFSConnector();
    termAsString = new String(Files.readAllBytes(Paths.get("./src/test/resources/terms/term.md")));
    scopeAsString =
        new String(Files.readAllBytes(Paths.get("./src/test/resources/terms/scope.md")));
  }

  @DisplayName("Given content exists at the location when getContent then return expected content")
  @Test
  void testValidGetContent() {
    String expectedFirstLine = "#";
    String expectedSecondLine =
        "# This is a Scope Administration File that can be used in conjunction with TEv2.";
    String actualContent = connector.getContent(context.getOwnerRepo(), VALID_CONTENT_NAME);
    assertThat(actualContent).isNotNull();
    String[] actualLines = actualContent.split("\n");
    assertThat(actualLines[0]).isEqualTo(expectedFirstLine);
    assertThat(actualLines[1]).isEqualTo(expectedSecondLine);
  }

  @DisplayName("Given content does not exist at the location when getContent then throw exception")
  @Test
  void testInvalidGetContent() {
    String contentName = "foo";
    Path expectedPath = Paths.get(contentName);
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> connector.getContent(context.getOwnerRepo(), contentName))
        .withMessage(
            String.format(
                MRGGenerationException.COULD_NOT_READ_LOCAL_CONTENT, expectedPath.toUri()));
  }

  @Test
  @DisplayName("Given valid directory when getDirectoryContent then return correct content")
  void testValidGetDirectoryContent() {
    FileContent term = new FileContent("term.md", termAsString, new ArrayList<>());
    FileContent scope = new FileContent("scope.md", scopeAsString, new ArrayList<>());
    List<FileContent> contents =
        connector.getDirectoryContent(context.getOwnerRepo(), VALID_DIRECTORY_NAME);
    assertThat(contents).containsExactlyInAnyOrder(term, scope);
  }

  @DisplayName("Given a non existent directory when getDirectoryContent then throw exception")
  @Test
  void testInvalidGetDirectoryContent() {
    String directoryName = "foo";
    Path expectedPath = Paths.get(directoryName);
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> connector.getDirectoryContent(context.getOwnerRepo(), directoryName))
        .withMessage(
            String.format(
                MRGGenerationException.COULD_NOT_READ_LOCAL_CONTENT,
                expectedPath.toAbsolutePath().toUri()));
  }
}
