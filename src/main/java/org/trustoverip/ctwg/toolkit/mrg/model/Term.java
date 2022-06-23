package org.trustoverip.ctwg.toolkit.mrg.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sih
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Term implements Comparable<Term> {
  @EqualsAndHashCode.Include private String id;
  private String scope;
  private String termtype;
  private String termid;
  private String formphrases;
  private String status;
  private String grouptags;
  private String created;
  private String updated;
  private String vsntag;
  private String commit;
  private String contributors;

  @JsonIgnore
  @Getter(AccessLevel.PROTECTED)
  private String filename;

  @JsonIgnore
  @Getter(AccessLevel.PROTECTED)
  private List<String> headings;

  @Override
  public int compareTo(Term other) {
    return this.getId().compareTo(other.getId());
  }
}
