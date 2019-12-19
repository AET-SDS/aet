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
package com.cognifide.aet.repositories;

import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.models.ErrorType;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Set;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = GroupsRepository.class, immediate = true)
public class GroupsRepository implements Serializable {

  private static final Type JS_ERRORS_GROUPS_TYPE =
      new TypeToken<Set<Set<JsErrorLog>>>() {}.getType();

  private static final long serialVersionUID = 5458727119962296016L;

  @Reference private ArtifactsDAO artifactsDAO;

  public Set<Set<?>> processGroups(ErrorType errorType, DBKey dbKey, String artifactId) {
    try {
      switch (errorType) {
        case JS_ERRORS:
          return artifactsDAO.getJsonFormatArtifact(dbKey, artifactId, JS_ERRORS_GROUPS_TYPE);
        default:
          throw new IllegalArgumentException();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
