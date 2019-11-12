package com.cognifide.aet.communication.api.job;

import com.cognifide.aet.communication.api.metadata.Comparator;
import java.util.Map;

// todo javadoc
public class GrouperJobData extends JobData {

  private static final long serialVersionUID = -3814742820402766119L;

  private final String urlName;
  private final Comparator comparisonResult; // todo maybe ComparatorStepResult?
  private final Map<Comparator, Long> comparatorCounts;

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
      Map<Comparator, Long> comparatorCounts,
      String testName,
      String urlName,
      Comparator comparisonResult) {
    super(company, project, suiteName, testName);
    this.urlName = urlName;
    this.comparisonResult = comparisonResult;
    this.comparatorCounts = comparatorCounts;
  }

  public String getUrlName() {
    return urlName;
  }

  public Comparator getComparisonResult() {
    return comparisonResult;
  }

  public Map<Comparator, Long> getComparatorCounts() {
    return comparatorCounts;
  }
}
