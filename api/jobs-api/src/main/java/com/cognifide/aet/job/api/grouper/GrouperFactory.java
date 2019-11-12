package com.cognifide.aet.job.api.grouper;

//todo javadoc
public interface GrouperFactory {

  String getName();

  GrouperJob createInstance();  //todo more params
}
