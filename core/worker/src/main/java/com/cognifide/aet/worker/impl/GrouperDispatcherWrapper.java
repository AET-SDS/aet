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

import com.cognifide.aet.communication.api.SuiteTestIdentifier;
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
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class GrouperDispatcherWrapper implements GrouperDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperDispatcherWrapper.class);

  @Reference private JobRegistry jobRegistry;
  @Reference private GrouperDispatcherFactory grouperDispatcherFactory;

  @Override
  public GrouperResultData run(String correlationId, GrouperJobData grouperJobData) {
    SuiteTestIdentifier suiteTestIdentifier =
        new SuiteTestIdentifier(correlationId, grouperJobData.getTestName());
    GrouperDispatcher dispatcher =
        grouperDispatcherFactory.getDispatcher(
            suiteTestIdentifier,
            grouperJobData.getSuiteComparatorsCount(),
            () -> prepareGrouperJobs(grouperJobData));
    GrouperResultData resultData = dispatcher.run(correlationId, grouperJobData);
    if (resultData.isReady()) {
      grouperDispatcherFactory.tick(suiteTestIdentifier);
    }
    return resultData;
  }

  private Map<String, GrouperJob> prepareGrouperJobs(GrouperJobData grouperJobData) {
    final Set<Pair<Comparator, Integer>> allComparatorCountsForTest =
        grouperJobData
            .getSuiteComparatorsCount()
            .getAllComparatorCountsForTest(grouperJobData.getTestName());
    final DBKey dbKey = new SimpleDBKey(grouperJobData.getCompany(), grouperJobData.getProject());
    final Map<String, GrouperJob> grouperJobs = new HashMap<>();
    for (Pair<Comparator, Integer> pair : allComparatorCountsForTest) {
      String comparatorTypeName = pair.getLeft().getType();
      Optional<GrouperFactory> grouperFactory = jobRegistry.getGrouperFactory(comparatorTypeName);
      if (grouperFactory.isPresent()) {
        GrouperJob grouperJob = grouperFactory.get().createInstance(dbKey, pair.getRight());
        grouperJobs.put(comparatorTypeName, grouperJob);
      } else {
        LOGGER.warn("GrouperJob not found for given type: {}", comparatorTypeName);
      }
    }
    return grouperJobs;
  }
}
