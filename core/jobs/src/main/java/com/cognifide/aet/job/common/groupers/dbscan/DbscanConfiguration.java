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

import com.cognifide.aet.job.common.groupers.DistanceFunction;

public class DbscanConfiguration<T> {

  private final double threshold;
  private final int minimumGroupSize;
  private final DistanceFunction<T> distanceFunction;

  public DbscanConfiguration(
      double threshold, int minGroupSize, DistanceFunction<T> distanceFunction) {
    this.threshold = threshold;
    this.minimumGroupSize = minGroupSize;
    this.distanceFunction = distanceFunction;
  }

  public double getThreshold() {
    return threshold;
  }

  public int getMinimumGroupSize() {
    return minimumGroupSize;
  }

  public DistanceFunction<T> getDistanceFunction() {
    return distanceFunction;
  }
}
