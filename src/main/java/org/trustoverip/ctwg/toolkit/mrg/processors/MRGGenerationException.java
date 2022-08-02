package org.trustoverip.ctwg.toolkit.mrg.processors;

import java.security.PrivilegedActionException;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sih
 */
@Getter
@Setter
public class MRGGenerationException extends RuntimeException {

  public static final String COULD_NOT_READ_LOCAL_CONTENT =
      "Could not read local content from path: %s";
  public static final String NOT_FOUND =
      "%s: There is no such resource or anonymous access to this repository is not allowed.";
  public static final String UNABLE_TO_PARSE_SAF =
      "Generation failed: Unable to parse SAF. Check that it is valid YAML";

  public static final String UNABLE_TO_PARSE_MRG =
      "Generation failed: Unable to parse remote MRG. Check that it is valid YAML";
  public static final String NO_GLOSSARY_DIR =
      "Generation failed: The glossarydir attribute in the SAF scope is empty so no location to save MRG.";
  public static final String NO_SUCH_VERSION =
      "Generation failed: No version with version tag (vsntag) of %s found in SAF";
  public static final String CANNOT_WRITE_MRG =
      "Generation failed: Unable to write MRG to location %s";
  public static final String CANNOT_PARSE_TERM = "Could not create term from input string of:\n%s";

  public static final String CANNOT_CREATE_GLOSSARY_DIR = "Could not create glossary dir at %s";

  public static final String GITHUB_LOGON_ERROR =
      "Could not log on to GitHub for user %s. Check you are running with valid credentials. You will need a personal access token";

  /**
   * Constructs a new exception with {@code null} as its detail message. The cause is not
   * initialized, and may subsequently be initialized by a call to {@link #initCause}.
   */
  public MRGGenerationException() {
    super();
  }

  /**
   * Constructs a new exception with the specified detail message. The cause is not initialized, and
   * may subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   */
  public MRGGenerationException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * <p>Note that the detail message associated with {@code cause} is <i>not</i> automatically
   * incorporated in this exception's detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   *     #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   * @since 1.4
   */
  public MRGGenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause and a detail message of {@code (cause==null
   * ? null : cause.toString())} (which typically contains the class and detail message of {@code
   * cause}). This constructor is useful for exceptions that are little more than wrappers for other
   * throwables (for example, {@link PrivilegedActionException}).
   *
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   * @since 1.4
   */
  public MRGGenerationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new exception with the specified detail message, cause, suppression enabled or
   * disabled, and writable stack trace enabled or disabled.
   *
   * @param message the detail message.
   * @param cause the cause. (A {@code null} value is permitted, and indicates that the cause is
   *     nonexistent or unknown.)
   * @param enableSuppression whether or not suppression is enabled or disabled
   * @param writableStackTrace whether or not the stack trace should be writable
   * @since 1.7
   */
  protected MRGGenerationException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
