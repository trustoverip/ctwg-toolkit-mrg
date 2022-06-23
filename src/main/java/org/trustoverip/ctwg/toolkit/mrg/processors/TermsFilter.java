package org.trustoverip.ctwg.toolkit.mrg.processors;

import java.util.function.Predicate;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;

/**
 * @author sih
 */
public class TermsFilter implements Predicate<Term> {

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param term the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  @Override
  public boolean test(Term term) {
    return false;
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
