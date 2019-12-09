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

package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.job.api.grouper.GrouperFactory;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.job.common.comparators.jserrors.JsErrorsComparator;
import com.cognifide.aet.job.common.groupers.DistanceFunction;
import com.cognifide.aet.job.common.groupers.GroupingAlgorithm;
import com.cognifide.aet.job.common.groupers.dbscan.DbscanAlgorithm;
import com.cognifide.aet.job.common.groupers.dbscan.DbscanConfiguration;
import com.cognifide.aet.models.jserrors.JsErrorLog;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import java.util.concurrent.atomic.AtomicLong;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class JsErrorsGrouperFactory implements GrouperFactory {

  // todo make configurable
  private static final double DBSCAN_THRESHOLD = 0.1;
  private static final int DBSCAN_MIN_GROUP_SIZE = 1;
  private static final DistanceFunction<JsErrorLog> DBSCAN_DISTANCE_FUNCTION =
      new JsErrorsDistanceFunction();

  @Reference private ArtifactsDAO artifactsDAO;

  @Override
  public String getName() {
    return JsErrorsComparator.COMPARATOR_NAME;
  }

  @Override
  public GrouperJob createInstance(DBKey dbKey, long expectedInputCount) {
    DbscanConfiguration<JsErrorLog> algorithmConfig =
        new DbscanConfiguration<>(
            DBSCAN_THRESHOLD, DBSCAN_MIN_GROUP_SIZE, DBSCAN_DISTANCE_FUNCTION);
    GroupingAlgorithm<JsErrorLog> algorithm = new DbscanAlgorithm<>(algorithmConfig);
    AtomicLong expectedMessagesCount = new AtomicLong(expectedInputCount);
    return new JsErrorsGrouper(artifactsDAO, dbKey, expectedMessagesCount, algorithm);
  }
}
