package com.cognifide.aet.runner.processing.steps;

import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.queues.JmsConnection;
import com.cognifide.aet.communication.api.queues.QueuesConstant;
import com.cognifide.aet.runner.RunnerConfiguration;
import com.cognifide.aet.runner.processing.TimeoutWatch;
import com.cognifide.aet.runner.processing.data.wrappers.RunIndexWrapper;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

public class GroupingResultsRouter extends StepManager implements ChangeObserver, TaskFinishPoint {

  private static final String STEP_NAME = "GROUPED";

  private static final String MODULE_NAME = "grouping";

  private final String taskName;

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
    this.taskName = runIndexWrapper.get().getRealSuite().getName();
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
      // todo save artifactid to metadata
      updateCounters(grouperResultData.getJobStatus());
    } catch (JMSException e) {
      e.printStackTrace(); // todo
    }
    if (isFinished()) {
      timer.finishAndLog(taskName);
    }
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
}
