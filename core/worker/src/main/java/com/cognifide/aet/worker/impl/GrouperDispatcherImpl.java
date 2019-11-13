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
      result = new GrouperResultData(JobStatus.SUCCESS, value == 0);
    } else {
      GrouperJob grouperJob = grouperJobs.get(comparisonResult);
      result = grouperJob.group(grouperJobData);
    }
    return result;
  }
}
