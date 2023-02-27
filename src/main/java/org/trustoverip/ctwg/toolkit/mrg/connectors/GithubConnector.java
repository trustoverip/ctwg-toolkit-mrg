package org.trustoverip.ctwg.toolkit.mrg.connectors;

import static org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException.GITHUB_LOGON_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.trustoverip.ctwg.toolkit.mrg.processors.MRGGenerationException;

/**
 * @author sih
 */
@Slf4j
@Service
@Primary
public class GithubConnector implements MRGConnector {
  private static final String GH_NAME = "gh_user";

  private static final String GH_TOKEN = "gh_token";
  private final GitHub gh;


  public GithubConnector() {
    String user = null;
    try {
      user = System.getenv(GH_NAME);
      log.info("Connecting to Github as {}", user);
      gh = GitHub.connect(user, System.getenv(GH_TOKEN));
    } catch (IOException ioe) {
      throw new MRGGenerationException(String.format(GITHUB_LOGON_ERROR, user));
    }
  }

  @Override
  public String getContent(final String repository, final String contentName) {
    GHRepository repo;
    try {
      repo = gh.getRepository(repository);
      GHContent content = repo.getFileContent(contentName);
      if (null == content) {
        return null;
      }
      return contentAsString(content);
    } catch (GHFileNotFoundException e) {
      log.warn("Could not find GitHub resource {} in repo {}", contentName, repository);
      return null;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<FileContent> getDirectoryContent(
      final String repository, final String directoryName) {
    List<FileContent> contents = new ArrayList<>();
    try {
      GHRepository repo = gh.getRepository(repository);
      List<GHContent> gitContents = repo.getDirectoryContent(directoryName);
      if (gitContents != null && !gitContents.isEmpty()) {
        contents =
            gitContents.stream()
                .filter(GHContent::isFile)
                .map(
                    gc ->
                        new FileContent(gc.getName(), this.contentAsString(gc), new ArrayList<>()))
                .collect(Collectors.toList());
      }
    } catch (GHFileNotFoundException e) {
      log.warn("There's no such directory {} in the repo {}", directoryName, repository);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return contents;
  }

  private String contentAsString(GHContent content) {
    try (InputStream is = content.read()) {
      return new String(is.readAllBytes(), StandardCharsets.US_ASCII);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
