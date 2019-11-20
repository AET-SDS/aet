package com.cognifide.aet.worker.impl;

import java.util.Objects;
import java.util.StringJoiner;

class SuiteTestIdentifier {

  private final String correlationId;
  private final String testName;

  SuiteTestIdentifier(String correlationId, String testName) {
    this.correlationId = correlationId;
    this.testName = testName;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getTestName() {
    return testName;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SuiteTestIdentifier.class.getSimpleName() + "[", "]")
        .add("correlationId='" + correlationId + "'")
        .add("testName='" + testName + "'")
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuiteTestIdentifier that = (SuiteTestIdentifier) o;
    return Objects.equals(correlationId, that.correlationId)
        && Objects.equals(testName, that.testName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(correlationId, testName);
  }
}
