package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;

/**
 * @author sih
 */
class TermsFilterTest {

  private Term foo;
  private Term bar;
  private Term foobar;
  private Term noo;

  @BeforeEach
  void setUp() {
    foo = new Term();
    foo.setGrouptags("foo");
    bar = new Term();
    bar.setGrouptags("bar");
    foobar = new Term();
    foobar.setGrouptags("foo, bar");
    noo = new Term();
    noo.setGrouptags(null);
  }

  @Test
  @DisplayName("""
      Given a filter value of all
      When filter
      Then should return true
      """)
  void testAll() {
    TermsFilter all = TermsFilter.all();
    assertThat(all.test(foo)).isTrue();
    assertThat(all.test(bar)).isTrue();
    assertThat(all.test(foobar)).isTrue();
    assertThat(all.test(noo)).isTrue();
  }


  @Test
  @DisplayName("""
      Given a filter value that is not all
      When filter
      Then should return true where grouptags match
      """)
  void testFilter() {
    TermsFilter fooFilter = TermsFilter.of("foo");
    assertThat(fooFilter.test(foo)).isTrue();
    assertThat(fooFilter.test(bar)).isFalse();
    assertThat(fooFilter.test(foobar)).isTrue();
    assertThat(fooFilter.test(noo)).isFalse();

    TermsFilter barFilter = TermsFilter.of("bar");
    assertThat(barFilter.test(foo)).isFalse();
    assertThat(barFilter.test(bar)).isTrue();
    assertThat(barFilter.test(foobar)).isTrue();
    assertThat(barFilter.test(noo)).isFalse();

    TermsFilter emptyFilter = TermsFilter.of("");
    assertThat(emptyFilter.test(foo)).isFalse();
    assertThat(emptyFilter.test(bar)).isFalse();
    assertThat(emptyFilter.test(foobar)).isFalse();
    assertThat(emptyFilter.test(noo)).isFalse();

    TermsFilter nonMatchingFilter = TermsFilter.of("moo");
    assertThat(nonMatchingFilter.test(foo)).isFalse();
    assertThat(nonMatchingFilter.test(bar)).isFalse();
    assertThat(nonMatchingFilter.test(foobar)).isFalse();
    assertThat(nonMatchingFilter.test(noo)).isFalse();
  }

  @DisplayName("""
      Given lists
      When filter
      Then should filter to leave appropriate items
      """)
  @Test
  void testLists() {
    List<Term> all = List.of(foo, bar, foobar, noo);
    List<Term> onlyFoos = all.stream().filter(TermsFilter.of("foo")).collect(Collectors.toList());
    assertThat(onlyFoos).containsExactlyInAnyOrder(foo, foobar);
  }

  @DisplayName("""
      Given a list of all items
      When filters chained with and
      Then should filter to leave appropriate items
      """)
  @Test
  void testListAnd() {
    List<Term> all = List.of(foo, bar, foobar, noo);
    List<Term> mustHaveBoth = all.stream().filter(TermsFilter.of("foo").and(TermsFilter.of("bar"))).collect(Collectors.toList());
    assertThat(mustHaveBoth).containsExactly(foobar);
  }

  @DisplayName("""
      Given a list of all items
      When filters chained with or
      Then should filter to leave appropriate items
      """)
  @Test
  void testListOr() {
    List<Term> all = List.of(foo, bar, foobar, noo);
    List<Term> mustHaveBoth = all.stream().filter(TermsFilter.of("foo").or(TermsFilter.of("bar"))).collect(Collectors.toList());
    assertThat(mustHaveBoth).containsExactlyInAnyOrder(foo, bar, foobar);
  }


  @DisplayName("""
      Given a list of all items
      When not filter
      Then should filter to leave appropriate items
      """)
  @Test
  void testListNegate() {
    List<Term> all = List.of(foo, bar, foobar, noo);
    List<Term> mustHaveBoth = all.stream().filter(TermsFilter.of("foo").negate()).collect(Collectors.toList());
    assertThat(mustHaveBoth).containsExactlyInAnyOrder( bar, noo);
  }

}