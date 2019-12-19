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
package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.job.common.groupers.DistanceFunction;
import java.util.Objects;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class JsErrorsDistanceFunction implements DistanceFunction<JsErrorLog> {

  private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

  @Override
  public Double apply(JsErrorLog a, JsErrorLog b) {
    String s1 = Objects.requireNonNull(a.getErrorMessage());
    String s2 = Objects.requireNonNull(b.getErrorMessage());
    if (Objects.equals(s1, s2)) {
      return 0d;
    }
    int distance = levenshteinDistance.apply(s1, s2);
    int longer = Math.max(s1.length(), s2.length());
    return 1 - ((longer - distance) / (double) longer);
  }
}
