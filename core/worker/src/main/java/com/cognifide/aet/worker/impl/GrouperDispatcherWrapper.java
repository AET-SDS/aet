package com.cognifide.aet.worker.impl;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.worker.api.GrouperDispatcher;
import com.cognifide.aet.worker.api.JobRegistry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class GrouperDispatcherWrapper implements GrouperDispatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrouperDispatcherWrapper.class);

  @Reference
  private JobRegistry jobRegistry;

  private final ConcurrentMap<String, GrouperDispatcherImpl> dispatchers =
      new ConcurrentHashMap<>();

  @Override
  public void run(String correlationId, GrouperJobData grouperJobData) {
    GrouperDispatcherImpl dispatcher =
        dispatchers.computeIfAbsent(
            correlationId,
            it -> new GrouperDispatcherImpl(jobRegistry, grouperJobData.getComparatorCounts()));
    dispatcher.run(correlationId, grouperJobData);
    if (dispatcher.isFinished()) {
      LOGGER.error("DELETING DISPATCHER FOR ID: {}", correlationId);
      dispatchers.remove(correlationId);
    }
  }
}
