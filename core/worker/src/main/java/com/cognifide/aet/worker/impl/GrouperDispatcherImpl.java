package com.cognifide.aet.worker.impl;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class GrouperDispatcherImpl implements GrouperDispatcher {

  private final Map<Comparator, Long> comparatorCounts;

  private final AtomicInteger counter = new AtomicInteger(0);

  GrouperDispatcherImpl(Map<Comparator, Long> comparatorCounts) {
    this.comparatorCounts = comparatorCounts;
  }

  @Override
  public void run(String correlationId, GrouperJobData grouperJobData) {
    counter.incrementAndGet();
  }

  boolean isFinished() {
    return counter.get() == comparatorCounts.values().stream().reduce(0L, Long::sum); // todo
  }
}
