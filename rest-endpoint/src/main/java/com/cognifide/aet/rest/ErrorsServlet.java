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

import com.cognifide.aet.communication.api.metadata.Test;
import com.cognifide.aet.models.ErrorType;
import com.cognifide.aet.models.ErrorWrapper;
import com.cognifide.aet.services.ErrorsService;
import com.cognifide.aet.vs.DBKey;
import com.cognifide.aet.vs.MetadataDAO;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

@Component(immediate = true)
public class ErrorsServlet extends BasicTestDataServlet {

  private static final long serialVersionUID = 4312853975173807071L;

  @Reference
  private MetadataDAO metadataDAO;
  @Reference
  private ErrorsService errorsService;
  @Reference
  private transient HttpService httpService;

  @Override
  protected String getJsonResponse(String errorType, Test test, DBKey dbKey) {
    Map<ErrorType, List<ErrorWrapper>> errorsMap = errorsService
        .getErrorsFromTest(test, dbKey, errorType);
    return GSON.toJson(errorsMap);
  }

  @Override
  protected MetadataDAO getMetadataDAO() {
    return metadataDAO;
  }

  @Override
  protected HttpService getHttpService() {
    return httpService;
  }

  @Override
  protected void setHttpService(HttpService httpService) {
    this.httpService = httpService;
  }

  @Activate
  public void start() {
    register(Helper.getErrorsPath());
  }

  @Deactivate
  public void stop() {
    unregister(Helper.getErrorsPath());
  }
}
