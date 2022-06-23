package org.trustoverip.ctwg.toolkit.mrg.connectors;

import java.util.List;

/**
 * @author sih
 */
public interface MRGConnector {

  String getContent(String repository, String contentName);

  List<FileContent> getDirectoryContent(String repository, String directoryName);
}
