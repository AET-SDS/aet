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

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.grouper.GrouperFactory;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.vs.DBKey;
import com.cognifide.aet.vs.SimpleDBKey;
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

  private final ConcurrentMap<SuiteTestIdentifier, AtomicInteger> completedJobsCounters =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<SuiteTestIdentifier, GrouperDispatcher> dispatchers =
      new ConcurrentHashMap<>();

  @Override
  public GrouperResultData run(String correlationId, GrouperJobData grouperJobData) {
    SuiteTestIdentifier suiteTestIdentifier =
        new SuiteTestIdentifier(correlationId, grouperJobData.getTestName());
    Map<Comparator, Long> comparatorCounts = grouperJobData.getComparatorCounts();
    GrouperDispatcher dispatcher = getDispatcher(suiteTestIdentifier, grouperJobData);
    GrouperResultData resultData = dispatcher.run(correlationId, grouperJobData);
    if (resultData.isReady()) {
      AtomicInteger completedJobsCounter =
          getCompletedJobsCounter(suiteTestIdentifier, comparatorCounts);
      int numberOfPendingJobs = completedJobsCounter.decrementAndGet();
      if (numberOfPendingJobs == 0) {
        LOGGER.error("DELETING DISPATCHER FOR ID: {}", suiteTestIdentifier); // todo
        dispatchers.remove(suiteTestIdentifier);
        completedJobsCounters.remove(suiteTestIdentifier);
      }
    }
    return resultData;
  }

  private AtomicInteger getCompletedJobsCounter(
      SuiteTestIdentifier suiteTestIdentifier, Map<Comparator, Long> comparatorCounts) {
    return completedJobsCounters.computeIfAbsent(
        suiteTestIdentifier, __ -> new AtomicInteger(comparatorCounts.keySet().size()));
  }

  private GrouperDispatcher getDispatcher(
      SuiteTestIdentifier suiteTestIdentifier, GrouperJobData grouperJobData) {
    Map<Comparator, Long> comparatorCounts = grouperJobData.getComparatorCounts();
    return dispatchers.computeIfAbsent(
        suiteTestIdentifier,
        __ -> {
          Map<Comparator, AtomicLong> counters = prepareComparatorCounters(comparatorCounts);
          Map<Comparator, GrouperJob> grouperJobs = prepareGrouperJobs(grouperJobData);
          return new GrouperDispatcherImpl(counters, grouperJobs);
        });
  }

  private Map<Comparator, AtomicLong> prepareComparatorCounters(
      Map<Comparator, Long> comparatorCounts) {
    return comparatorCounts.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, it -> new AtomicLong(it.getValue())));
  }

  private Map<Comparator, GrouperJob> prepareGrouperJobs(GrouperJobData grouperJobData) {
    final DBKey dbKey = new SimpleDBKey(grouperJobData.getCompany(), grouperJobData.getProject());
    Map<Comparator, Long> comparatorCounts = grouperJobData.getComparatorCounts();
    Map<Comparator, GrouperJob> grouperJobs = new HashMap<>();
    for (Comparator comparator : comparatorCounts.keySet()) {
      String comparatorTypeName = comparator.getType();
      Optional<GrouperFactory> grouperFactory = jobRegistry.getGrouperFactory(comparatorTypeName);
      if (grouperFactory.isPresent()) {
        long expectedInputCount = comparatorCounts.get(comparator);
        GrouperJob grouperJob = grouperFactory.get().createInstance(dbKey, expectedInputCount);
        grouperJobs.put(comparator, grouperJob);
      } else {
        LOGGER.warn("GrouperJob not found for given type: {}", comparatorTypeName);
      }
    }
    return grouperJobs;
  }
}
