package com.cognifide.aet.worker.impl;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class GrouperDispatcherWrapper implements GrouperDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperDispatcherWrapper.class);

  private final ConcurrentMap<String, GrouperDispatcherImpl> dispatchers =
      new ConcurrentHashMap<>();

  @Override
  public void run(String correlationId, GrouperJobData grouperJobData) {
    GrouperDispatcherImpl dispatcher =
        dispatchers.computeIfAbsent(
            correlationId, it -> new GrouperDispatcherImpl(grouperJobData.getComparatorCounts()));
    dispatcher.run(correlationId, grouperJobData);
    if (dispatcher.isFinished()) {
      LOGGER.error("DELETING DISPATCHER FOR ID: {}", correlationId);
      dispatchers.remove(correlationId);
    }
  }
}
