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

package com.cognifide.aet.job.api.grouper;

// todo javadoc
public class SimilarityValue<T> {

  private final T error1;
  private final T error2;
  private final int value;

  public SimilarityValue(T error1, T error2, int value) {
    this.error1 = error1;
    this.error2 = error2;
    this.value = value;
  }
}
