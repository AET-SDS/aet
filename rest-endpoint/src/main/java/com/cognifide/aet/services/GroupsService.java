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
package com.cognifide.aet.services;

import com.cognifide.aet.communication.api.metadata.Test;
import com.cognifide.aet.repositories.GroupsRepository;
import com.cognifide.aet.models.ErrorType;
import com.cognifide.aet.vs.DBKey;
import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = GroupsService.class, immediate = true)
public class GroupsService implements Serializable {

  private static final long serialVersionUID = -1177482293065252166L;

  @Reference
  private GroupsRepository groupsRepository;

  public Map<ErrorType, Set<Set<?>>> getGroupsFromTest(Test test, DBKey dbKey, String errorType) {
    if (!Strings.isNullOrEmpty(errorType)) {
      return getGroupsOfSpecifiedErrorType(errorType, test.getGrouperResults(), dbKey);
    } else {
      return getGroupsOfAllErrorTypes(test.getGrouperResults(), dbKey);
    }
  }

  private Map<ErrorType, Set<Set<?>>> getGroupsOfSpecifiedErrorType(String errorType,
      Map<String, String> grouperResult, DBKey dbKey) {
    Map<ErrorType, Set<Set<?>>> groupsMap = new HashMap<>();

    String artifactId = grouperResult.get(errorType);
    if (!Strings.isNullOrEmpty(artifactId)) {
      putGroupsToMap(errorType, artifactId, dbKey, groupsMap);
    }

    return groupsMap;
  }

  private Map<ErrorType, Set<Set<?>>> getGroupsOfAllErrorTypes(Map<String, String> grouperResult,
      DBKey dbKey) {
    Map<ErrorType, Set<Set<?>>> groupsMap = new HashMap<>();

    for (Entry<String, String> entry : grouperResult.entrySet()) {
      putGroupsToMap(entry.getKey(), entry.getValue(), dbKey, groupsMap);
    }

    return groupsMap;
  }

  private void putGroupsToMap(String errorType, String artifactId, DBKey dbKey,
      Map<ErrorType, Set<Set<?>>> groupsMap) {
    ErrorType type = ErrorType.byStringType(errorType);
    groupsMap.put(type, groupsRepository.processGroups(type, dbKey, artifactId));
  }
}
