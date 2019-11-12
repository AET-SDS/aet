package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsErrorsGrouper implements GrouperJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsErrorsGrouper.class);

  private static final String CLASS_NAME =
      new TypeToken<JsErrorsGrouper>() {}.getType().getTypeName();

  public JsErrorsGrouper() {
    LOGGER.error("{} job just spawned", CLASS_NAME);
  }
}
