package org.trustoverip.ctwg.toolkit.mrg.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;

/**
 * @author sih
 */
public class TermsFilter implements Predicate<Term> {

  public static final String ALL_TAGS = "*";
  private static final String DELIMITER = ",";

  private final List<String> normalisedValues;

  private final TermFilterType filterType;


  private TermsFilter(TermFilterType filterType, String delimitedValues) {
    this.filterType = filterType;
    if (delimitedValues != null) {
      normalisedValues = splitAndNormalise(delimitedValues);
    } else {
      normalisedValues = new ArrayList<>();
    }
  }

  public static TermsFilter of(TermFilterType filterType, String delimitedValues) {
    return new TermsFilter(filterType, delimitedValues);
  }

  public static TermsFilter all() {
    return new TermsFilter(TermFilterType.ALL, null);
  }

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param term the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  @Override
  public boolean test(Term term) {
    return switch (this.filterType) {
      case ALL -> true;
      case TERM -> {
        String normalisedTermid = StringUtils.trim(term.getTermid()).toLowerCase(Locale.ROOT);
        yield normalisedValues.contains(normalisedTermid);
      }
      case GROUPTAG -> {
        List<String> normalisedGrouptags = splitAndNormalise(term.getGrouptags());
        if (normalisedGrouptags.isEmpty()) { yield false;}
        else {yield normalisedGrouptags.stream().anyMatch(normalisedValues::contains);}
      }
    };
  }


  private List<String> splitAndNormalise(String delimitedValues) {
    String[] vals = (null == delimitedValues) ? new String[0] : delimitedValues.split(DELIMITER);
    return switch (vals.length) {
      case 0 -> new ArrayList<>();
      case 1 -> List.of(StringUtils.trim(vals[0]).toLowerCase(Locale.ROOT));
      default -> Arrays.stream(vals).map(StringUtils::trim).map(StringUtils::toRootLowerCase).toList();
    };
 }

  enum TermFilterType {
    GROUPTAG,
    TERM,
    ALL
  }
}
