package com.kappaware.kdescribe

import scala.collection.convert.WrapAsJava

import com.kappaware.kdescribe.config.Configuration
import kafka.utils.ZkUtils
import org.log4s._
import kafka.common.BrokerNotAvailableException
import kafka.utils.Json
import kafka.common.KafkaException
import org.apache.kafka.common.protocol.SecurityProtocol
import kafka.cluster.EndPoint
import java.util.ArrayList

object Engine {
  private val logger = getLogger

  def run(config: Configuration) : Model = {
    val zkUtils = ZkUtils.apply(config.getZookeeper, 30000, 30000, false)
    val brokerIds = zkUtils.getChildrenParentMayNotExist(ZkUtils.BrokerIdsPath).sorted
    val brokers = brokerIds.map(_.toInt).map(buildBrokerInfo(zkUtils, _)).filter(_.isDefined).map(_.get)
    val model = new Model()
    model.brokers = new ArrayList(WrapAsJava.seqAsJavaList(brokers))
    model.controllerId = zkUtils.getController()
    model
  }

  def buildBrokerInfo(zkUtils: ZkUtils, brokerId: Int): Option[Model.Broker] = {
    zkUtils.readDataMaybeNull(ZkUtils.BrokerIdsPath + "/" + brokerId)._1 match {
      case Some(brokerInfo) => Some(createBroker(brokerId, brokerInfo))
      case None => None
    }
  }

  def createBroker(brokerId: Int, brokerInfoString: String): Model.Broker = {
    if (brokerInfoString == null)
      throw new BrokerNotAvailableException(s"Broker id $brokerId does not exist")
    try {
      Json.parseFull(brokerInfoString) match {
        case Some(m) =>
          val broker = new Model.Broker(brokerId, brokerInfoString)
          broker
        case None =>
          throw new BrokerNotAvailableException(s"Broker id $brokerId does not exist")
      }
    } catch {
      case t: Throwable =>
        throw new KafkaException(s"Failed to parse the broker info from zookeeper: $brokerInfoString", t)
    }
  }

}

