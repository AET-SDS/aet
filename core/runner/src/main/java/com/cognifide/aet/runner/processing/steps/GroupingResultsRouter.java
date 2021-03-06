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

package com.cognifide.aet.runner.processing.steps;

import com.cognifide.aet.communication.api.ProcessingError;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Suite;
import com.cognifide.aet.communication.api.metadata.Test;
import com.cognifide.aet.communication.api.queues.JmsConnection;
import com.cognifide.aet.communication.api.queues.QueuesConstant;
import com.cognifide.aet.runner.RunnerConfiguration;
import com.cognifide.aet.runner.processing.TimeoutWatch;
import com.cognifide.aet.runner.processing.data.wrappers.RunIndexWrapper;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupingResultsRouter extends StepManager implements ChangeObserver, TaskFinishPoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(GroupingResultsRouter.class);
  private static final String STEP_NAME = "GROUPED";
  private static final String MODULE_NAME = "grouping";

  private final Suite suite;
  private boolean comparisonCompleted = false;

  public GroupingResultsRouter(
      TimeoutWatch timeoutWatch,
      JmsConnection jmsConnection,
      RunnerConfiguration runnerConfiguration,
      RunIndexWrapper<?> runIndexWrapper,
      int messagesToReceive)
      throws JMSException {
    super(
        timeoutWatch,
        jmsConnection,
        runIndexWrapper.get().getCorrelationId(),
        runnerConfiguration.getMttl());
    this.suite = runIndexWrapper.get().getRealSuite();
    this.messagesToReceive.set(messagesToReceive);
  }

  @Override
  public void updateAmountToReceive(int additionalCount) {
    // not used
  }

  @Override
  public void informChangesCompleted() {
    comparisonCompleted = true;
    logIfFinished();
  }

  @Override
  public void onMessage(Message message) {
    if (!(message instanceof ObjectMessage)) {
      return;
    }
    timeoutWatch.update();
    try {
      GrouperResultData grouperResultData =
          (GrouperResultData) ((ObjectMessage) message).getObject();
      updateCounters(grouperResultData.getJobStatus());
      saveGrouperResult(grouperResultData);
    } catch (JMSException e) {
      LOGGER.error("Error while grouping. CorrelationId: {}", correlationId, e);
      onError(ProcessingError.comparingError(e.getMessage()));
    } finally {
      logIfFinished();
    }
  }

  private void saveGrouperResult(GrouperResultData grouperResultData) {
    Test test = suite.getTest(grouperResultData.getTestName()).get();
    test.addGrouperResult(grouperResultData.getGrouperType(), grouperResultData.getArtifactId());
  }

  @Override
  public boolean isFinished() {
    return comparisonCompleted
        && messagesToReceive.get() == messagesReceivedSuccess.get() + messagesReceivedFailed.get();
  }

  @Override
  protected String getQueueInName() {
    return QueuesConstant.GROUPER.getResultsQueueName();
  }

  @Override
  protected String getQueueOutName() {
    return null;
  }

  @Override
  protected String getStepName() {
    return STEP_NAME;
  }

  @Override
  protected String getModuleNameForTimer() {
    return MODULE_NAME;
  }

  private void logIfFinished() {
    if (isFinished()) {
      timer.finishAndLog(suite.getName());
      LOGGER.info(
          "Grouping stage finished (received {} messages). CorrelationId: {}",
          messagesToReceive.get(),
          correlationId);
    }
  }
}
