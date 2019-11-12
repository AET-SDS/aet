package com.cognifide.aet.communication.api.job;

import java.io.Serializable;

public final class GrouperResultData implements Serializable {

  private static final long serialVersionUID = -692080374675249201L;

  private final boolean isFinished;

  public GrouperResultData(boolean isFinished) {
    this.isFinished = isFinished;
  }

  public boolean isReady() {
    return isFinished;
  }
}
