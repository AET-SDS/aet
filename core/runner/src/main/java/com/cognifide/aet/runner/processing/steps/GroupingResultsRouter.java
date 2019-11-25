package com.cognifide.aet.runner.processing.steps;

import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.metadata.Statistics;
import com.cognifide.aet.communication.api.metadata.Suite;
import com.cognifide.aet.communication.api.metadata.Suite.Timestamp;
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
      RunIndexWrapper runIndexWrapper)
      throws JMSException {
    super(
        timeoutWatch,
        jmsConnection,
        runIndexWrapper.get().getCorrelationId(),
        runnerConfiguration.getMttl());
    this.suite = runIndexWrapper.get().getRealSuite();
    this.messagesToReceive.set(runIndexWrapper.getUsedComparators().size());
  }

  @Override
  public void updateAmountToReceive(int additionalCount) {
    // not used
  }

  @Override
  public void informChangesCompleted() {
    comparisonCompleted = true;
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
      e.printStackTrace(); // todo
    } finally {
      if (isFinished()) {
        LOGGER.info(
            "All results received ({})! Persisting metadata. CorrelationId: {}",
            messagesToReceive.get(),
            correlationId);
        timer.finishAndLog(suite.getName());
        // todo move this responsibility somewhere else
        suite.setFinishedTimestamp(new Timestamp(System.currentTimeMillis()));
        long delta = suite.getFinishedTimestamp().get() - suite.getRunTimestamp().get();
        suite.setStatistics(new Statistics(delta));
      }
    }
  }

  private void saveGrouperResult(GrouperResultData grouperResultData) {
    Test test = suite.getTest(grouperResultData.getTestName()).get();
    test.addGrouperResult(grouperResultData.getGrouperType(), grouperResultData.getArtifactId());
  }

  @Override
  public boolean isFinished() {
    // todo comparisonCompleted is possible to be set after receiving results
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
}
