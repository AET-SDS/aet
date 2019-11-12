package com.cognifide.aet.worker.impl;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.grouper.GrouperFactory;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import com.cognifide.aet.worker.api.JobRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GrouperDispatcherImpl implements GrouperDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperDispatcherImpl.class);

  private final Map<Comparator, Long> comparatorCounts;

  private final Map<Comparator, GrouperJob> grouperJobs = new HashMap<>();

  private final AtomicInteger counter = new AtomicInteger(0);

  GrouperDispatcherImpl(JobRegistry jobRegistry, Map<Comparator, Long> comparatorCounts) {
    this.comparatorCounts = comparatorCounts;
    for (Comparator comparator : comparatorCounts.keySet()) {
      String comparatorTypeName = comparator.getType();
      Optional<GrouperFactory> grouperFactory = jobRegistry.getGrouperFactory(comparatorTypeName);
      if (grouperFactory.isPresent()) {
        GrouperJob grouperJob = grouperFactory.get().createInstance();
        grouperJobs.put(comparator, grouperJob);
      } else {
        LOGGER.warn("GrouperJob not found for given type: {}", comparatorTypeName);
      }
    }
  }

  @Override
  public void run(String correlationId, GrouperJobData grouperJobData) {
    counter.incrementAndGet();
  }

  boolean isFinished() {
    return counter.get() == comparatorCounts.values().stream().reduce(0L, Long::sum); // todo
  }
}
