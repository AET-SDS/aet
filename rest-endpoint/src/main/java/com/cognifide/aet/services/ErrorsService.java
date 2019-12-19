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
package com.cognifide.aet.services;

import com.cognifide.aet.communication.api.metadata.Comparator;
import com.cognifide.aet.communication.api.metadata.ComparatorStepResult.Status;
import com.cognifide.aet.communication.api.metadata.Step;
import com.cognifide.aet.communication.api.metadata.Test;
import com.cognifide.aet.communication.api.metadata.Url;
import com.cognifide.aet.repositories.ErrorWrapperRepository;
import com.cognifide.aet.models.ErrorType;
import com.cognifide.aet.models.ErrorWrapper;
import com.cognifide.aet.models.W3cHtml5ErrorWrapper;
import com.cognifide.aet.vs.DBKey;
import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ErrorsService.class, immediate = true)
public class ErrorsService implements Serializable {

  private static final long serialVersionUID = 4620465433359434447L;

  @Reference
  private ErrorWrapperRepository errorWrapperRepository;

  public Map<ErrorType, List<ErrorWrapper>> getErrorsFromTest(Test test, DBKey dbKey,
      String errorType) {
    Map<ErrorType, List<ErrorWrapper>> errorsMap = new HashMap<>();

    for (Url url : test.getUrls()) {
      Map<ErrorType, List<ErrorWrapper>> urlErrorsMap = processUrl(url, dbKey, errorType);
      mergeMap(errorsMap, urlErrorsMap);
    }

    return errorsMap;
  }

  private Map<ErrorType, List<ErrorWrapper>> processUrl(Url url, DBKey dbKey, String errorType) {
    Stream<Step> stepsToProcess;
    if (!Strings.isNullOrEmpty(errorType)) {
      stepsToProcess = url.getSteps().stream()
          .filter(s -> s.getType().equalsIgnoreCase(errorType));
    } else {
      stepsToProcess = url.getSteps().stream();
    }

    return stepsToProcess
        .flatMap(step -> processStep(step, dbKey, step.getType(), url.getName()).stream())
        .collect(Collectors
            .groupingBy(Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));

  }

  private List<Pair<ErrorType, ErrorWrapper>> processStep(Step step, DBKey dbKey, String errorType,
      String urlName) {
    List<Pair<ErrorType, ErrorWrapper>> pairList = new ArrayList<>();
    if (step.getComparators() == null || step.getComparators().isEmpty()) {
      return pairList;
    }

    for (Comparator comparator : step.getComparators()) {
      if (comparator.getStepResult().getStatus() == Status.PASSED) {
        continue;
      }
      ErrorType type = ErrorType.byStringType(errorType);
      ErrorWrapper errorWrapper = errorWrapperRepository
          .processError(type, comparator, dbKey, urlName,
          step.getName());
      if (type == ErrorType.SOURCE) {
        type = comparator.getParameters().get(Comparator.COMPARATOR_PARAMETER).equals(
            W3cHtml5ErrorWrapper.COMPARATOR_TYPE) ? ErrorType.SOURCE_W3CHTML5 : ErrorType.SOURCE;
      }
      pairList.add(Pair.of(type, errorWrapper));
    }

    return pairList;
  }

  private void mergeMap(Map<ErrorType, List<ErrorWrapper>> errorsMap,
      Map<ErrorType, List<ErrorWrapper>> urlErrorsMap) {
    urlErrorsMap.forEach((type, list) -> errorsMap.merge(type, new ArrayList<>(list),
        (oldList, newList) -> {
          oldList.addAll(newList);
          return oldList;
        }));
  }
}
