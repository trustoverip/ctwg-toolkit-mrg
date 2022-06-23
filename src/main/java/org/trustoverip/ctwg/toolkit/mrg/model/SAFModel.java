package org.trustoverip.ctwg.toolkit.mrg.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sih
 */
@Getter
@Setter
@EqualsAndHashCode
public final class SAFModel {
  private Scope scope;
  private List<ScopeRef> scopes;
  private List<Version> versions;
}
