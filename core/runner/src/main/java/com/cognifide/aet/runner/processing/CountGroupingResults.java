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

package com.cognifide.aet.runner.processing;

import com.cognifide.aet.communication.api.metadata.Suite;
import com.cognifide.aet.communication.api.metadata.Test;
import java.util.function.Function;

/**
 * First class function, used for counting expected grouping-stage results in a certain suite.
 *
 * @see Suite
 */
public class CountGroupingResults implements Function<Suite, Integer> {

  public static final CountGroupingResults INSTANCE = new CountGroupingResults();

  private CountGroupingResults() {
  }

  /**
   * @param suite suite object
   * @return number of distinct comparator types within the suite
   */
  @Override
  public Integer apply(Suite suite) {
    return suite.getTests().stream()
        .map(this::countDistinctComparatorsInTest)
        .reduce(0, Integer::sum);
  }

  private int countDistinctComparatorsInTest(Test test) {
    return test.getUrls().stream()
        .flatMap(url -> url.getSteps().stream())
        .flatMap(step -> step.getComparators().stream())
        .distinct()
        .mapToInt(comparator -> 1)
        .sum();
  }
}
