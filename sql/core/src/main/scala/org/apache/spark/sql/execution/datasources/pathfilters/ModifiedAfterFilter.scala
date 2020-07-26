/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources.pathfilters

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.util.{CaseInsensitiveMap, DateTimeUtils}

/**
 * [SPARK-31962]
 * Filter used to determine whether file was modified
 * after the provided timestamp.
 *
 * @param sparkSession SparkSession
 * @param hadoopConf Hadoop Configuration object
 * @param options Map containing options
 */
case class ModifiedAfterFilter(sparkSession: SparkSession,
                               hadoopConf: Configuration,
                               options: CaseInsensitiveMap[String])
    extends ModifiedDateFilter(sparkSession, hadoopConf, options)
    with FileIndexFilter {
  override def accept(fileStatus: FileStatus): Boolean =
    (localTime(
      DateTimeUtils
      /* getModificationTime returns in milliseconds */
        .millisToMicros(fileStatus.getModificationTime))
      /* We standardize on microseconds wherever possible */
        - thresholdTime > 0)

  override def accept(path: Path): Boolean = true
  override def strategy(): String = "modifiedAfter"
}

case object ModifiedAfterFilter extends PathFilterObject {
  def get(sparkSession: SparkSession,
          configuration: Configuration,
          options: CaseInsensitiveMap[String]): ModifiedAfterFilter = {
    new ModifiedAfterFilter(sparkSession, configuration, options)
  }
  def strategy(): String = "modifiedafter"
}
