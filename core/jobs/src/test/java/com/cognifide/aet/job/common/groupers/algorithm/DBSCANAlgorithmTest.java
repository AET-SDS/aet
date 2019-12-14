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

package com.cognifide.aet.job.common.groupers.algorithm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cognifide.aet.job.common.groupers.DistanceFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DBSCANAlgorithmTest<T> {

  @Mock
  private DistanceFunction<T> distanceFunction;
  private DBSCANConfiguration<T> config;

  private DBSCANAlgorithm<T> algorithm;

  @Test(expected = NullPointerException.class)
  public void group_whenConfigurationIsNull_expectException() throws GroupingException {
    config = null;
    algorithm = new DBSCANAlgorithm<>(config);

    algorithm.group(Collections.emptySet());
  }

  @Test(expected = NullPointerException.class)
  public void group_whenDistanceFunctionIsNull_expectException() throws GroupingException {
    config = new DBSCANConfiguration<>(0.1, 1, null);
    algorithm = new DBSCANAlgorithm<>(config);

    algorithm.group(Collections.singletonList((T) new Object()));
  }

  @Test
  public void group_whenThresholdIsLessThanZero_expectException() {
    config = new DBSCANConfiguration<>(-1.0, 1, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    try {
      algorithm.group(Collections.emptySet());
      fail("GroupingException expected");
    } catch (GroupingException e) {
      assertThat(e.getMessage(), is("Threshold cannot be less than 0"));
    }
  }

  @Test
  public void group_whenElementsToGroupAreEmpty_expectEmptySet() throws GroupingException {
    config = new DBSCANConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(Collections.emptySet());
    assertThat(result.isEmpty(), is(true));
  }

  @Test(expected = NullPointerException.class)
  public void group_whenElementsToGroupsIsNull_expectException() throws GroupingException {
    config = new DBSCANConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    algorithm.group(null);
  }

  @Test
  public void group_when2ElementsAreTheSame_expectSetOfSize1() throws GroupingException {
    when(distanceFunction.apply(any(), any())).thenReturn((double) 0);
    config = new DBSCANConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(createListOfTwoObjects());
    assertThat(result.size(), is(1));
    verify(distanceFunction, times(4)).apply(any(), any());
  }

  @Test
  public void group_when2ElementsAreCompletelyDifferent_expectSetOfSize2()
      throws GroupingException {
    List<T> list = createListAndPrepareMockito(1);
    config = new DBSCANConfiguration<>(0.1, 1, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(list);
    assertThat(result.size(), is(2));
    verify(distanceFunction, times(4)).apply(any(), any());
  }

  @Test
  public void group_whenElementsHaveSimilarityGreaterThanThreshold_expectSetOfSize2()
      throws GroupingException {
    List<T> list = createListAndPrepareMockito(0.2);
    double threshold = 0.1;
    config = new DBSCANConfiguration<>(threshold, 1, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(list);
    assertThat(result.size(), is(2));
    verify(distanceFunction, times(4)).apply(any(), any());
  }

  @Test
  public void group_whenElementsHaveSimilarityLessThanOrEqualThanThreshold_expectSetOfSize1()
      throws GroupingException {
    List<T> list = createListAndPrepareMockito(0.05);
    double threshold = 0.1;
    config = new DBSCANConfiguration<>(threshold, 1, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(list);
    assertThat(result.size(), is(1));
    verify(distanceFunction, times(4)).apply(any(), any());
  }

  @Test
  public void group_whenMinGroupSizeIsGreaterThan1AndElementsAreDifferent_expectSetOfSize0()
      throws GroupingException {
    List<T> list = createListAndPrepareMockito(1);
    int minGroupSize = 2;
    config = new DBSCANConfiguration<>(0.1, minGroupSize, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(list);
    assertThat(result.size(), is(0));
    verify(distanceFunction, times(4)).apply(any(), any());
  }

  @Test
  public void group_whenMinGroupSizeIsGreaterThan1AndElementsAreTheSame_expectSetOfSize1()
      throws GroupingException {
    List<T> list = createListAndPrepareMockito(0.05);
    int minGroupSize = 2;
    config = new DBSCANConfiguration<>(0.1, minGroupSize, distanceFunction);
    algorithm = new DBSCANAlgorithm<>(config);

    Set<Set<T>> result = algorithm.group(list);
    assertThat(result.size(), is(1));
    verify(distanceFunction, times(4)).apply(any(), any());
  }

  private List<T> createListAndPrepareMockito(double similarity) {
    List<T> list = createListOfTwoObjects();
    T object1 = list.get(0);
    T object2 = list.get(1);

    when(distanceFunction.apply(any(), any())).thenReturn(similarity);
    when(distanceFunction.apply(eq(object1), eq(object1))).thenReturn((double) 0);
    when(distanceFunction.apply(eq(object2), eq(object2))).thenReturn((double) 0);

    return list;
  }

  private List<T> createListOfTwoObjects() {
    return Arrays.asList((T) new Object(), (T) new Object());
  }
}
