package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.communication.api.JobStatus;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class JsErrorsGrouper implements GrouperJob {

  private static final Type COMPARATOR_OUTPUT_TYPE = new TypeToken<Set<JsErrorLog>>() {}.getType();

  private final ArtifactsDAO artifactsDAO;
  private final DBKey dbKey;
  private final AtomicLong inputCounter;

  JsErrorsGrouper(ArtifactsDAO artifactsDAO, DBKey dbKey, AtomicLong inputCounter) {
    this.artifactsDAO = artifactsDAO;
    this.dbKey = dbKey;
    this.inputCounter = inputCounter;
  }

  @Override
  public GrouperResultData group(GrouperJobData jobData) {
    Comparator comparisonResult = jobData.getComparisonResult();
    String artifactId = comparisonResult.getStepResult().getArtifactId();
    try {
      Set<JsErrorLog> jsErrorLogs =
          artifactsDAO.getJsonFormatArtifact(dbKey, artifactId, COMPARATOR_OUTPUT_TYPE);
      String a = "";
    } catch (IOException e) {
      e.printStackTrace(); // todo
    }

    long value = inputCounter.decrementAndGet();
    return new GrouperResultData(JobStatus.SUCCESS, value == 0);
  }
}
