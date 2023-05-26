package org.trustoverip.ctwg.toolkit.mrg.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author sih
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public final class Version {
  private String vsntag;
  private List<String> altvsntags;
  private String mrgfile;
  private List<String> termselcrit;
  private String status;
  private String from;
  private String to;
}
