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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupingAlgorithm<T> {

  private final GroupingAlgorithmConfiguration<T> config;
  private final List<T> elementsToGroup;
  private final Set<T> processedElements = new HashSet<>();

  public GroupingAlgorithm(Collection<T> input, GroupingAlgorithmConfiguration<T> config) {
    this.elementsToGroup = new ArrayList<>(input);
    this.config = config;
  }

  public List<List<T>> group() throws GroupingException {
    if (elementsToGroup.isEmpty() || elementsToGroup.size() < 2 || config.getThreshold() < 0) {
      throw new GroupingException("error");
    }

    processedElements.clear();
    return performGrouping();
  }

  private List<List<T>> performGrouping() {
    List<List<T>> result = new ArrayList<>();
    List<T> related;
    int index = 0;

    for (T currentElement : elementsToGroup) {
      if (!processedElements.contains(currentElement)) {
        processedElements.add(currentElement);
        related = getRelated(currentElement);

        if (related.size() >= config.getMinimumGroupSize()) {
          for (int j = 0; j < related.size(); j++) {
            T relatedElement = related.get(j);
            if (!processedElements.contains(relatedElement)) {
              processedElements.add(relatedElement);
              List<T> individualRelated = getRelated(relatedElement);
              if (individualRelated.size() >= config.getMinimumGroupSize()) {
                combineRelatedElements(related, individualRelated);
              }
            }
          }
          result.add(related);
        }
      }
    }

    return result;
  }

  private List<T> getRelated(final T inputElement) {
    List<T> related = new ArrayList<>();
    elementsToGroup.stream()
        .filter(e -> config.getDistanceFunction().apply(inputElement, e) <= config.getThreshold())
        .forEach(related::add);

    return related;
  }

  private void combineRelatedElements(List<T> related1, List<T> related2) {
    related2.stream().filter(r -> !related1.contains(r)).forEach(related1::add);
  }
}
