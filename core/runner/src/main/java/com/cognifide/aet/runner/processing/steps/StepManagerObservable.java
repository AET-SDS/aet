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
