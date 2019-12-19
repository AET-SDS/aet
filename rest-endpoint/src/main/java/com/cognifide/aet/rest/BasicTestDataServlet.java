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

package com.cognifide.aet.rest;

import static com.cognifide.aet.rest.Helper.isValidCorrelationId;
import static com.cognifide.aet.rest.Helper.responseAsJson;

import com.cognifide.aet.communication.api.metadata.Suite;
import com.cognifide.aet.communication.api.metadata.Test;
import com.cognifide.aet.vs.DBKey;
import com.cognifide.aet.vs.MetadataDAO;
import com.cognifide.aet.vs.StorageException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTestDataServlet extends BasicDataServlet {

  private static final long serialVersionUID = 5487677535408082436L;
  private static final Logger LOGGER = LoggerFactory.getLogger(BasicTestDataServlet.class);

  protected abstract String getJsonResponse(String errorType, Test test, DBKey dbKey);

  protected abstract MetadataDAO getMetadataDAO();

  @Override
  protected void process(DBKey dbKey, HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String correlationId = req.getParameter(Helper.CORRELATION_ID_PARAM);
    String testName = req.getParameter(Helper.TEST_RERUN_PARAM);

    Suite suite;
    try {
      if (isValidCorrelationId(correlationId)) {
        suite = getMetadataDAO().getSuite(dbKey, correlationId);
      } else {
        resp.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        resp.getWriter()
            .write(responseAsJson(GSON, "Invalid correlationId of suite was specified."));
        return;
      }
    } catch (StorageException e) {
      LOGGER.error("Failed to get suite", e);
      resp.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
      resp.getWriter().write(responseAsJson(GSON, "Failed to get suite %s", e.getMessage()));
      return;
    }

    if (suite != null) {
      Optional<Test> test = suite.getTests().stream()
          .filter(t -> t.getName().equals(testName)).findFirst();
      if (test.isPresent()) {
        String errorType = Helper.getErrorTypeFromRequest(req);

        String abc = getJsonResponse(errorType, test.get(), dbKey);

        resp.setContentType(Helper.APPLICATION_JSON_CONTENT_TYPE);
        resp.getWriter().write(abc);
      } else {
        Helper.createNotFoundTestResponse(resp, testName, dbKey);
      }
    } else {
      Helper.createNotFoundSuiteResponse(resp, correlationId, dbKey);
    }
  }
}
