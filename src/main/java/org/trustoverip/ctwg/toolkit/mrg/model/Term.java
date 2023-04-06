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
  @EqualsAndHashCode.Include private String term;
  private String id;
  private String scope;
  private String scopetag;
  private String locator;
  private String isa;
  private String termType;
  private String formphrases;
  private String status;
  private String synonyms;
  private String grouptags;
  private String glossaryText;
  private String created;
  private String updated;
  private String vsntag;
  private String commit;
  private String contributors;
  private String attribution;
  private String originalLicense;

  @JsonIgnore
  @Getter(AccessLevel.PROTECTED)
  private String filename;

  @Getter(AccessLevel.PROTECTED)
  private String navurl;

  @Getter(AccessLevel.PROTECTED)
  private List<String> headings;

  @Override
  public int compareTo(Term other) {
    return this.term.compareTo(other.term);
  }
}
