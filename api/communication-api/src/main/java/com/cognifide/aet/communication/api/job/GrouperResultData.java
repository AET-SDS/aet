package com.cognifide.aet.communication.api.job;

import com.cognifide.aet.communication.api.JobStatus;
import java.io.Serializable;

public final class GrouperResultData implements Serializable {

  private static final long serialVersionUID = -692080374675249201L;

  private final JobStatus jobStatus;
  private final boolean isFinished;

  public GrouperResultData(JobStatus jobStatus, boolean isFinished) {
    this.jobStatus = jobStatus;
    this.isFinished = isFinished;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public boolean isReady() {
    return isFinished;
  }
}
