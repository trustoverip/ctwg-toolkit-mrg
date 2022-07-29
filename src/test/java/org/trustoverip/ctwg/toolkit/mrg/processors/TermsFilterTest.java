package org.trustoverip.ctwg.toolkit.mrg.processors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.trustoverip.ctwg.toolkit.mrg.model.MRGEntry;
import org.trustoverip.ctwg.toolkit.mrg.model.Term;
import org.trustoverip.ctwg.toolkit.mrg.processors.TermsFilter.TermFilterType;

/**
 * @author sih
 */
class TermsFilterTest {

  private Term foo;
  private Term bar;
  private Term foobar;
  private Term noo;

  private MRGEntry mrgFoo;

  private MRGEntry mrgBar;
  private MRGEntry mrgFoobar;
  @BeforeEach
  void setUp() {
    foo = new Term();
    foo.setTermid("foo");
    foo.setGrouptags("foo");
    bar = new Term();
    bar.setTermid("bar");
    bar.setGrouptags("bar");
    foobar = new Term();
    foobar.setTermid("foobar");
    foobar.setGrouptags("foo, bar");
    noo = new Term();
    noo.setTermid("noo");
    noo.setGrouptags(null);
    mrgFoo = new MRGEntry(foo);
    mrgBar = new MRGEntry(bar);
    mrgFoobar = new MRGEntry(foobar);
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
  void testFilterTags() {
    TermsFilter fooFilter = TermsFilter.of(TermFilterType.GROUPTAG, "foo");
    assertThat(fooFilter.test(foo)).isTrue();
    assertThat(fooFilter.test(bar)).isFalse();
    assertThat(fooFilter.test(foobar)).isTrue();
    assertThat(fooFilter.test(noo)).isFalse();

    TermsFilter barFilter = TermsFilter.of(TermFilterType.GROUPTAG, "bar");
    assertThat(barFilter.test(foo)).isFalse();
    assertThat(barFilter.test(bar)).isTrue();
    assertThat(barFilter.test(foobar)).isTrue();
    assertThat(barFilter.test(noo)).isFalse();

    TermsFilter emptyFilter = TermsFilter.of(TermFilterType.GROUPTAG, "");
    assertThat(emptyFilter.test(foo)).isFalse();
    assertThat(emptyFilter.test(bar)).isFalse();
    assertThat(emptyFilter.test(foobar)).isFalse();
    assertThat(emptyFilter.test(noo)).isFalse();

    TermsFilter nonMatchingFilter = TermsFilter.of(TermFilterType.GROUPTAG, "moo");
    assertThat(nonMatchingFilter.test(foo)).isFalse();
    assertThat(nonMatchingFilter.test(bar)).isFalse();
    assertThat(nonMatchingFilter.test(foobar)).isFalse();
    assertThat(nonMatchingFilter.test(noo)).isFalse();
  }


  @Test
  @DisplayName("""
      Given a filter value that is not all
      When filter on mrg entries
      Then should return true where grouptags match
      """)
  void testFilterTagsMrg() {
    TermsFilter fooFilter = TermsFilter.of(TermFilterType.GROUPTAG, "foo");
    assertThat(fooFilter.test(mrgFoo)).isTrue();
    assertThat(fooFilter.test(mrgBar)).isFalse();
    assertThat(fooFilter.test(mrgFoobar)).isTrue();
  }

  @Test
  @DisplayName("""
      Given a filter value that is not all
      When filter
      Then should return true where termids match
      """)
  void testFilterTerms() {
    TermsFilter fooFilter = TermsFilter.of(TermFilterType.TERM, "foo");
    assertThat(fooFilter.test(foo)).isTrue();
    assertThat(fooFilter.test(bar)).isFalse();
    assertThat(fooFilter.test(foobar)).isFalse();
    assertThat(fooFilter.test(noo)).isFalse();

    TermsFilter nonMatchingFilter = TermsFilter.of(TermFilterType.TERM, "moo");
    assertThat(nonMatchingFilter.test(foo)).isFalse();
    assertThat(nonMatchingFilter.test(bar)).isFalse();
    assertThat(nonMatchingFilter.test(foobar)).isFalse();
    assertThat(nonMatchingFilter.test(noo)).isFalse();

    TermsFilter multiMatchFilter = TermsFilter.of(TermFilterType.TERM, "foo, bar");
    assertThat(multiMatchFilter.test(foo)).isTrue();
    assertThat(multiMatchFilter.test(bar)).isTrue();
    assertThat(multiMatchFilter.test(foobar)).isFalse();
    assertThat(multiMatchFilter.test(noo)).isFalse();
  }

  @Test
  @DisplayName("""
      Given a filter value that is not all
      When filter on mrg entries
      Then should return true where termids match
      """)
  void testFilterTermsMrg() {
    TermsFilter fooFilter = TermsFilter.of(TermFilterType.TERM, "foo");
    assertThat(fooFilter.test(mrgFoo)).isTrue();
    assertThat(fooFilter.test(mrgBar)).isFalse();
    assertThat(fooFilter.test(mrgFoobar)).isFalse();
  }

  @DisplayName("""
      Given lists
      When filter
      Then should filter to leave appropriate items
      """)
  @Test
  void testListTags() {
    List<Term> all = List.of(foo, bar, foobar, noo);
    List<Term> onlyFoos = all.stream().filter(TermsFilter.of(TermFilterType.GROUPTAG, "foo")).collect(Collectors.toList());
    assertThat(onlyFoos).containsExactlyInAnyOrder(foo, foobar);
    List<Term> allMatches = all.stream().filter(TermsFilter.of(TermFilterType.GROUPTAG, "foo, bar ")).collect(
        Collectors.toList());
    assertThat(allMatches).containsExactlyInAnyOrder(foo, bar, foobar);
  }

  @DisplayName("""
      Given a list of all items
      When filters chained with and
      Then should filter to leave appropriate items
      """)
  @Test
  void testListAnd() {
    List<Term> all = List.of(foo, bar, foobar, noo);
    List<Term> mustHaveBoth = all.stream().filter(TermsFilter.of(TermFilterType.GROUPTAG, "foo").and(TermsFilter.of(TermFilterType.GROUPTAG, "bar"))).collect(Collectors.toList());
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
    List<Term> mustHaveBoth = all.stream().filter(TermsFilter.of(TermFilterType.GROUPTAG, "foo").or(TermsFilter.of(TermFilterType.GROUPTAG, "bar"))).collect(Collectors.toList());
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
    List<Term> mustHaveNeither = all.stream().filter(TermsFilter.of(TermFilterType.GROUPTAG, "foo").negate()).collect(Collectors.toList());
    assertThat(mustHaveNeither).containsExactlyInAnyOrder(bar, noo);
    mustHaveNeither = all.stream().filter(TermsFilter.of(TermFilterType.GROUPTAG, "bar, foo").negate()).collect(Collectors.toList());
    assertThat(mustHaveNeither).containsExactlyInAnyOrder(noo);
    // now test with terms
    mustHaveNeither = all.stream().filter(TermsFilter.of(TermFilterType.TERM, "bar, foo").negate()).collect(Collectors.toList());
    assertThat(mustHaveNeither).containsExactlyInAnyOrder(noo, foobar);
    mustHaveNeither = all.stream().filter(TermsFilter.of(TermFilterType.TERM, "bar, foo, foobar , noo").negate()).collect(Collectors.toList());
    assertThat(mustHaveNeither).isEmpty();
  }

  @Test
  @DisplayName("""
      Given a list of tags
      When converting to predicates and reducing using or
      Then can apply resulting predicate to filter a list to the correct terms
      """)
  void testFromTagList() {
    List<Term> all = List.of(foo, bar, foobar, noo);
    List<String> tagsOfInterest = List.of("foo", "bar");
    Predicate<Term> predicate = tagsOfInterest.stream().map(tag -> (Predicate<Term>)TermsFilter.of(TermFilterType.GROUPTAG, tag)).reduce(Predicate::or).get();
    List<Term> filtered = all.stream().filter(predicate).collect(Collectors.toList());
    assertThat(filtered).containsExactlyInAnyOrder(foo, bar, foobar);
  }

}