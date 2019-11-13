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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class GrouperDispatcherWrapper implements GrouperDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperDispatcherWrapper.class);

  @Reference private JobRegistry jobRegistry;

  private final ConcurrentMap<String, AtomicInteger> completedJobsCounters =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<String, GrouperDispatcher> dispatchers = new ConcurrentHashMap<>();

  @Override
  public GrouperResultData run(String correlationId, GrouperJobData grouperJobData) {
    Map<Comparator, Long> comparatorCounts = grouperJobData.getComparatorCounts();
    GrouperDispatcher dispatcher = getDispatcher(correlationId, comparatorCounts);
    GrouperResultData resultData = dispatcher.run(correlationId, grouperJobData);
    if (resultData.isReady()) {
      AtomicInteger completedJobsCounter = getCompletedJobsCounter(correlationId, comparatorCounts);
      int numberOfPendingJobs = completedJobsCounter.decrementAndGet();
      if (numberOfPendingJobs == 0) {
        LOGGER.error("DELETING DISPATCHER FOR ID: {}", correlationId); // todo
        dispatchers.remove(correlationId);
        completedJobsCounters.remove(correlationId);
      }
    }
    return resultData;
  }

  private AtomicInteger getCompletedJobsCounter(
      String correlationId, Map<Comparator, Long> comparatorCounts) {
    return completedJobsCounters.computeIfAbsent(
        correlationId, __ -> new AtomicInteger(comparatorCounts.keySet().size()));
  }

  private GrouperDispatcher getDispatcher(
      String correlationId, Map<Comparator, Long> comparatorCounts) {
    return dispatchers.computeIfAbsent(
        correlationId,
        __ -> {
          Map<Comparator, AtomicLong> counters = prepareComparatorCounters(comparatorCounts);
          Map<Comparator, GrouperJob> grouperJobs = prepareGrouperJobs(comparatorCounts);
          return new GrouperDispatcherImpl(counters, grouperJobs);
        });
  }

  private Map<Comparator, AtomicLong> prepareComparatorCounters(
      Map<Comparator, Long> comparatorCounts) {
    return comparatorCounts.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, it -> new AtomicLong(it.getValue())));
  }

  private Map<Comparator, GrouperJob> prepareGrouperJobs(Map<Comparator, Long> comparatorCounts) {
    Map<Comparator, GrouperJob> grouperJobs = new HashMap<>();
    for (Comparator comparator : comparatorCounts.keySet()) {
      String comparatorTypeName = comparator.getType();
      Optional<GrouperFactory> grouperFactory = jobRegistry.getGrouperFactory(comparatorTypeName);
      if (grouperFactory.isPresent()) {
        long expectedInputCount = comparatorCounts.get(comparator);
        GrouperJob grouperJob = grouperFactory.get().createInstance(expectedInputCount);
        grouperJobs.put(comparator, grouperJob);
      } else {
        LOGGER.warn("GrouperJob not found for given type: {}", comparatorTypeName);
      }
    }
    return grouperJobs;
  }
}
