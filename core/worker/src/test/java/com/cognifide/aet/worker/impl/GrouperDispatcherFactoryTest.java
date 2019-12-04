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

package com.cognifide.aet.worker.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.cognifide.aet.communication.api.SuiteComparatorsCount;
import com.cognifide.aet.communication.api.SuiteTestIdentifier;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.communication.api.metadata.Step;
import com.cognifide.aet.communication.api.metadata.Url;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;

public class GrouperDispatcherFactoryTest {

  private final SuiteTestIdentifier id = new SuiteTestIdentifier("correlationId", "testName");
  private final Supplier<Map<String, GrouperJob>> grouperJobs = Maps::newHashMap;
  private SuiteComparatorsCount comparatorsCount;
  private GrouperDispatcherFactory grouperDispatcherFactory;

  @Before
  public void setup() {
    grouperDispatcherFactory = new GrouperDispatcherFactory();
  }

  @Test(expected = IllegalArgumentException.class)
  public void getDispatcher_whenNoComparatorsFound_expectException() {
    comparatorsCount = getComparatorsCount(0);
    grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
  }

  @Test
  public void getDispatcher_whenAnyComparatorFound_expectDispatcherReturned() {
    comparatorsCount = getComparatorsCount(1);
    GrouperDispatcher dispatcher =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    assertThat(dispatcher, notNullValue());
  }

  @Test
  public void getDispatcher_whenAlreadyRemoved_expectAnotherInstance() {
    comparatorsCount = getComparatorsCount(1);
    GrouperDispatcher dispatcher1 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    grouperDispatcherFactory.forceRemove(id);
    GrouperDispatcher dispatcher2 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    assertThat(dispatcher1, not(dispatcher2));
  }

  @Test
  public void getDispatcher_whenNotRemoved_expectTheSameInstance() {
    comparatorsCount = getComparatorsCount(1);
    GrouperDispatcher dispatcher1 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    GrouperDispatcher dispatcher2 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    assertThat(dispatcher1, is(dispatcher2));
  }

  @Test(expected = NullPointerException.class)
  public void tick_whenTooManyTicks_expectException() {
    comparatorsCount = getComparatorsCount(1);
    grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    grouperDispatcherFactory.tick(id);
    grouperDispatcherFactory.tick(id);
  }

  @Test
  public void tick_whenCalledMultipleTimes_expectRemainingLifespanDecrease() {
    comparatorsCount = getComparatorsCount(5);
    grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    int val1 = grouperDispatcherFactory.tick(id);
    int val2 = grouperDispatcherFactory.tick(id);
    int val3 = grouperDispatcherFactory.tick(id);
    assertThat(val1 - val2, is(1));
    assertThat(val2 - val3, is(1));
  }

  @Test
  public void tick_whenCountedDownToZero_expectDispatcherRemoved() {
    comparatorsCount = getComparatorsCount(1);
    GrouperDispatcher dispatcher1 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    int tick = grouperDispatcherFactory.tick(id);
    GrouperDispatcher dispatcher2 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    assertThat(tick, is(0));
    assertThat(dispatcher1, not(dispatcher2));
  }

  @Test
  public void tick_whenDidNotCountDownToZero_expectDispatcherNotRemoved() {
    comparatorsCount = getComparatorsCount(2);
    GrouperDispatcher dispatcher1 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    int tick = grouperDispatcherFactory.tick(id);
    GrouperDispatcher dispatcher2 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    assertThat(tick, not(0));
    assertThat(dispatcher1, is(dispatcher2));
  }

  @Test
  public void forceRemove_whenEntryExistsForGivenId_expectDispatcherRemoved() {
    comparatorsCount = getComparatorsCount(1);
    GrouperDispatcher dispatcher1 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    grouperDispatcherFactory.forceRemove(id);
    GrouperDispatcher dispatcher2 =
        grouperDispatcherFactory.getDispatcher(id, comparatorsCount, grouperJobs);
    assertThat(dispatcher1, not(dispatcher2));
  }

  @Test
  public void forceRemove_whenEntryDoesNotExistForGivenId_expectNoException() {
    comparatorsCount = getComparatorsCount(1);
    grouperDispatcherFactory.forceRemove(id);
  }

  private SuiteComparatorsCount getComparatorsCount(int distinctComparators) {
    return SuiteComparatorsCount.of(Lists.newArrayList(getTest("testName", distinctComparators)));
  }

  private com.cognifide.aet.communication.api.metadata.Test getTest(
      String testName, int distinctComparators) {
    com.cognifide.aet.communication.api.metadata.Test test =
        new com.cognifide.aet.communication.api.metadata.Test(testName, "", "");
    Url url = new Url("urlName", "", "");
    Set<Comparator> comparators = Sets.newHashSet();
    for (int i = 0; i < distinctComparators; i++) {
      comparators.add(new Comparator("comparatorType" + i));
    }
    url.addStep(Step.newBuilder("type", 0).withComparators(comparators).build());
    test.addUrl(url);
    return test;
  }
}
