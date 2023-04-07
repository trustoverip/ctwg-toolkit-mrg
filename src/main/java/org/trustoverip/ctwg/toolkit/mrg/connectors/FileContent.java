package org.trustoverip.ctwg.toolkit.mrg.connectors;

import java.util.List;

/**
 * @author sih
 */
public record FileContent(String filename, String content, String htmlLink, List<String> headings) {

}
