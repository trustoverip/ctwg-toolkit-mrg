package org.trustoverip.ctwg.toolkit.mrg.connectors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author sih
 */
class GithubConnectorIntegrationTest {

  private String repo;
  private String curatedDir;
  private String nonExistentDir;
  private GithubConnector reader;

  @BeforeEach
  void setUp() {
    repo = "essif-lab/framework";
    curatedDir = "docs/tev2/terms";
    nonExistentDir = "foo";
    reader = new GithubConnector();
  }

  @Test
  @DisplayName("When reading a valid directory getDirectoryContent should return all the items")
  void testValidDirectory() {
    /*
    This is a bit of a brittle test as files could be added or removed whilst the terms are being defined
    There are 17 files as of 2022-06-18 so leave a bit of leeway especially on the upper bound
    */
    int expectedSizeLow = 13;
    int expectedSizeHigh = 30;
    List<FileContent> directoryItems = reader.getDirectoryContent(repo, curatedDir);
    assertThat(directoryItems).hasSizeBetween(expectedSizeLow, expectedSizeHigh);
  }

  @Test
  @DisplayName("When reading an invalid directory getDirectoryContent should return an empty array")
  void testInvalidDirectory() {
    List<FileContent> directoryItems = reader.getDirectoryContent(repo, nonExistentDir);
    assertThat(directoryItems).isEmpty();
  }
}
