package com.cognifide.aet.communication.api.job;

import com.cognifide.aet.communication.api.metadata.Comparator;

// todo javadoc
public class GrouperJobData extends JobData {

  private final String urlName;

  private final Comparator comparisonResult; // todo maybe ComparatorStepResult?

  /**
   * @param company - company name.
   * @param project - project name.
   * @param suiteName - suite name.
   * @param testName - test name.
   * @param urlName - name of url.
   * @param comparisonResult - result of the comparison.
   */
  public GrouperJobData(
      String company,
      String project,
      String suiteName,
      String testName,
      String urlName,
      Comparator comparisonResult) {
    super(company, project, suiteName, testName);
    this.urlName = urlName;
    this.comparisonResult = comparisonResult;
  }

  public String getUrlName() {
    return urlName;
  }

  public Comparator getComparisonResult() {
    return comparisonResult;
  }
}
