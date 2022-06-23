package org.trustoverip.ctwg.toolkit.mrg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sih
 */
@Getter
@Setter
public class MRGEntry extends Term {
  private List<String> headingids;
  private String locator;
  private String navurl;

  public MRGEntry() {}

  public MRGEntry(Term t) {
    this.setId(t.getId());
    this.setScope(t.getScope());
    this.setTermtype(t.getTermtype());
    this.setTermid(t.getTermid());
    this.setFormphrases(t.getFormphrases());
    this.setGrouptags(t.getGrouptags());
    this.setStatus(t.getStatus());
    this.setCreated(t.getCreated());
    this.setUpdated(t.getUpdated());
    this.setVsntag(t.getVsntag());
    this.setCommit(t.getCommit());
    this.setContributors(t.getContributors());
    this.setLocator(t.getFilename());
    this.setNavurl("To be definedT");
    this.setHeadingids(t.getHeadings());
  }

  @Override
  @JsonProperty("scopetag") // called scope in Term but scopetag in MRGEntry
  public String getScope() {
    return super.getScope();
  }
}
