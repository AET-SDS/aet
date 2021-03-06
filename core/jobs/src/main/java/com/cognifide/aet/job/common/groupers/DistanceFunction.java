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

import java.util.function.BiFunction;

/**
 * Function calculating distance between two objects of the same type.
 *
 * @param <T> class of objects for which the distance will be calculated
 */
public interface DistanceFunction<T> extends BiFunction<T, T, Double> {

  /** @return distance between two input objects, with value between 0 and 1 */
  @Override
  Double apply(T first, T second);
}
