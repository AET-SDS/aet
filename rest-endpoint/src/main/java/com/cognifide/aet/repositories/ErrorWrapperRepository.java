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
package com.cognifide.aet.repositories;

import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.job.common.comparators.accessibility.report.AccessibilityReport;
import com.cognifide.aet.job.common.comparators.accessibility.report.AccessibilityReportConfiguration;
import com.cognifide.aet.job.common.comparators.cookie.CookieCompareComparatorResult;
import com.cognifide.aet.job.common.comparators.cookie.CookieTestComparatorResult;
import com.cognifide.aet.job.common.comparators.source.diff.ResultDelta;
import com.cognifide.aet.job.common.comparators.statuscodes.StatusCodesComparatorResult;
import com.cognifide.aet.job.common.comparators.w3chtml5.W3cHtml5ComparatorResult;
import com.cognifide.aet.models.AccessibilityErrorWrapper;
import com.cognifide.aet.models.CookieErrorWrapper;
import com.cognifide.aet.models.ErrorType;
import com.cognifide.aet.models.ErrorWrapper;
import com.cognifide.aet.models.JsErrorWrapper;
import com.cognifide.aet.models.ScreenErrorWrapper;
import com.cognifide.aet.models.SourceErrorWrapper;
import com.cognifide.aet.models.StatusCodesErrorWrapper;
import com.cognifide.aet.models.W3cHtml5ErrorWrapper;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ErrorWrapperRepository.class, immediate = true)
public class ErrorWrapperRepository implements Serializable {

  private static final long serialVersionUID = -565635532690309676L;

  @Reference
  private ArtifactsDAO artifactsDAO;

  public ErrorWrapper processError(ErrorType errorType, Comparator comparator, DBKey dbKey,
      String urlName, String stepName) {
    try {
      switch (errorType) {
        case JS_ERRORS:
          return processJsErrors(comparator, dbKey, urlName);
        case STATUS_CODES:
          return processStatusCodesErrors(comparator, dbKey, urlName);
        case ACCESSIBILITY:
          return processAccessibilityErrors(comparator, dbKey, urlName);
        case SCREEN:
          return processScreenErrors(comparator, urlName, stepName);
        case COOKIE:
          return processCookieErrors(comparator, dbKey, urlName);
        case SOURCE:
          return processSourceErrors(comparator, dbKey, urlName);
        default:
          throw new IllegalArgumentException();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ErrorWrapper processJsErrors(Comparator comparator, DBKey dbKey, String urlName)
      throws IOException {
    Set<JsErrorLog> jsErrors = artifactsDAO.getJsonFormatArtifact(dbKey,
        comparator.getStepResult().getArtifactId(), JsErrorWrapper.ARTIFACT_TYPE);

    return new JsErrorWrapper(jsErrors, urlName);
  }

  private ErrorWrapper processStatusCodesErrors(Comparator comparator, DBKey dbKey, String urlName)
      throws IOException {
    StatusCodesComparatorResult comparatorResult = artifactsDAO.getJsonFormatArtifact(dbKey,
        comparator.getStepResult().getArtifactId(), StatusCodesErrorWrapper.ARTIFACT_TYPE);

    return new StatusCodesErrorWrapper(comparatorResult, urlName);
  }

  private ErrorWrapper processAccessibilityErrors(Comparator comparator, DBKey dbKey,
      String urlName) throws IOException {
    AccessibilityReport accessibilityReport = artifactsDAO.getJsonFormatArtifact(dbKey,
        comparator.getStepResult().getArtifactId(), AccessibilityErrorWrapper.ARTIFACT_TYPE);
    Map<String, String> params = new HashMap<>();
    AccessibilityReportConfiguration config = new AccessibilityReportConfiguration(params);

    return new AccessibilityErrorWrapper(accessibilityReport, config, urlName);
  }

  private ErrorWrapper processScreenErrors(Comparator comparator, String urlName, String stepName) {
    return new ScreenErrorWrapper(stepName, comparator.getStepResult().getData(), urlName);
  }

  private ErrorWrapper processCookieErrors(Comparator comparator, DBKey dbKey, String urlName)
      throws IOException {
    String action = comparator.getParameters().get(CookieErrorWrapper.ACTION_PARAM);

    if (action.equals(CookieErrorWrapper.ACTION_COMPARE)) {
      CookieCompareComparatorResult result = artifactsDAO.getJsonFormatArtifact(dbKey,
          comparator.getStepResult().getArtifactId(),
          CookieErrorWrapper.ARTIFACT_COOKIE_COMPARE_TYPE);
      return new CookieErrorWrapper(result, urlName);
    } else if (action.equals(CookieErrorWrapper.ACTION_TEST)) {
      CookieTestComparatorResult result = artifactsDAO.getJsonFormatArtifact(dbKey,
          comparator.getStepResult().getArtifactId(), CookieErrorWrapper.ARTIFACT_COOKIE_TEST_TYPE);
      return new CookieErrorWrapper(result, urlName);
    }

    throw new IllegalArgumentException();
  }

  private ErrorWrapper processSourceErrors(Comparator comparator, DBKey dbKey, String urlName)
      throws IOException {
    String comparatorType = comparator.getParameters().get(Comparator.COMPARATOR_PARAMETER);
    if (comparatorType.equals(W3cHtml5ErrorWrapper.COMPARATOR_TYPE)) {
      W3cHtml5ComparatorResult result = artifactsDAO.getJsonFormatArtifact(dbKey,
          comparator.getStepResult().getArtifactId(), W3cHtml5ErrorWrapper.ARTIFACT_TYPE);
      return new W3cHtml5ErrorWrapper(result, urlName);

    } else if (comparatorType.equals(SourceErrorWrapper.COMPARATOR_TYPE)) {
      Map<String, List<ResultDelta>> result = artifactsDAO.getJsonFormatArtifact(dbKey,
          comparator.getStepResult().getArtifactId(), SourceErrorWrapper.ARTIFACT_TYPE);

      return new SourceErrorWrapper(result, urlName, comparator.getStepResult().getData());
    }

    throw new IllegalArgumentException();
  }
}
