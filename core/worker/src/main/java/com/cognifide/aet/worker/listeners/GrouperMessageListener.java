package com.cognifide.aet.worker.listeners;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.queues.JmsConnection;
import com.cognifide.aet.queues.JmsUtils;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import javax.jms.JMSException;
import javax.jms.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GrouperMessageListener extends WorkerMessageListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperMessageListener.class);

  private final GrouperDispatcher grouperDispatcher;

  GrouperMessageListener(
      String name,
      JmsConnection jmsConnection,
      String consumerQueueName,
      String producerQueueName,
      GrouperDispatcher grouperDispatcher) {
    super(name, jmsConnection, consumerQueueName, producerQueueName);
    this.grouperDispatcher = grouperDispatcher;
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
    LOGGER.error(grouperJobData.getComparisonResult().getStepResult().getArtifactId());
    // todo null when comparatorstepresult.status = passed
  }
}
