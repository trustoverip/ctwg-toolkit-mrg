package org.trustoverip.ctwg.toolkit.mrg.model;

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
    this.setTermType(t.getTermType());
    this.setTerm(t.getTerm());
    this.setFormPhrases(t.getFormPhrases());
    this.setGrouptags(t.getGrouptags());
    this.setGlossaryText(t.getGlossaryText());
    this.setScopetag(t.getScopetag());
    this.setStatus(t.getStatus());
    this.setCreated(t.getCreated());
    this.setUpdated(t.getUpdated());
    this.setVsntag(t.getVsntag());
    this.setCommit(t.getCommit());
    this.setContributors(t.getContributors());
    this.setLocator(t.getFilename());
    this.setNavurl(t.getNavurl());
    this.setHeadingids(t.getHeadings());
  }

}
