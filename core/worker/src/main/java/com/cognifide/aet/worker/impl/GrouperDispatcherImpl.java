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

import com.cognifide.aet.communication.api.JobStatus;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

class GrouperDispatcherImpl implements GrouperDispatcher {

  private final Map<Comparator, AtomicLong> comparatorCounts;
  private final Map<Comparator, GrouperJob> grouperJobs;

  GrouperDispatcherImpl(
      Map<Comparator, AtomicLong> comparatorCounts, Map<Comparator, GrouperJob> grouperJobs) {
    this.comparatorCounts = comparatorCounts;
    this.grouperJobs = grouperJobs;
  }

  @Override
  public GrouperResultData run(String correlationId, GrouperJobData grouperJobData) {
    Comparator comparisonResult = grouperJobData.getComparisonResult();
    GrouperResultData result;
    if (!grouperJobs.containsKey(comparisonResult)) {
      long value = comparatorCounts.get(comparisonResult).decrementAndGet();
      result =
          new GrouperResultData(
              JobStatus.SUCCESS,
              comparisonResult.getType(),
              value == 0,
              grouperJobData.getTestName());
    } else {
      GrouperJob grouperJob = grouperJobs.get(comparisonResult);
      result = grouperJob.group(grouperJobData);
    }
    return result;
  }
}
