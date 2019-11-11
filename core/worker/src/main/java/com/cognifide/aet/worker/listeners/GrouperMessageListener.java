package com.cognifide.aet.worker.listeners;

import com.cognifide.aet.communication.api.job.GrouperJobData;
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
    LOGGER.error(grouperJobData.getComparisonResult().getStepResult().getArtifactId());
    dispatcher.run(jmsCorrelationId);
    // todo artifactId null when comparatorstepresult.status = passed
  }
}
