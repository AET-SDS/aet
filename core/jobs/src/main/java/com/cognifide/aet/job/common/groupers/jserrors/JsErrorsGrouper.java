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

import com.cognifide.aet.communication.api.JobStatus;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.job.api.grouper.SimilarityValue;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsErrorsGrouper implements GrouperJob {

  public static final String NAME = "js-errors"; // todo ErrorType?
  private static final Type COMPARATOR_OUTPUT_TYPE = new TypeToken<Set<JsErrorLog>>() {}.getType();
  private static final Logger LOGGER = LoggerFactory.getLogger(JsErrorsGrouper.class);

  private final ArtifactsDAO artifactsDAO;
  private final DBKey dbKey;
  private final AtomicLong messagesRemaining;
  private final List<JsErrorLog> jsErrors = new ArrayList<>();

  JsErrorsGrouper(ArtifactsDAO artifactsDAO, DBKey dbKey, AtomicLong messagesRemaining) {
    this.artifactsDAO = artifactsDAO;
    this.dbKey = dbKey;
    this.messagesRemaining = messagesRemaining;
  }

  @Override
  public GrouperResultData group(GrouperJobData jobData) {
    String inputArtifactId = jobData.getComparisonResult().getArtifactId();
    if (!Strings.isNullOrEmpty(inputArtifactId)) {
      loadJsErrors(inputArtifactId);
    }
    long currentMessagesRemaining = messagesRemaining.decrementAndGet();
    GrouperResultData result =
        new GrouperResultData(
            JobStatus.SUCCESS, NAME, currentMessagesRemaining == 0, jobData.getTestName());
    LOGGER.debug(
        "JsErrors grouping... {} remaining. Input artifactId: {}. Status: {}",
        currentMessagesRemaining,
        inputArtifactId,
        result);
    if (currentMessagesRemaining == 0) {
      List<SimilarityValue<JsErrorLog>> similarityValues = calculateDistances(jsErrors);
      String outputArtifactId = artifactsDAO.saveArtifactInJsonFormat(dbKey, similarityValues);
      result.setArtifactId(outputArtifactId);
      LOGGER.debug("JsErrors grouped! Output artifactId: {}", outputArtifactId);
    }
    return result;
  }

  private void loadJsErrors(String inputArtifactId) {
    try {
      Set<JsErrorLog> jsErrorLogs =
          artifactsDAO.getJsonFormatArtifact(dbKey, inputArtifactId, COMPARATOR_OUTPUT_TYPE);
      jsErrors.addAll(jsErrorLogs);
    } catch (IOException e) {
      LOGGER.error("Could not fetch jsErrors: {}", inputArtifactId, e);
      // todo change jobStatus? partial success?
    }
  }

  private static List<SimilarityValue<JsErrorLog>> calculateDistances(List<JsErrorLog> jsErrors) {
    int listSize = jsErrors.size() * (jsErrors.size() - 1) / 2;
    List<SimilarityValue<JsErrorLog>> distances = new ArrayList<>(listSize);
    for (int i = 0; i < jsErrors.size(); i++) {
      JsErrorLog jsError1 = jsErrors.get(i);
      for (int j = i + 1; j < jsErrors.size(); j++) {
        JsErrorLog jsError2 = jsErrors.get(j);
        distances.add(getSimilarityValue(jsError1, jsError2));
      }
    }
    return distances;
  }

  private static SimilarityValue<JsErrorLog> getSimilarityValue(JsErrorLog e1, JsErrorLog e2) {
    String msg1 = e1.getErrorMessage();
    String msg2 = e2.getErrorMessage();
    int similarity = calculateRelativeDistance(msg1, msg2);
    return new SimilarityValue<>(e1, e2, similarity);
  }

  private static int calculateRelativeDistance(String s1, String s2) {
    int distance = new LevenshteinDistance().apply(s1, s2);
    int longerLength = Math.max(s1.length(), s2.length());
    return (longerLength - distance) * 100 / longerLength;
  }
}
