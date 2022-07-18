package org.trustoverip.ctwg.toolkit.mrg.processors;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;

/**
 * @author sih
 */
public class TermsFilter implements Predicate<Term> {

  private static final String ALL_TAGS = "*";
  private static final String SEPARATOR = ",";
  private final boolean matchAllTags;
  private final String grouptag;

  private TermsFilter(String grouptag) {
    this.grouptag = grouptag;
    matchAllTags = ALL_TAGS.equals(grouptag);
  }

  public static TermsFilter of(String grouptag) {
    return new TermsFilter(grouptag);
  }

  public static TermsFilter all() {
    return new TermsFilter(ALL_TAGS);
  }

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param term the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  @Override
  public boolean test(Term term) {
    if (matchAllTags) {
      return true;
    } else if (StringUtils.isEmpty(term.getGrouptags())) {
      return false;
    } else {
      List<String> grouptags = Arrays.asList(term.getGrouptags().split(SEPARATOR));
      return grouptags.stream().map(StringUtils::trim).toList().contains(grouptag);
    }
  }

  /**
   * Returns a composed predicate that represents a short-circuiting logical AND of this predicate
   * and another. When evaluating the composed predicate, if this predicate is {@code false}, then
   * the {@code other} predicate is not evaluated.
   *
   * <p>Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
   * evaluation of this predicate throws an exception, the {@code other} predicate will not be
   * evaluated.
   *
   * @param other a predicate that will be logically-ANDed with this predicate
   * @return a composed predicate that represents the short-circuiting logical AND of this predicate
   *     and the {@code other} predicate
   * @throws NullPointerException if other is null
   */
  @Override
  public Predicate<Term> and(Predicate<? super Term> other) {
    return Predicate.super.and(other);
  }

  /**
   * Returns a predicate that represents the logical negation of this predicate.
   *
   * @return a predicate that represents the logical negation of this predicate
   */
  @Override
  public Predicate<Term> negate() {
    return Predicate.super.negate();
  }

  /**
   * Returns a composed predicate that represents a short-circuiting logical OR of this predicate
   * and another. When evaluating the composed predicate, if this predicate is {@code true}, then
   * the {@code other} predicate is not evaluated.
   *
   * <p>Any exceptions thrown during evaluation of either predicate are relayed to the caller; if
   * evaluation of this predicate throws an exception, the {@code other} predicate will not be
   * evaluated.
   *
   * @param other a predicate that will be logically-ORed with this predicate
   * @return a composed predicate that represents the short-circuiting logical OR of this predicate
   *     and the {@code other} predicate
   * @throws NullPointerException if other is null
   */
  @Override
  public Predicate<Term> or(Predicate<? super Term> other) {
    return Predicate.super.or(other);
  }
}
