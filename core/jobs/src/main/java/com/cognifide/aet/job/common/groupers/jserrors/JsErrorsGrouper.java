package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.communication.api.JobStatus;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsErrorsGrouper implements GrouperJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsErrorsGrouper.class);
  private static final Type COMPARATOR_OUTPUT_TYPE = new TypeToken<Set<JsErrorLog>>() {}.getType();

  private final ArtifactsDAO artifactsDAO;
  private final DBKey dbKey;
  private final AtomicLong inputCounter;
  private final Multimap<String, JsErrorLog> jsErrors = LinkedListMultimap.create();
  private final Map<Pair<Entry<String, JsErrorLog>, Entry<String, JsErrorLog>>, Integer> distances;

  JsErrorsGrouper(ArtifactsDAO artifactsDAO, DBKey dbKey, AtomicLong inputCounter) {
    this.artifactsDAO = artifactsDAO;
    this.dbKey = dbKey;
    this.inputCounter = inputCounter;
    this.distances = new HashMap<>();
  }

  @Override
  public GrouperResultData group(GrouperJobData jobData) {
    try {
      String urlName = jobData.getUrlName();
      Comparator comparisonResult = jobData.getComparisonResult();
      String artifactId = comparisonResult.getStepResult().getArtifactId();
      Set<JsErrorLog> jsErrorLogs =
          artifactsDAO.getJsonFormatArtifact(
              dbKey, artifactId, COMPARATOR_OUTPUT_TYPE); // todo entries will contain urls
      jsErrorLogs.forEach(it -> jsErrors.put(urlName, it));
    } catch (IOException e) {
      e.printStackTrace(); // todo
    }

    long value = inputCounter.decrementAndGet();
    if (value == 0) {
      calculateDistances();
      String artifactId = artifactsDAO.saveArtifactInJsonFormat(dbKey, distances);
      // todo prints:
      // "(promocje\u003dcom.cognifide.aet.job.api.collector.JsErrorLog@90ac2780,promocje\u003dcom.cognifide.aet.job.api.collector.JsErrorLog@90ac2780)":0
      LOGGER.error("GROUPER RESULT ARTIFACT_ID: {}", artifactId); // todo
    }
    return new GrouperResultData(JobStatus.SUCCESS, value == 0);
  }

  private void calculateDistances() {
    for (Entry<String, JsErrorLog> entry1 : jsErrors.entries()) {
      for (Entry<String, JsErrorLog> entry2 : jsErrors.entries()) {
        // todo what if the same
        String errorMessage1 = entry1.getValue().getErrorMessage();
        String errorMessage2 = entry2.getValue().getErrorMessage();
        int distance = new LevenshteinDistance().apply(errorMessage1, errorMessage2);
        int longerLength = Math.max(errorMessage1.length(), errorMessage2.length());
        int relativeDistance = (longerLength - distance) * 100 / longerLength;
        Pair<Entry<String, JsErrorLog>, Entry<String, JsErrorLog>> key = Pair.of(entry1, entry2);
        distances.put(key, relativeDistance);
      }
    }
  }
}
