package com.cognifide.aet.worker.api;

import com.cognifide.aet.communication.api.job.GrouperJobData;

public interface GrouperDispatcher {

  void run(String correlationId, GrouperJobData grouperJobData);
}
