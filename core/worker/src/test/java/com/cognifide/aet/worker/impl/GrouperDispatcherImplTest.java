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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cognifide.aet.communication.api.JobStatus;
import com.cognifide.aet.communication.api.SuiteComparatorsCount;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.ComparatorStepResult;
import com.cognifide.aet.communication.api.metadata.ComparatorStepResult.Status;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GrouperDispatcherImplTest {

  private final GrouperResultData grouperResultData =
      new GrouperResultData(JobStatus.SUCCESS, "grouperType", true, "testName");

  @Mock
  private GrouperJob grouperJob;
  private GrouperJobData grouperJobData;
  private GrouperDispatcherImpl grouperDispatcherImpl;

  @Before
  public void setup() {
    when(grouperJob.group(any())).thenReturn(grouperResultData);
    Map<String, AtomicInteger> comparatorCounts = getComparatorCountMap("grouperJobNotFound", 2);
    Map<String, GrouperJob> grouperJobs = getGrouperJobMap("grouperJobExists", grouperJob);
    grouperDispatcherImpl = new GrouperDispatcherImpl(comparatorCounts, grouperJobs);
  }

  @Test
  public void run_whenGrouperJobExists_expectCallingIt() {
    grouperJobData = getGrouperJobData("grouperJobExists");
    grouperDispatcherImpl.run(null, grouperJobData);
    verify(grouperJob, times(1)).group(grouperJobData);
  }

  @Test
  public void run_whenGrouperJobExists_expectReturningItsResult() {
    grouperJobData = getGrouperJobData("grouperJobExists");
    GrouperResultData grouperResultData = grouperDispatcherImpl.run(null, grouperJobData);
    assertThat(grouperResultData, is(this.grouperResultData));
  }

  @Test
  public void run_whenGrouperJobDoesNotExistAndShouldCountTo2_expectResultNotFinishedOnFirstCall() {
    grouperJobData = getGrouperJobData("grouperJobNotFound");
    GrouperResultData resultData1 = grouperDispatcherImpl.run(null, grouperJobData);
    assertFalse(resultData1.isReady());
  }

  @Test
  public void run_whenGrouperJobDoesNotExistAndShouldCountTo2_expectResultFinishedOnSecondCall() {
    grouperJobData = getGrouperJobData("grouperJobNotFound");
    GrouperResultData resultData1 = grouperDispatcherImpl.run(null, grouperJobData);
    GrouperResultData resultData2 = grouperDispatcherImpl.run(null, grouperJobData);
    assertTrue(resultData2.isReady());
  }

  private static Map<String, AtomicInteger> getComparatorCountMap(
      String comparatorName, int comparatorCount) {
    Map<String, AtomicInteger> comparatorCounts = new HashMap<>();
    comparatorCounts.put(comparatorName, new AtomicInteger(comparatorCount));
    return comparatorCounts;
  }

  private static Map<String, GrouperJob> getGrouperJobMap(
      String comparatorTypeName, GrouperJob grouperJob) {
    Map<String, GrouperJob> grouperJobs = new HashMap<>();
    grouperJobs.put(comparatorTypeName, grouperJob);
    return grouperJobs;
  }

  private static GrouperJobData getGrouperJobData(String comparatorType) {
    return new GrouperJobData(
        "company",
        "project",
        "suiteName",
        "testName",
        SuiteComparatorsCount.of(Lists.newArrayList()),
        new ComparatorStepResult("artifactId", Status.PASSED),
        comparatorType);
  }
}
