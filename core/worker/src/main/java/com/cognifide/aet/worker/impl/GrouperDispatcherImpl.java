package com.cognifide.aet.worker.impl;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.grouper.GrouperFactory;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import com.cognifide.aet.worker.api.JobRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GrouperDispatcherImpl implements GrouperDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperDispatcherImpl.class);

  private final Map<Comparator, AtomicLong> comparatorCounts = new HashMap<>();
  private final Map<Comparator, GrouperJob> grouperJobs = new HashMap<>();
  private final AtomicInteger counter = new AtomicInteger(0);
  private final int numberOfComparatorTypes;

  //todo clean this mess
  GrouperDispatcherImpl(JobRegistry jobRegistry, Map<Comparator, Long> comparatorCounts) {
    comparatorCounts.forEach((key, value) -> this.comparatorCounts.put(key, new AtomicLong(value)));
    this.numberOfComparatorTypes = comparatorCounts.keySet().size();
    for (Comparator comparator : comparatorCounts.keySet()) {
      String comparatorTypeName = comparator.getType();
      Optional<GrouperFactory> grouperFactory = jobRegistry.getGrouperFactory(comparatorTypeName);
      if (grouperFactory.isPresent()) {
        GrouperJob grouperJob =
            grouperFactory.get().createInstance(comparatorCounts.get(comparator));
        grouperJobs.put(comparator, grouperJob);
      } else {
        LOGGER.warn("GrouperJob not found for given type: {}", comparatorTypeName);
      }
    }
  }

  @Override
  public GrouperResultData run(String correlationId, GrouperJobData grouperJobData) {
    Comparator comparisonResult = grouperJobData.getComparisonResult();
    GrouperResultData result;
    if (!grouperJobs.containsKey(comparisonResult)) {   //todo improve
      long value = comparatorCounts.get(comparisonResult).decrementAndGet();
      result = new GrouperResultData(value == 0);
    } else {
      GrouperJob grouperJob = grouperJobs.get(comparisonResult);
      result = grouperJob.group(grouperJobData);
    }
    if (result.isReady()) {
      counter.incrementAndGet();
    }
    return result;
  }

  boolean isFinished() {
    return counter.get() == numberOfComparatorTypes; // todo move to wrapper?, enhance?
  }
}
