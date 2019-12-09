/*
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cognifide.aet.communication.api;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.communication.api.metadata.Step;
import com.cognifide.aet.communication.api.metadata.Url;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class SuiteComparatorsCountTest {

  private List<com.cognifide.aet.communication.api.metadata.Test> tests;
  private SuiteComparatorsCount suiteComparatorsCount;

  @Test(expected = NullPointerException.class)
  public void getDistinctComparatorsCountForTest_whenNotFound_expectException() {
    tests = Lists.newArrayList();
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    suiteComparatorsCount.getDistinctComparatorsCountForTest("notFound");
  }

  @Test
  public void getDistinctComparatorsCountForTest_whenOneTypeInOneStep_expectOne() {
    Set<Comparator> comparators = getComparators("type", 1);
    Step step = Step.newBuilder("", 0).withComparators(comparators).build();
    tests = Lists.newArrayList(getTest("testName", step));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    int count = suiteComparatorsCount.getDistinctComparatorsCountForTest("testName");
    assertThat(count, is(1));
  }

  @Test
  public void getDistinctComparatorsCountForTest_whenOneTypeInMultipleSteps_expectOne() {
    Set<Comparator> comparators = getComparators("type", 1);
    Step step1 = Step.newBuilder("", 0).withComparators(comparators).build();
    Step step2 = Step.newBuilder("", 1).withComparators(comparators).build();
    Step step3 = Step.newBuilder("", 2).withComparators(comparators).build();
    tests = Lists.newArrayList(getTest("testName", step1, step2, step3));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    int count = suiteComparatorsCount.getDistinctComparatorsCountForTest("testName");
    assertThat(count, is(1));
  }

  @Test
  public void getDistinctComparatorsCountForTest_whenTwoTypesInOneStep_expectTwo() {
    Set<Comparator> comparators = getComparators("type", 2);
    Step step = Step.newBuilder("", 0).withComparators(comparators).build();
    tests = Lists.newArrayList(getTest("testName", step));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    int count = suiteComparatorsCount.getDistinctComparatorsCountForTest("testName");
    assertThat(count, is(2));
  }

  @Test
  public void getDistinctComparatorsCountForTest_whenTwoTypesInTwoSteps_expectTwo() {
    Set<Comparator> comparators1 = getComparators("type1", 1);
    Set<Comparator> comparators2 = getComparators("type2", 1);
    Step step1 = Step.newBuilder("", 0).withComparators(comparators1).build();
    Step step2 = Step.newBuilder("", 1).withComparators(comparators2).build();
    tests = Lists.newArrayList(getTest("testName", step1, step2));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    int count = suiteComparatorsCount.getDistinctComparatorsCountForTest("testName");
    assertThat(count, is(2));
  }

  @Test
  public void getDistinctComparatorsCountForTest_when97TypesInDifferentSteps_expect97() {
    Set<Comparator> comparators = getComparators("type", 97);
    Step step1 = Step.newBuilder("", 0).withComparators(comparators).build();
    Step step2 = Step.newBuilder("", 1).withComparators(comparators).build();
    Step step3 = Step.newBuilder("", 2).withComparators(comparators).build();
    Step step4 = Step.newBuilder("", 3).withComparators(comparators).build();
    Step step5 = Step.newBuilder("", 4).withComparators(comparators).build();
    tests = Lists.newArrayList(getTest("testName", step1, step2, step3, step4, step5));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    int count = suiteComparatorsCount.getDistinctComparatorsCountForTest("testName");
    assertThat(count, is(97));
  }

  @Test(expected = NullPointerException.class)
  public void getAllComparatorCountsForTest_whenTestNameNotFound_expectException() {
    tests = Lists.newArrayList();
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    suiteComparatorsCount.getAllComparatorCountsForTest("notFound");
  }

  @Test
  public void getAllComparatorCountsForTest_whenNoComparators_expectEmptyCollection() {
    Step step = Step.newBuilder("", 0).build();
    tests = Lists.newArrayList(getTest("testName", step));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    Set<Pair<Comparator, Integer>> allComparatorCounts =
        suiteComparatorsCount.getAllComparatorCountsForTest("testName");
    assertThat(allComparatorCounts.size(), is(0));
  }

  @Test
  public void getAllComparatorCountsForTest_when5DifferentTypes_expectCollectionOfSize5() {
    Set<Comparator> comparators = getComparators("type", 5);
    Step step = Step.newBuilder("", 0).withComparators(comparators).build();
    tests = Lists.newArrayList(getTest("testName", step));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    Set<Pair<Comparator, Integer>> allComparatorCounts =
        suiteComparatorsCount.getAllComparatorCountsForTest("testName");
    assertThat(allComparatorCounts.size(), is(5));
  }

  @Test
  public void getAllComparatorCountsForTest_whenManyComparatorsOfTheSameType_expectCorrectCounts() {
    Set<Comparator> comparators1 = getComparators("type", 1);
    Set<Comparator> comparators2 = getComparators("type", 2);
    Set<Comparator> comparators3 = getComparators("type", 3);
    Step step1 = Step.newBuilder("", 0).withComparators(comparators1).build();
    Step step2 = Step.newBuilder("", 1).withComparators(comparators2).build();
    Step step3 = Step.newBuilder("", 2).withComparators(comparators3).build();
    tests = Lists.newArrayList(getTest("testName", step1, step2, step3));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    Set<Pair<Comparator, Integer>> allComparatorCounts =
        suiteComparatorsCount.getAllComparatorCountsForTest("testName");
    assertTrue(allComparatorCounts.contains(Pair.of(new Comparator("type0"), 3)));
    assertTrue(allComparatorCounts.contains(Pair.of(new Comparator("type1"), 2)));
    assertTrue(allComparatorCounts.contains(Pair.of(new Comparator("type2"), 1)));
  }

  @Test(expected = NullPointerException.class)
  public void prepareCountdownsByComparatorTypes_whenTestNameNotFound_expectException() {
    tests = Lists.newArrayList();
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    suiteComparatorsCount.prepareCountdownsByComparatorTypes("notFound");
  }

  @Test
  public void prepareCountdownsByComparatorTypes_whenNoComparators_expectEmptyMap() {
    Step step = Step.newBuilder("step", 0).build();
    tests = Lists.newArrayList(getTest("testName", step));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    Map<String, AtomicInteger> countdowns =
        suiteComparatorsCount.prepareCountdownsByComparatorTypes("testName");
    assertTrue(countdowns.isEmpty());
  }

  @Test
  public void prepareCountdownsByComparatorTypes_whenOneComparator_expectCountdownEqualOne() {
    Set<Comparator> comparators = getComparators("type", 1);
    Step step = Step.newBuilder("step", 0).withComparators(comparators).build();
    tests = Lists.newArrayList(getTest("testName", step));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    Map<String, AtomicInteger> countdowns =
        suiteComparatorsCount.prepareCountdownsByComparatorTypes("testName");
    assertThat(countdowns.get("type0").get(), is(1));
  }

  @Test
  public void prepareCountdownsByComparatorTypes_whenManyComparators_expectCorrectCounts() {
    Set<Comparator> comparators1 = getComparators("type", 1);
    Set<Comparator> comparators2 = getComparators("type", 2);
    Set<Comparator> comparators3 = getComparators("type", 3);
    Step step1 = Step.newBuilder("step", 0).withComparators(comparators1).build();
    Step step2 = Step.newBuilder("step", 1).withComparators(comparators2).build();
    Step step3 = Step.newBuilder("step", 2).withComparators(comparators3).build();
    tests = Lists.newArrayList(getTest("testName", step1, step2, step3));
    suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    Map<String, AtomicInteger> countdowns =
        suiteComparatorsCount.prepareCountdownsByComparatorTypes("testName");
    assertThat(countdowns.get("type0").get(), is(3));
    assertThat(countdowns.get("type1").get(), is(2));
    assertThat(countdowns.get("type2").get(), is(1));
  }

  private static Set<Comparator> getComparators(String type, int amount) {
    Set<Comparator> comparators = new HashSet<>();
    for (int i = 0; i < amount; i++) {
      comparators.add(new Comparator(type + i));
    }
    return comparators;
  }

  private static com.cognifide.aet.communication.api.metadata.Test getTest(
      String name, Step... steps) {
    Url url = new Url("urlName", "url", "");
    for (Step step : steps) {
      url.addStep(step);
    }
    com.cognifide.aet.communication.api.metadata.Test test =
        new com.cognifide.aet.communication.api.metadata.Test(name, "", "");
    test.addUrl(url);
    return test;
  }
}
