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
    this.setTermType(t.getTermType());
    this.setTerm(t.getTerm());
    this.setFormPhrases(t.getFormPhrases());
    this.setGroupTags(t.getGroupTags());
    this.setGlossaryText(t.getGlossaryText());
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

}
