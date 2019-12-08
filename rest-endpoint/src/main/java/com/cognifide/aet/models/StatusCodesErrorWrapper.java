/**
 * AET
 * <p>
 * Copyright (C) 2013 Cognifide Limited
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.cognifide.aet.models;

import com.cognifide.aet.job.common.collectors.statuscodes.StatusCode;
import com.cognifide.aet.job.common.comparators.statuscodes.StatusCodesComparatorResult;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class StatusCodesErrorWrapper extends StatusCodesComparatorResult implements ErrorWrapper {

  private static final long serialVersionUID = -6582956951044747903L;

  public static final Type ARTIFACT_TYPE = new TypeToken<StatusCodesComparatorResult>() {
  }.getType();

  private final String urlName;

  public StatusCodesErrorWrapper(StatusCodesComparatorResult result, String urlName) {
    this(result.getStatusCodes(), result.getFilteredStatusCodes(), result.getExcludedStatusCodes(),
        urlName);
  }

  private StatusCodesErrorWrapper(
      List<StatusCode> statusCodes, List<StatusCode> filteredStatusCodes,
      List<StatusCode> excludedStatusCodes, String urlName) {
    super(statusCodes, filteredStatusCodes, excludedStatusCodes);
    this.urlName = urlName;
  }

  @Override
  public String getUrl() {
    return urlName;
  }
}
