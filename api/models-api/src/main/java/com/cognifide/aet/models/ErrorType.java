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

public enum ErrorType {
  JS_ERRORS("js-errors"),
  STATUS_CODES("status-codes"),
  ACCESSIBILITY("accessibility"),
  SCREEN("screen"),
  COOKIE("cookie"),
  COOKIE_COMPARE("cookie_compare"),
  COOKIE_TEST("cookie_test"),
  SOURCE("source"),
  SOURCE_W3CHTML5("source_w3c-html5"),
  UNKNOWN("");

  private final String errorType;

  ErrorType(String errorType) {
    this.errorType = errorType;
  }

  public String getErrorType() {
    return errorType;
  }

  public static ErrorType byStringType(String errorType) {
    for (ErrorType type : ErrorType.values()) {
      if (type.errorType.equalsIgnoreCase(errorType)) {
        return type;
      }
    }

    return UNKNOWN;
  }


  @Override
  public String toString() {
    return errorType;
  }
}
