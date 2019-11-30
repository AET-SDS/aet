package com.cognifide.aet.runner.processing;

import com.cognifide.aet.communication.api.metadata.Suite;
import java.util.function.Function;

// todo javadoc
public class CountGroupingResults implements Function<Suite, Integer> {

  public static final CountGroupingResults INSTANCE = new CountGroupingResults();

  private CountGroupingResults() {
  }

  @Override
  public Integer apply(Suite suite) {
    return suite.getTests().stream()
        .map(
            test ->
                test.getUrls().stream()
                    .flatMap(url -> url.getSteps().stream())
                    .flatMap(step -> step.getComparators().stream())
                    .distinct()
                    .mapToInt(comparator -> 1)
                    .sum())
        .reduce(0, Integer::sum);
  }
}
