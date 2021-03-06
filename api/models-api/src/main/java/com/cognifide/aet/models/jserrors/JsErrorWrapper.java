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
package com.cognifide.aet.models.jserrors;

import com.cognifide.aet.models.ErrorWrapper;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Set;

public class JsErrorWrapper implements ErrorWrapper {

  public static final Type ARTIFACT_TYPE = new TypeToken<Set<JsErrorLog>>() {
  }.getType();

  private final Set<JsErrorLog> jsErrors;
  private final String urlName;

  public JsErrorWrapper(Set<JsErrorLog> jsErrors, String urlName) {
    this.jsErrors = jsErrors;
    this.urlName = urlName;
  }

  public Set<JsErrorLog> getJsErrors() {
    return jsErrors;
  }

  @Override
  public String getUrl() {
    return urlName;
  }
}
