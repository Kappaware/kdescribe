/*
 * Copyright (C) 2016 BROADSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kappaware.kdescribe

import org.log4s._
import com.kappaware.kdescribe.config.Parameters
import com.kappaware.kdescribe.config.ConfigurationImpl
import com.kappaware.kdescribe.config.ConfigurationException
import com.kappaware.kdescribe.config.Configuration
import com.kappaware.kdescribe.config.OutputFormat

object Main {
  private val logger = getLogger

  def main(args: Array[String]): Unit = {

    try {
      val config: Configuration = new ConfigurationImpl(new Parameters(args))
      val model = Engine.run(config)
      
      val out = config.getOutputFormat match {
        case OutputFormat.json => model.toJson
        case OutputFormat.yaml => model.toYaml
      }
      println(out)
      System.exit(0)
    } catch {
      case ce: ConfigurationException =>
        logger.error(ce.getMessage)
        System.exit(1)
      case e: Exception =>
        logger.error(e)("ERROR")
        System.exit(3)
    }

  }
}

