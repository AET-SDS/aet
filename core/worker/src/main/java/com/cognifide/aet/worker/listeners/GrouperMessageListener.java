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

package com.cognifide.aet.worker.listeners;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.communication.api.queues.JmsConnection;
import com.cognifide.aet.queues.JmsUtils;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import com.google.common.base.Strings;
import java.util.Objects;
import javax.jms.JMSException;
import javax.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GrouperMessageListener extends WorkerMessageListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperMessageListener.class);

  private final GrouperDispatcher dispatcher;

  GrouperMessageListener(
      String name,
      JmsConnection jmsConnection,
      String consumerQueueName,
      String producerQueueName,
      GrouperDispatcher dispatcher) {
    super(name, jmsConnection, consumerQueueName, producerQueueName);
    this.dispatcher = dispatcher;
  }

  @Override
  public void onMessage(Message message) {
    GrouperJobData grouperJobData = null;
    try {
      grouperJobData = JmsUtils.getFromMessage(message, GrouperJobData.class);
    } catch (JMSException e) {
      e.printStackTrace(); // todo
    }
    String jmsCorrelationId = JmsUtils.getJMSCorrelationID(message);

    if (Objects.isNull(grouperJobData) || Strings.isNullOrEmpty(jmsCorrelationId)) {
      return;
    }
    // todo artifactId null when comparatorstepresult.status = passed
    // LOGGER.error(grouperJobData.getComparisonResult().getStepResult().getArtifactId());
    GrouperResultData resultData = dispatcher.run(jmsCorrelationId, grouperJobData);
    if (resultData.isReady()) { // todo maybe when artifactid not null
      feedbackQueue.sendObjectMessageWithCorrelationID(resultData, jmsCorrelationId);
    }
  }
}
