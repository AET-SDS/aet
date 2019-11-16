package com.cognifide.aet.job.api.grouper;

import com.cognifide.aet.vs.DBKey;

//todo javadoc
public interface GrouperFactory {

  String getName();

  GrouperJob createInstance(DBKey dbKey, long expectedInputCount);  //todo more params
}
