/*
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cognifide.aet.job.common.groupers.jserrors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.cognifide.aet.communication.api.SuiteComparatorsCount;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.ComparatorStepResult;
import com.cognifide.aet.communication.api.metadata.ComparatorStepResult.Status;
import com.cognifide.aet.job.api.collector.JsErrorLog;
import com.cognifide.aet.job.api.grouper.SimilarityValue;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import com.cognifide.aet.vs.SimpleDBKey;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.Strings;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsErrorsGrouperTest {

  @Mock private ArtifactsDAO artifactsDao;
  private final DBKey dbKey = new SimpleDBKey("company", "project");
  private final Type inputArtifactType = new TypeToken<Set<JsErrorLog>>() {}.getType();
  private JsErrorsGrouper jsErrorsGrouper;

  @Before
  public void setup() throws IOException {
    when(artifactsDao.getJsonFormatArtifact(any(), any(), any())).thenReturn(Sets.newHashSet());
    when(artifactsDao.saveArtifactInJsonFormat(any(), any())).thenReturn("outputArtifactId");
  }

  @Test
  public void group_whenInputArtifactIdNull_expectNoCallToArtifactsDao() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(2));
    GrouperJobData grouperJobData = newGrouperJobData(null);
    jsErrorsGrouper.group(grouperJobData);
    verifyZeroInteractions(artifactsDao);
  }

  @Test
  public void group_whenInputArtifactIdEmpty_expectNoCallToArtifactsDao() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(2));
    GrouperJobData grouperJobData = newGrouperJobData("");
    jsErrorsGrouper.group(grouperJobData);
    verifyZeroInteractions(artifactsDao);
  }

  @Test
  public void group_whenInputArtifactIdIsNotEmpty_expectOneCallToArtifactsDao() throws IOException {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(2));
    GrouperJobData grouperJobData = newGrouperJobData("artifactId");
    jsErrorsGrouper.group(grouperJobData);
    verify(artifactsDao, times(1)).getJsonFormatArtifact(dbKey, "artifactId", inputArtifactType);
  }

  @Test
  public void group_whenIsLastExpectedMessage_expectSaveToArtifactsDao() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(1));
    GrouperJobData grouperJobData = newGrouperJobData("");
    jsErrorsGrouper.group(grouperJobData);
    verify(artifactsDao, times(1)).saveArtifactInJsonFormat(dbKey, Sets.newHashSet());
  }

  @Test
  public void group_whenIsLastExpectedMessage_expectResultIsReadyTrue() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(1));
    GrouperJobData grouperJobData = newGrouperJobData("");
    GrouperResultData result = jsErrorsGrouper.group(grouperJobData);
    assertTrue(result.isReady());
  }

  @Test
  public void group_whenIsLastExpectedMessage_expectOutputArtifactIdNotNullNorEmpty() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(1));
    GrouperJobData grouperJobData = newGrouperJobData("");
    GrouperResultData result = jsErrorsGrouper.group(grouperJobData);
    assertFalse(Strings.isNullOrEmpty(result.getArtifactId()));
  }

  @Test
  public void group_whenIsNotLastExpectedMessage_expectNoCallToArtifactsDao() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(2));
    GrouperJobData grouperJobData = newGrouperJobData("");
    jsErrorsGrouper.group(grouperJobData);
    verifyZeroInteractions(artifactsDao);
  }

  @Test
  public void group_whenIsNotLastExpectedMessage_expectResultIsReadyFalse() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(2));
    GrouperJobData grouperJobData = newGrouperJobData("");
    GrouperResultData result = jsErrorsGrouper.group(grouperJobData);
    assertFalse(result.isReady());
  }

  @Test
  public void group_whenIsNotLastExpectedMessage_expectOutputArtifactIdNullOrEmpty() {
    jsErrorsGrouper = new JsErrorsGrouper(artifactsDao, dbKey, new AtomicLong(2));
    GrouperJobData grouperJobData = newGrouperJobData("");
    GrouperResultData result = jsErrorsGrouper.group(grouperJobData);
    assertTrue(Strings.isNullOrEmpty(result.getArtifactId()));
  }

  private GrouperJobData newGrouperJobData(String inputArtifactId) {
    return new GrouperJobData(
        "company",
        "project",
        "suiteName",
        "testName",
        SuiteComparatorsCount.of(Lists.newArrayList()),
        new ComparatorStepResult(inputArtifactId, Status.PASSED),
        "comparatorType");
  }
}
