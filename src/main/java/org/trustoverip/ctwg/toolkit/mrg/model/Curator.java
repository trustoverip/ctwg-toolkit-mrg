package org.trustoverip.ctwg.toolkit.mrg.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author sih
 */
public final class Curator {
  private final String name;
  private final Email email;

  @JsonCreator
  public Curator(@JsonProperty("name") String name, @JsonProperty("email") Email email) {
    this.name = name;
    this.email = email;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Curator curator = (Curator) o;

    if (!name.equals(curator.name)) {
      return false;
    }
    return email.equals(curator.email);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + email.hashCode();
    return result;
  }

  public String toString() {
    return String.join(" ", name, email.toString());
  }
}
