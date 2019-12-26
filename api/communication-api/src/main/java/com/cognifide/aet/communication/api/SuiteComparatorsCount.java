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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides useful methods for obtaining some specific data regarding counting comparator types
 * within the suite object.
 *
 * @see com.cognifide.aet.communication.api.metadata.Suite
 */
public class SuiteComparatorsCount implements Serializable {

  private static final long serialVersionUID = -2745599996925319741L;

  private final Map<String, Map<Comparator, Integer>> map;

  private SuiteComparatorsCount(Map<String, Map<Comparator, Integer>> map) {
    this.map = map;
  }

  /**
   * @param tests collection of test objects that will be considered when counting comparators
   * @return instance of this class
   * @see Test
   */
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

  /**
   * @param testName test name
   * @return number of distinct comparator types for given test name
   * @throws NullPointerException when test name not found
   */
  public int getDistinctComparatorsCountForTest(String testName) {
    return map.get(testName).keySet().size();
  }

  /**
   * @param testName test name
   * @return set of all the comparatorType-count pairs for given test name
   * @throws NullPointerException when test name not found
   * @see Pair
   */
  public Set<Pair<Comparator, Integer>> getAllComparatorCountsForTest(String testName) {
    return map.get(testName).entrySet().stream()
        .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());
  }

  /**
   * @param testName test name
   * @return map, where key is a type of the comparator and value is an AtomicInteger object
   * instantiated with the number of this comparator type usages within the test
   * @throws NullPointerException when test name not found
   */
  public Map<String, AtomicInteger> prepareCountdownsByComparatorTypes(String testName) {
    Map<String, AtomicInteger> result = new HashMap<>();
    for (Entry<Comparator, Integer> entry : map.get(testName).entrySet()) {
      if (entry.getKey().getType().equals("source")) {
        String compType = entry.getKey().getParameters().get(Comparator.COMPARATOR_PARAMETER);
        if (compType.equals("w3c-html5")) {
          result.put("source_w3c-html5", new AtomicInteger(entry.getValue()));
        } else {
          result.put("source", new AtomicInteger(entry.getValue()));
        }
      } else {
        result.put(entry.getKey().getType(), new AtomicInteger(entry.getValue()));
      }
    }

    return result;
//    // todo enum as key
//    return map.get(testName).entrySet().stream()
//        .collect(
//            Collectors.toMap(e -> e.getKey().getType(), it -> new AtomicInteger(it.getValue())));
  }

  public Map<String, Map<Comparator, Integer>> abc() {
    return map;
  }
}
