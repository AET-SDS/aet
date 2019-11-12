package com.cognifide.aet.worker.api;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;

public interface GrouperDispatcher {

  GrouperResultData run(String correlationId, GrouperJobData grouperJobData);
}
