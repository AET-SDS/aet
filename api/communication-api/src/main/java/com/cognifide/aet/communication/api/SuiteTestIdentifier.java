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

package com.cognifide.aet.communication.api;

import java.util.Objects;
import java.util.StringJoiner;

public class SuiteTestIdentifier {

  private final String correlationId;
  private final String testName;

  public SuiteTestIdentifier(String correlationId, String testName) {
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
