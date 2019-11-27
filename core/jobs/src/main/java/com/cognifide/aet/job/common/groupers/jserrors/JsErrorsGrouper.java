package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.communication.api.JobStatus;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.job.api.grouper.SimilarityValue;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class JsErrorsGrouper implements GrouperJob {

  public static final String NAME = "js-errors"; // todo ErrorType?
  private static final Type COMPARATOR_OUTPUT_TYPE = new TypeToken<Set<JsErrorLog>>() {}.getType();

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
    try {
      Comparator comparisonResult = jobData.getComparisonResult();
      String artifactId = comparisonResult.getStepResult().getArtifactId();
      Set<JsErrorLog> jsErrorLogs =
          artifactsDAO.getJsonFormatArtifact(dbKey, artifactId, COMPARATOR_OUTPUT_TYPE);
      jsErrors.addAll(jsErrorLogs);
    } catch (IOException e) {
      e.printStackTrace(); // todo
    }

    long currentMessagesRemaining = messagesRemaining.decrementAndGet();
    GrouperResultData result =
        new GrouperResultData(
            JobStatus.SUCCESS, NAME, currentMessagesRemaining == 0, jobData.getTestName());
    if (currentMessagesRemaining == 0) {
      List<SimilarityValue<JsErrorLog>> similarityValues = calculateDistances(jsErrors);
      String artifactId = artifactsDAO.saveArtifactInJsonFormat(dbKey, similarityValues);
      result.setArtifactId(artifactId);
    }
    return result;
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
