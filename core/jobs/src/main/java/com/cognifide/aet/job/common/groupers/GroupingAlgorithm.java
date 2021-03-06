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
package com.cognifide.aet.job.common.groupers;

import java.util.Collection;
import java.util.Set;

/**
 * Algorithm implementing grouping logic of the elements of the same type T. Class T must provide
 * correct {@link Object#hashCode()} implementation, as the output of this algorithm are sets.
 *
 * @param <T> class of objects to be grouped, needs to provide correct {@link Object#hashCode()}
 *     implementation
 */
public interface GroupingAlgorithm<T> {

  /**
   * @param elementsToGroup collection of elements to be grouped
   * @return set of groups
   * @throws GroupingException when grouping could not be executed
   */
  Set<Set<T>> group(Collection<T> elementsToGroup) throws GroupingException;
}
