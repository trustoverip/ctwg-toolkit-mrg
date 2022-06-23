package org.trustoverip.ctwg.toolkit.mrg.model;

import java.util.List;
import lombok.Data;

/**
 * @author sih
 */
@Data
public final class Scope {
  private String scopetag;
  private String scopedir;
  private String curatedir;
  private String glossarydir;
  private String mrgfile;
  private String hrgfile;
  private String license;
  private List<String> statuses;
  private String issues;
  private String website;
  private String slack;
  private List<Curator> curators;
}
