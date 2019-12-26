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
package com.cognifide.aet.runner.processing.steps;

import com.cognifide.aet.communication.api.JobStatus;
import com.cognifide.aet.communication.api.ProcessingError;
import com.cognifide.aet.communication.api.SuiteComparatorsCount;
import com.cognifide.aet.communication.api.job.ComparatorResultData;
import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.metadata.Step;
import com.cognifide.aet.communication.api.metadata.Test;
import com.cognifide.aet.communication.api.metadata.Url;
import com.cognifide.aet.communication.api.queues.JmsConnection;
import com.cognifide.aet.communication.api.queues.QueuesConstant;
import com.cognifide.aet.runner.RunnerConfiguration;
import com.cognifide.aet.runner.processing.TimeoutWatch;
import com.cognifide.aet.runner.processing.data.wrappers.RunIndexWrapper;
import java.util.List;
import java.util.Optional;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComparisonResultsRouter extends StepManagerObservable
    implements ChangeObserver, TaskFinishPoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(ComparisonResultsRouter.class);

  private static final String STEP_NAME = "COMPARED";

  private static final String MODULE_NAME = "comparison";

  private final RunIndexWrapper<?> runIndexWrapper;

  private boolean collectingFinished;

  private boolean aborted;

  public ComparisonResultsRouter(TimeoutWatch timeoutWatch, JmsConnection jmsConnection,
      RunnerConfiguration runnerConfiguration, RunIndexWrapper runIndexWrapper) throws JMSException {
    super(timeoutWatch, jmsConnection, runIndexWrapper.get().getCorrelationId(),
        runnerConfiguration.getMttl());
    this.runIndexWrapper = runIndexWrapper;
  }

  @Override
  public void updateAmountToReceive(int additionalCount) {
    messagesToReceive.addAndGet(additionalCount);
  }

  @Override
  public void informChangesCompleted() {
    collectingFinished = true;
  }

  @Override
  public void onMessage(Message message) {
    if (message instanceof ObjectMessage) {
      timeoutWatch.update();
      try {
        ComparatorResultData comparatorResultData = (ComparatorResultData) ((ObjectMessage) message)
            .getObject();

        updateCounters(comparatorResultData.getStatus());
        LOGGER.info("Compare result message (ID: {}) {}! Results received successful {}, " +
                "failed {} of {} total. CorrelationId: {}",
            message.getJMSMessageID(), comparatorResultData, messagesReceivedSuccess.get(),
            messagesReceivedFailed.get(), getTotalTasksCount(), correlationId);

        addComparatorToSuite(comparatorResultData);
        sendGrouperJobData(comparatorResultData);
        if (comparatorResultData.getStatus() != JobStatus.SUCCESS) {
          onError(comparatorResultData.getProcessingError());
        }
      } catch (JMSException e) {
        LOGGER.error("Error while collecting results in CollectionResultsRouter. CorrelationId: {}",
            correlationId, e);
        onError(ProcessingError.comparingError(e.getMessage()));
      } finally {
        if (isFinished()) {
          notifyCompleted();
          timer.finishAndLog(runIndexWrapper.get().getRealSuite().getName());
          LOGGER.info(
              "Comparison stage finished (received {} messages). CorrelationId: {}",
              messagesToReceive.get(),
              correlationId);
        }
      }
    }
  }

  private void sendGrouperJobData(ComparatorResultData comparatorResultData) throws JMSException {
    List<Test> tests = runIndexWrapper.get().getRealSuite().getTests();
    SuiteComparatorsCount suiteComparatorsCount = SuiteComparatorsCount.of(tests);
    LOGGER.info("map: {}", suiteComparatorsCount.abc());
    GrouperJobData grouperJobData =
        new GrouperJobData(
            runIndexWrapper.get().getCompany(),
            runIndexWrapper.get().getProject(),
            runIndexWrapper.get().getName(),
            comparatorResultData.getTestName(),
            suiteComparatorsCount, // todo too much data being sent?
            comparatorResultData.getComparisonResult().getStepResult(),
            comparatorResultData.getComparisonResult().getType());
    ObjectMessage message = session.createObjectMessage(grouperJobData);
    message.setJMSCorrelationID(correlationId);
    sender.send(message);
  }

  private void addComparatorToSuite(ComparatorResultData comparisonResult) {
    final Optional<Url> urlOptional = runIndexWrapper
        .getTestUrl(comparisonResult.getTestName(), comparisonResult.getUrlName());
    if (urlOptional.isPresent()) {
      final Url url = urlOptional.get();
      final Step step = url.getSteps().get(comparisonResult.getStepIndex());
      if (step != null) {
        step.addComparator(comparisonResult.getComparisonResult());
      } else {
        LOGGER.error("Fatal error while saving comparison result: {} of suite.", comparisonResult,
            runIndexWrapper.get().getCorrelationId());
      }
    }
  }

  private boolean allResultsReceived() {
    return collectingFinished
        && messagesToReceive.get() == messagesReceivedSuccess.get() + messagesReceivedFailed.get();
  }

  /**
   * This task is finished, when all comparisons are received or was aborted
   */
  @Override
  public boolean isFinished() {
    return aborted || allResultsReceived();
  }

  public void abort() {
    if (!isFinished()) {
      LOGGER.warn("Suite {} aborted!. Still waiting for {} of {} comparisons!",
          runIndexWrapper.get().getCorrelationId(),
          messagesToReceive.get() - messagesReceivedSuccess.get() - messagesReceivedFailed.get(),
          messagesToReceive.get());
    }
    aborted = true;
  }

  @Override
  protected String getQueueInName() {
    return QueuesConstant.COMPARATOR.getResultsQueueName();
  }

  @Override
  protected String getQueueOutName() {
    return QueuesConstant.GROUPER.getJobsQueueName();
  }

  @Override
  protected String getStepName() {
    return STEP_NAME;
  }

  @Override
  protected String getModuleNameForTimer() {
    return MODULE_NAME;
  }
}
