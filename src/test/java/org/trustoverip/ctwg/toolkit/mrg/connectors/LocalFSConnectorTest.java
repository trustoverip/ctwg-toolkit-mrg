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
  private GeneratorContext context;
  private MRGConnector connector;
  private String termAsString;
  private String scopeAsString;

  @BeforeEach
  void setUp() throws Exception {
    String osSeparator = FileSystems.getDefault().getSeparator();
    // will use the SAF file in the ./src/test/resources directory so set this dir as the scopedir
    String projectRoot = System.getProperty(WORKING_DIRECTORY_KEY);
    String scopedir = String.join(osSeparator, projectRoot, "src", "test", "resources");
    context = new GeneratorContext(scopedir, scopedir, "mrgtest", "terms");
    connector = new LocalFSConnector();
    termAsString = new String(Files.readAllBytes(Paths.get("./src/test/resources/terms/term.md")));
    scopeAsString =
        new String(Files.readAllBytes(Paths.get("./src/test/resources/terms/scope.md")));
  }

  @DisplayName("Given content exists at the location when getContent then return expected content")
  @Test
  void testValidGetContent() {
    String contentName = "saf-sample-1.yaml";
    String expectedFirstLine = "#";
    String expectedSecondLine =
        "# This is a Scope Administration File that can be used in conjunction with TEv2.";
    String actualContent = connector.getContent(context.getOwnerRepo(), contentName);
    assertThat(actualContent).isNotNull();
    String[] actualLines = actualContent.split("\n");
    assertThat(actualLines[0]).isEqualTo(expectedFirstLine);
    assertThat(actualLines[1]).isEqualTo(expectedSecondLine);
  }

  @DisplayName("Given content does not exist at the location when getContent then throw exception")
  @Test
  void testInvalidGetContent() {
    String contentName = "foo";
    Path expectedPath = Path.of(context.getRootDirPath(), contentName);
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> connector.getContent(context.getOwnerRepo(), contentName))
        .withMessage(
            String.format(
                MRGGenerationException.COULD_NOT_READ_LOCAL_CONTENT,
                expectedPath.toAbsolutePath().toUri()));
  }

  @Test
  @DisplayName("Given valid directory when getDirectoryContent then return correct content")
  void testValidGetDirectoryContent() {
    String directoryName = "terms";
    FileContent term = new FileContent("term.md", termAsString, new ArrayList<>());
    FileContent scope = new FileContent("scope.md", scopeAsString, new ArrayList<>());
    List<FileContent> contents =
        connector.getDirectoryContent(context.getOwnerRepo(), directoryName);
    assertThat(contents).containsExactlyInAnyOrder(term, scope);
  }

  @DisplayName("Given a non existent directory when getDirectoryContent then throw exception")
  @Test
  void testInvalidGetDirectoryContent() {
    String directoryName = "foo";
    Path expectedPath = Path.of(context.getRootDirPath(), directoryName);
    assertThatExceptionOfType(MRGGenerationException.class)
        .isThrownBy(() -> connector.getDirectoryContent(context.getOwnerRepo(), directoryName))
        .withMessage(
            String.format(
                MRGGenerationException.COULD_NOT_READ_LOCAL_CONTENT,
                expectedPath.toAbsolutePath().toUri()));
  }
}
