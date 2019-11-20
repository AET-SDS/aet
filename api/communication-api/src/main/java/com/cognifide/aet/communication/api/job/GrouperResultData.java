package com.cognifide.aet.communication.api.job;

import com.cognifide.aet.communication.api.JobStatus;
import java.io.Serializable;

public final class GrouperResultData implements Serializable {

  private static final long serialVersionUID = -692080374675249201L;

  private final JobStatus jobStatus;
  private final boolean isFinished;
  private final String testName;
  private String artifactId;

  public GrouperResultData(JobStatus jobStatus, boolean isFinished, String testName) {
    this.jobStatus = jobStatus;
    this.isFinished = isFinished;
    this.testName = testName;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public boolean isReady() {
    return isFinished;
  }

  public String getTestName() {
    return testName;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }
}
