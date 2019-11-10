package com.cognifide.aet.runner.processing.steps;

import com.cognifide.aet.communication.api.queues.JmsConnection;
import com.cognifide.aet.runner.processing.TimeoutWatch;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.jms.JMSException;

public abstract class StepManagerObservable extends StepManager {

  private final List<ChangeObserver> changeObservers;

  public StepManagerObservable(
      TimeoutWatch timeoutWatch,
      JmsConnection jmsConnection,
      String correlationId,
      long messageTimeToLive)
      throws JMSException {
    super(timeoutWatch, jmsConnection, correlationId, messageTimeToLive);
    this.changeObservers = new CopyOnWriteArrayList<>();
  }

  public void addChangeObserver(ChangeObserver observer) {
    changeObservers.add(observer);
  }

  protected void notifyCompleted() {
    changeObservers.forEach(ChangeObserver::informChangesCompleted);
  }

  protected void notifyMessagesCount(int messagesCount) {
    changeObservers.forEach(it -> it.updateAmountToReceive(messagesCount));
  }
}
