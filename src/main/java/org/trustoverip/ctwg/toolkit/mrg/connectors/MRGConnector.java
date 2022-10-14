package org.trustoverip.ctwg.toolkit.mrg.connectors;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author sih
 */
@Service
public interface MRGConnector {

  String getContent(String repository, String contentName);

  List<FileContent> getDirectoryContent(String repository, String directoryName);
}
