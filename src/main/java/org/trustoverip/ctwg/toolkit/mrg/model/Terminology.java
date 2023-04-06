package org.trustoverip.ctwg.toolkit.mrg.model;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author sih
 */
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Terminology {
  @EqualsAndHashCode.Include private final String scopetag;

  @EqualsAndHashCode.Include private final String scopedir;

  @EqualsAndHashCode.Include private final String curatedir;

  @EqualsAndHashCode.Include private final String vsnTag;

  private String license;
  private List<String> altvsntags;
}
