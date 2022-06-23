package org.trustoverip.ctwg.toolkit.mrg.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author sih
 */
public class Email {
  private final String id;
  private final String at;

  @JsonCreator
  public Email(@JsonProperty("id") String id, @JsonProperty("at") String at) {
    this.id = id;
    this.at = at;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Email email = (Email) o;

    if (!id.equals(email.id)) {
      return false;
    }
    return at.equals(email.at);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + at.hashCode();
    return result;
  }

  public String toString() {
    return String.format("%s@%s", id, at);
  }
}
