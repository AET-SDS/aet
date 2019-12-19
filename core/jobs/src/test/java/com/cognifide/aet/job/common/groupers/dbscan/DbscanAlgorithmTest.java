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

package com.cognifide.aet.job.common.groupers.dbscan;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cognifide.aet.job.common.groupers.DistanceFunction;
import com.cognifide.aet.job.common.groupers.GroupingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DbscanAlgorithmTest<T> {

  @Mock
  private DistanceFunction<T> distanceFunction;
  private DbscanConfiguration<T> config;
  private DbscanAlgorithm<T> algorithm;

  @Test(expected = NullPointerException.class)
  public void group_whenConfigurationIsNull_expectException() throws GroupingException {
    config = null;
    algorithm = new DbscanAlgorithm<>(config);

    algorithm.group(Collections.emptySet());
  }

  @Test(expected = NullPointerException.class)
  public void group_whenDistanceFunctionIsNull_expectException() throws GroupingException {
    config = new DbscanConfiguration<>(0.1, 1, null);
    algorithm = new DbscanAlgorithm<>(config);

    algorithm.group(createElementsToGroup(1));
  }

  @Test
  public void group_whenThresholdIsLessThanZero_expectException() {
    config = new DbscanConfiguration<>(-1.0, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    try {
      algorithm.group(Collections.emptySet());
      fail("GroupingException expected");
    } catch (GroupingException e) {
      assertThat(e.getMessage(), is("Threshold cannot be less than 0 or more than 1"));
    }
  }

  @Test
  public void group_whenThresholdIsMoreThanOne_expectException() {
    config = new DbscanConfiguration<>(1.1, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    try {
      algorithm.group(Collections.emptySet());
      fail("GroupingException expected");
    } catch (GroupingException e) {
      assertThat(e.getMessage(), is("Threshold cannot be less than 0 or more than 1"));
    }
  }

  @Test
  public void group_whenElementsToGroupAreEmpty_expectEmptySet() throws GroupingException {
    config = new DbscanConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(Collections.emptySet());
    assertTrue(result.isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void group_whenElementsToGroupIsNull_expectException() throws GroupingException {
    config = new DbscanConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    algorithm.group(null);
  }

  @Test
  public void group_when2ElementsAreTheSame_expectSetOfSize1() throws GroupingException {
    when(distanceFunction.apply(any(), any())).thenReturn((double) 0);
    config = new DbscanConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(createElementsToGroup(2));
    assertThat(result.size(), is(1));
  }

  @Test
  public void group_when2ElementsAreCompletelyDifferent_expectSetOfSize2()
      throws GroupingException {
    prepareMockReturnValues(1);
    config = new DbscanConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(createElementsToGroup(2));
    assertThat(result.size(), is(2));
  }

  @Test
  public void group_whenElementsHaveSimilarityGreaterThanThreshold_expectSetOfSize2()
      throws GroupingException {
    prepareMockReturnValues(0.2);
    double threshold = 0.1;
    config = new DbscanConfiguration<>(threshold, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(createElementsToGroup(2));
    assertThat(result.size(), is(2));
  }

  @Test
  public void group_whenElementsHaveSimilarityLessThanOrEqualThanThreshold_expectSetOfSize1()
      throws GroupingException {
    prepareMockReturnValues(0.05);
    double threshold = 0.1;
    config = new DbscanConfiguration<>(threshold, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(createElementsToGroup(2));
    assertThat(result.size(), is(1));
  }

  @Test
  public void group_whenMinGroupSizeIsGreaterThan1AndElementsAreDifferent_expectSetOfSize0()
      throws GroupingException {
    prepareMockReturnValues(1);
    int minGroupSize = 2;
    config = new DbscanConfiguration<>(0.1, minGroupSize, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(createElementsToGroup(2));
    assertThat(result.size(), is(0));
  }

  @Test
  public void group_whenMinGroupSizeIsGreaterThan1AndElementsAreTheSame_expectSetOfSize1()
      throws GroupingException {
    prepareMockReturnValues(0.05);
    int minGroupSize = 2;
    config = new DbscanConfiguration<>(0.1, minGroupSize, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(createElementsToGroup(2));
    assertThat(result.size(), is(1));
  }

  @Test
  public void group_whenThereAre2Elements_expect4FunctionCalls() throws GroupingException {
    prepareMockReturnValues(1);
    config = new DbscanConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    algorithm.group(createElementsToGroup(2));
    verify(distanceFunction, times(4)).apply(any(), any());
  }

  @Test
  public void group_whenThereAre5Elements_expect25FunctionCalls() throws GroupingException {
    prepareMockReturnValues(1);
    config = new DbscanConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DbscanAlgorithm<>(config);

    algorithm.group(createElementsToGroup(5));
    verify(distanceFunction, times(25)).apply(any(), any());
  }

  private void prepareMockReturnValues(final double similarity) {
    when(distanceFunction.apply(any(), any()))
        .thenAnswer(
            invocation -> {
              Object arg1 = invocation.getArguments()[0];
              Object arg2 = invocation.getArguments()[1];
              if (arg1.equals(arg2)) {
                return (double) 0;
              } else {
                return similarity;
              }
            });
  }

  private List<T> createElementsToGroup(int amount) {
    List<T> list = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      list.add((T) new Object());
    }
    return list;
  }
}
