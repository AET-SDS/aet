/*
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.cognifide.aet.communication.api.job;

import com.cognifide.aet.communication.api.JobStatus;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public final class GrouperResultData implements Serializable {

  private static final long serialVersionUID = -692080374675249201L;

  private final JobStatus jobStatus;
  private final String grouperType;
  private final boolean isFinished;
  private final String testName;
  private String artifactId;

  public GrouperResultData(
      JobStatus jobStatus, String grouperType, boolean isFinished, String testName) {
    this.jobStatus = jobStatus;
    this.grouperType = grouperType;
    this.isFinished = isFinished;
    this.testName = testName;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public String getGrouperType() {
    return grouperType;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GrouperResultData that = (GrouperResultData) o;
    return isFinished == that.isFinished
        && jobStatus == that.jobStatus
        && Objects.equals(grouperType, that.grouperType)
        && Objects.equals(testName, that.testName)
        && Objects.equals(artifactId, that.artifactId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jobStatus, grouperType, isFinished, testName, artifactId);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GrouperResultData.class.getSimpleName() + "[", "]")
        .add("jobStatus=" + jobStatus)
        .add("grouperType='" + grouperType + "'")
        .add("isFinished=" + isFinished)
        .add("testName='" + testName + "'")
        .add("artifactId='" + artifactId + "'")
        .toString();
  }
}
