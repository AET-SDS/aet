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
package com.cognifide.aet.job.common.groupers.algorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DBSCANAlgorithm<T> implements GroupingAlgorithm<T> {

  private final DBSCANConfiguration<T> config;
  private final Set<T> processedElements = new HashSet<>();

  public DBSCANAlgorithm(DBSCANConfiguration<T> config) {
    this.config = config;
  }

  @Override
  public Set<Set<T>> group(Collection<T> elementsToGroup) throws GroupingException {
    if (config.getThreshold() < 0) {
      throw new GroupingException("Threshold cannot be less than 0");
    }

    processedElements.clear();
    return performGrouping(elementsToGroup);
  }

  private Set<Set<T>> performGrouping(Collection<T> elementsToGroup) {
    Set<Set<T>> result = new HashSet<>();

    for (T currentElement : elementsToGroup) {
      if (!processedElements.contains(currentElement)) {
        processedElements.add(currentElement);
        Set<T> related = getRelated(currentElement, elementsToGroup);

        if (related.size() >= config.getMinimumGroupSize()) {
          for (T relatedElement : related) {
            if (!processedElements.contains(relatedElement)) {
              processedElements.add(relatedElement);
              Set<T> individualRelated = getRelated(relatedElement, elementsToGroup);
              if (individualRelated.size() >= config.getMinimumGroupSize()) {
                individualRelated.removeIf(related::contains);
                related.addAll(individualRelated);
              }
            }
          }
          result.add(related);
        }
      }
    }

    return result;
  }

  private Set<T> getRelated(final T inputElement, Collection<T> elementsToGroup) {
    return elementsToGroup.stream()
        .filter(e -> config.getDistanceFunction().apply(inputElement, e) <= config.getThreshold())
        .collect(Collectors.toSet());
  }
}
