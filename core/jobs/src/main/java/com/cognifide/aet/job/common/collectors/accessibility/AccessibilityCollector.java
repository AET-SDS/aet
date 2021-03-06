/**
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
package com.cognifide.aet.job.common.collectors.accessibility;

import com.cognifide.aet.communication.api.metadata.CollectorStepResult;
import com.cognifide.aet.job.api.collector.CollectorJob;
import com.cognifide.aet.job.api.collector.CollectorProperties;
import com.cognifide.aet.job.api.exceptions.ParametersException;
import com.cognifide.aet.job.api.exceptions.ProcessingException;
import com.cognifide.aet.job.common.utils.javascript.JavaScriptJobExecutor;
import com.cognifide.aet.models.accessibility.AccessibilityIssue;
import com.cognifide.aet.vs.ArtifactsDAO;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessibilityCollector implements CollectorJob {

  public static final String NAME = "accessibility";

  private static final String DOCUMENT_OUTER_HTML_SCRIPT = "return document.documentElement.outerHTML;";
  private static final String PARAM_STANDARD = "standard";
  private static final String DEFAULT_STANDARD = "WCAG2AA";

  private static final Logger LOGGER = LoggerFactory.getLogger(AccessibilityCollector.class);

  private final ArtifactsDAO artifactsDAO;
  private final BundleContext context;
  private final CollectorProperties properties;
  private final JavaScriptJobExecutor jsExecutor;

  private String standard = DEFAULT_STANDARD;

  AccessibilityCollector(ArtifactsDAO artifactsDAO, CollectorProperties collectorProperties,
      JavaScriptJobExecutor jsExecutor, BundleContext context) {
    this.artifactsDAO = artifactsDAO;
    this.context = context;
    this.properties = collectorProperties;
    this.jsExecutor = jsExecutor;
  }

  @Override
  public CollectorStepResult collect() throws ProcessingException {
    String script = getScriptFromFile();
    LOGGER.debug("Executing Accessibility Collector");
    final String html = jsExecutor.execute(DOCUMENT_OUTER_HTML_SCRIPT)
        .getExecutionResultAsString();
    final String json = jsExecutor.execute(script, standard).getExecutionResultAsString();
    List<AccessibilityIssue> issues = new AccessibilityIssueMarkupFinder(html, json).get();

    String resultId = artifactsDAO.saveArtifactInJsonFormat(properties, issues);

    return CollectorStepResult.newCollectedResult(resultId);
  }

  private String getScriptFromFile() throws ProcessingException {
    String script;
    try {
      URL entry = context.getBundle().getEntry("/collectors/accessibility/htmlcs.min.js");
      if (entry != null) {
        script = IOUtils.toString(entry.openStream(), StandardCharsets.UTF_8);
        script = "return " + script;
      } else {
        throw new ProcessingException("Can't find accessibility htmlcs scripts in bundle context!");
      }
    } catch (Exception e) {
      throw new ProcessingException(e.getMessage(), e);
    }
    return script;
  }

  @Override
  public void setParameters(Map<String, String> params) throws ParametersException {
    if (params.containsKey(PARAM_STANDARD)) {
      standard = params.get(PARAM_STANDARD);
    }
  }
}
