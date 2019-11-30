package com.cognifide.aet.runner.processing;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.communication.api.metadata.Step;
import com.cognifide.aet.communication.api.metadata.Suite;
import com.cognifide.aet.communication.api.metadata.Url;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CountGroupingResultsTest {

  @Mock
  private Suite suite;

  private CountGroupingResults f = CountGroupingResults.INSTANCE;

  @Test
  public void apply_whenNoComparators_thenReturnZero() {
    Step step = Step.newBuilder("", 0).build();
    when(suite.getTests()).thenReturn(Lists.newArrayList(getTest(step)));
    assertThat(f.apply(suite), is(0));
  }

  @Test
  public void apply_whenOneComparator_thenReturnOne() {
    Set<Comparator> comparators = getComparators(1);
    Step step = Step.newBuilder("", 0).withComparators(comparators).build();
    when(suite.getTests()).thenReturn(Lists.newArrayList(getTest(step)));
    assertThat(f.apply(suite), is(1));
  }

  @Test
  public void apply_when57Comparators_thenReturn57() {
    Set<Comparator> comparators = getComparators(57);
    Step step = Step.newBuilder("", 0).withComparators(comparators).build();
    when(suite.getTests()).thenReturn(Lists.newArrayList(getTest(step)));
    assertThat(f.apply(suite), is(57));
  }

  @Test
  public void apply_whenOneComparatorInTwoSteps_thenReturnOne() {
    Set<Comparator> comparators = getComparators(1);
    Step step1 = Step.newBuilder("", 0).withComparators(comparators).build();
    Step step2 = Step.newBuilder("", 1).withComparators(comparators).build();
    when(suite.getTests()).thenReturn(Lists.newArrayList(getTest(step1, step2)));
    assertThat(f.apply(suite), is(1));
  }

  @Test
  public void apply_whenOneComparatorInTwoUrls_thenReturnOne() {
    Set<Comparator> comparators = getComparators(1);
    Step step = Step.newBuilder("", 0).withComparators(comparators).build();
    Url url1 = new Url("urlName1", "url1", "");
    Url url2 = new Url("urlName2", "url2", "");
    url1.addStep(step);
    url2.addStep(step);
    when(suite.getTests()).thenReturn(Lists.newArrayList(getTest(url1, url2)));
    assertThat(f.apply(suite), is(1));
  }

  @Test
  public void apply_whenOneComparatorInTwoTests_thenReturnTwo() {
    Set<Comparator> comparators = getComparators(1);
    Step step = Step.newBuilder("", 0).withComparators(comparators).build();
    when(suite.getTests()).thenReturn(Lists.newArrayList(getTest(step), getTest(step)));
    assertThat(f.apply(suite), is(2));
  }

  private static Set<Comparator> getComparators(int amount) {
    Set<Comparator> comparators = new HashSet<>();
    for (int i = 0; i < amount; i++) {
      comparators.add(new Comparator("type" + i));
    }
    return comparators;
  }

  private static com.cognifide.aet.communication.api.metadata.Test getTest(Step... steps) {
    Url url = new Url("urlName", "url", "");
    for (Step step : steps) {
      url.addStep(step);
    }
    com.cognifide.aet.communication.api.metadata.Test test =
        new com.cognifide.aet.communication.api.metadata.Test("test", "", "");
    test.addUrl(url);
    return test;
  }

  private static com.cognifide.aet.communication.api.metadata.Test getTest(Url... urls) {
    com.cognifide.aet.communication.api.metadata.Test test =
        new com.cognifide.aet.communication.api.metadata.Test("test", "", "");
    for (Url url : urls) {
      test.addUrl(url);
    }
    return test;
  }
}
