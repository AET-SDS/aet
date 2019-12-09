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
package com.cognifide.aet.models.w3chtml5;

import com.cognifide.aet.models.ErrorWrapper;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class W3cHtml5ErrorWrapper extends W3cHtml5ComparatorResult implements ErrorWrapper {

  private static final long serialVersionUID = 8780994408203761214L;

  public static final String COMPARATOR_TYPE = "w3c-html5";
  public static final Type ARTIFACT_TYPE = new TypeToken<W3cHtml5ComparatorResult>() {
  }.getType();

  private final String urlName;

  public W3cHtml5ErrorWrapper(W3cHtml5ComparatorResult result, String urlName) {
    this(
        result.getErrorsCount(),
        result.getWarningsCount(),
        result.getIssues(),
        result.getExcludedIssues(),
        urlName);
  }

  private W3cHtml5ErrorWrapper(
      int errorCount,
      int warningCount,
      List<W3cHtml5Issue> issues,
      List<W3cHtml5Issue> excludedIssues,
      String urlName) {
    super(errorCount, warningCount, issues, excludedIssues);
    this.urlName = urlName;
  }

  @Override
  public String getUrl() {
    return urlName;
  }
}
