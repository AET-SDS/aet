package com.cognifide.aet.runner.processing.steps;

import com.cognifide.aet.communication.api.queues.JmsConnection;
import com.cognifide.aet.communication.api.queues.QueuesConstant;
import com.cognifide.aet.runner.RunnerConfiguration;
import com.cognifide.aet.runner.processing.TimeoutWatch;
import com.cognifide.aet.runner.processing.data.wrappers.RunIndexWrapper;
import javax.jms.JMSException;
import javax.jms.Message;

public class GroupingResultsRouter extends StepManager implements ChangeObserver, TaskFinishPoint {

  private static final String STEP_NAME = "GROUPED";

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
  }

  @Override
  public void updateAmountToReceive(int additionalCount) {
    messagesToReceive.addAndGet(additionalCount);
  }

  @Override
  public void informChangesCompleted() {
    comparisonCompleted = true;
  }

  @Override
  public void onMessage(Message message) {}

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
}