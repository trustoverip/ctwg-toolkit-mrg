package org.trustoverip.ctwg.toolkit.mrg.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

/**
 * @author sih
 */
@Getter
public final class ScopeRef {
  private final List<String> scopetags;
  private final String scopedir;

  @JsonCreator
  public ScopeRef(
      @JsonProperty("scopetags") List<String> scopetags,
      @JsonProperty("scopedir") String scopedir) {
    this.scopetags = scopetags;
    this.scopedir = scopedir;
  }
}
