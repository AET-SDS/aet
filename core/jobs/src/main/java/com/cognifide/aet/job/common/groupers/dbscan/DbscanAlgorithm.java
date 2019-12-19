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
package com.cognifide.aet.job.common.groupers.dbscan;

import com.cognifide.aet.job.common.groupers.GroupingAlgorithm;
import com.cognifide.aet.job.common.groupers.GroupingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of grouping algorithm, known as the DBSCAN algorithm.
 *
 * @param <T> {@inheritDoc}
 */
public class DbscanAlgorithm<T> implements GroupingAlgorithm<T> {

  private final DbscanConfiguration<T> config;

  public DbscanAlgorithm(DbscanConfiguration<T> config) {
    this.config = config;
  }

  @Override
  public Set<Set<T>> group(Collection<T> elementsToGroup) throws GroupingException {
    if (config.getThreshold() < 0 || config.getThreshold() > 1) {
      throw new GroupingException("Threshold cannot be less than 0 or more than 1");
    }
    return getGroups(elementsToGroup);
  }

  private Set<Set<T>> getGroups(Collection<T> elementsToGroup) {
    Set<T> processedElements = new HashSet<>();
    Set<Set<T>> groups = new HashSet<>();
    for (T currentElement : elementsToGroup) {
      if (!processedElements.contains(currentElement)) {
        processedElements.add(currentElement);
        Set<T> currentGroup = getRelated(currentElement, elementsToGroup);
        if (currentGroup.size() >= config.getMinimumGroupSize()) {
          expandGroup(currentGroup, processedElements, elementsToGroup);
          groups.add(currentGroup);
        }
      }
    }
    return groups;
  }

  private void expandGroup(Set<T> currentGroup, Set<T> processed, Collection<T> allElements) {
    List<T> neighboursToProcess = new ArrayList<>(currentGroup);
    // we need to consider as well the elements appended within this loop
    for (int i = 0; i < neighboursToProcess.size(); i++) {
      T neighbour = neighboursToProcess.get(i);
      if (!processed.contains(neighbour)) {
        processed.add(neighbour);
        Set<T> neighbourRelated = getRelated(neighbour, allElements);
        if (neighbourRelated.size() >= config.getMinimumGroupSize()) {
          neighboursToProcess.addAll(neighbourRelated);
          currentGroup.addAll(neighbourRelated);
        }
      }
    }
  }

  private Set<T> getRelated(final T inputElement, Collection<T> elementsToGroup) {
    return elementsToGroup.stream()
        .filter(e -> config.getDistanceFunction().apply(inputElement, e) <= config.getThreshold())
        .collect(Collectors.toSet());
  }
}
