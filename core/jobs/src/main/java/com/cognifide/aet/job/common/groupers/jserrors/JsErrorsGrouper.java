package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.google.common.reflect.TypeToken;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsErrorsGrouper implements GrouperJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsErrorsGrouper.class);
  private static final String CLASS_NAME =
      new TypeToken<JsErrorsGrouper>() {}.getType().getTypeName();

  private final AtomicLong counter;

  public JsErrorsGrouper(long expectedInputCount) {
    this.counter = new AtomicLong(expectedInputCount);
    LOGGER.error("{} job just spawned", CLASS_NAME);
  }

  @Override
  public GrouperResultData group(GrouperJobData jobData) {
    long value = counter.decrementAndGet(); // todo debug
    return new GrouperResultData(value == 0);
  }
}
