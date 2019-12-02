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

import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.communication.api.metadata.Test;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class SuiteComparatorsCount implements Serializable {

  private static final long serialVersionUID = -2745599996925319741L;

  private final Map<String, Map<Comparator, Integer>> map;

  private SuiteComparatorsCount(Map<String, Map<Comparator, Integer>> map) {
    this.map = map;
  }

  public static SuiteComparatorsCount of(Collection<Test> tests) {
    Map<String, Map<Comparator, Integer>> comparatorCounts = new HashMap<>();
    for (Test test : tests) {
      Map<Comparator, Integer> collect =
          test.getUrls().stream()
              .flatMap(url -> url.getSteps().stream())
              .flatMap(step -> step.getComparators().stream())
              .collect(
                  Collectors.groupingBy(
                      Function.identity(), Collectors.reducing(0, el -> 1, Integer::sum)));
      comparatorCounts.put(test.getName(), collect);
    }
    return new SuiteComparatorsCount(comparatorCounts);
  }

  public int getDistinctComparatorsCountForTest(String testName) {
    return map.get(testName).keySet().size();
  }

  public Set<Pair<Comparator, Integer>> getAllComparatorCountsForTest(String testName) {
    return map.get(testName).entrySet().stream()
        .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());
  }

  public Map<String, AtomicInteger> prepareCountdownsByComparatorTypes(String testName) {
    // todo enum as key
    return map.get(testName).entrySet().stream()
        .collect(
            Collectors.toMap(e -> e.getKey().getType(), it -> new AtomicInteger(it.getValue())));
  }
}
