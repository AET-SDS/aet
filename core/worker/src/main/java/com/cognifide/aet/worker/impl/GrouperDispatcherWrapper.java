package com.cognifide.aet.worker.impl;

import com.cognifide.aet.worker.api.GrouperDispatcher;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class GrouperDispatcherWrapper implements GrouperDispatcher {

  private final ConcurrentMap<String, GrouperDispatcherImpl> dispatchers =
      new ConcurrentHashMap<>();

  @Override
  public void run(String correlationId) {
    GrouperDispatcherImpl dispatcher =
        dispatchers.computeIfAbsent(correlationId, it -> new GrouperDispatcherImpl());
    dispatcher.run(correlationId);
    if (dispatcher.isFinished()) {
      dispatchers.remove(correlationId);
    }
  }
}
