package com.cognifide.aet.job.api.grouper;

import com.cognifide.aet.communication.api.job.GrouperJobData;
import com.cognifide.aet.communication.api.job.GrouperResultData;

public interface GrouperJob {

  //todo add methods
  GrouperResultData group(GrouperJobData jobData);
}
