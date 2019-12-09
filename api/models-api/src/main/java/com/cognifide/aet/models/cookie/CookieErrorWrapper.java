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
package com.cognifide.aet.models.cookie;

import com.cognifide.aet.models.ErrorWrapper;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class CookieErrorWrapper implements ErrorWrapper {

  public static final String ACTION_PARAM = "action";
  public static final String ACTION_COMPARE = "compare";
  public static final String ACTION_TEST = "test";

  public static final Type ARTIFACT_COOKIE_COMPARE_TYPE =
      new TypeToken<CookieCompareComparatorResult>() {
      }.getType();
  public static final Type ARTIFACT_COOKIE_TEST_TYPE =
      new TypeToken<CookieTestComparatorResult>() {
      }.getType();

  private final CookieComparatorResult result;
  private final String urlName;

  public CookieErrorWrapper(CookieComparatorResult result, String urlName) {
    this.result = result;
    this.urlName = urlName;
  }

  public CookieComparatorResult getResult() {
    return result;
  }

  @Override
  public String getUrl() {
    return urlName;
  }
}
