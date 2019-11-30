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

import com.cognifide.aet.communication.api.metadata.Comparator;
import java.util.Map;

// todo javadoc
public class GrouperJobData extends JobData {

  private static final long serialVersionUID = -3814742820402766119L;

  private final Comparator comparisonResult; // todo maybe ComparatorStepResult?
  private final Map<Comparator, Long> comparatorCounts;

  /**
   * @param company - company name.
   * @param project - project name.
   * @param suiteName - suite name.
   * @param testName - test name.
   * @param comparisonResult - result of the comparison.
   */
  public GrouperJobData(
      String company,
      String project,
      String suiteName,
      Map<Comparator, Long> comparatorCounts,
      String testName,
      Comparator comparisonResult) {
    super(company, project, suiteName, testName);
    this.comparisonResult = comparisonResult;
    this.comparatorCounts = comparatorCounts;
  }

  public Comparator getComparisonResult() {
    return comparisonResult;
  }

  public Map<Comparator, Long> getComparatorCounts() {
    return comparatorCounts;
  }
}
