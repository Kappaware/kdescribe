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

