/*
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cognifide.aet.job.api.grouper;

import com.cognifide.aet.vs.DBKey;

/**
 * This factory is used to instantiate Grouper instance with given name.
 *
 * <p>Implementation of this interface should be a OSGI service so each implementation requires to
 * have * {@literal @}Service and {@literal @}Component annotation to work and register properly.
 */
public interface GrouperFactory {

  /**
   * @return name, which the grouper factory will be registered on. It has to be unique for all
   * modules in the grouping phase.
   */
  String getName();

  /**
   * Each call returns new grouper job instance.
   *
   * @param dbKey              database connection properties
   * @param expectedInputCount expected number of input messages before result generation
   * @return new grouper job instance
   * @see GrouperJob
   */
  GrouperJob createInstance(DBKey dbKey, long expectedInputCount);
}
