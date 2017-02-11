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
import com.kappaware.kdescribe.Model.Topic
import kafka.admin.AdminUtils
import kafka.server.ConfigType

/*
 * TODO: Display current broker config
 * TODO: Display consumer groups and lags
 * 
 */
object Engine {
  private val logger = getLogger

  def run(config: Configuration): Model = {
    val zkUtils = ZkUtils.apply(config.getZookeeper, 30000, 30000, false)
    val model = new Model()

    // ------------------------------------------------------------------------- Brokers handling
    val brokerIds = zkUtils.getChildrenParentMayNotExist(ZkUtils.BrokerIdsPath).sorted
    val brokers = brokerIds.map(_.toInt).map(buildBrokerInfo(zkUtils, _)).filter(_.isDefined).map(_.get)
    model.brokers = new ArrayList(WrapAsJava.seqAsJavaList(brokers))
    model.controllerId = zkUtils.getController()

    // -------------------------------------------------------------------------- Topic handling
    val allTopics2 = zkUtils.getAllTopics().sorted
    val allTopics = if(config.isIncludeAll)  allTopics2 else  allTopics2.filter { ! _.startsWith("__") }
    model.topics = new ArrayList[Topic]
    for (topic <- allTopics) {
      val mTopic = new Model.Topic()
      mTopic.name = topic
      if (zkUtils.pathExists(ZkUtils.getDeleteTopicPath(topic))) {
        mTopic.deleted = true;
      }
      zkUtils.getPartitionAssignmentForTopics(List(topic)).get(topic) match {
        case Some(topicPartitionAssignment) =>
          mTopic.properties = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic, topic)
          val sortedPartitions = topicPartitionAssignment.toList.sortWith((m1, m2) => m1._1 < m2._1)
          mTopic.partitionFactor = topicPartitionAssignment.size
          mTopic.replicationFactor = topicPartitionAssignment.head._2.size
          for ((partitionId, assignedReplicas) <- sortedPartitions) {
            val mPartition = new Model.Topic.Partition()
            mPartition.id = partitionId
            val isr = zkUtils.getInSyncReplicasForPartition(topic, partitionId)
            val sisr = isr.toSet
            val nisr = assignedReplicas.filterNot { sisr.contains(_) }
            mPartition.inSyncReplica = new ArrayList(WrapAsJava.seqAsJavaList(isr.map { x => x: java.lang.Integer }))
            mPartition.unsyncReplica = new ArrayList(WrapAsJava.seqAsJavaList(nisr.map { x => x: java.lang.Integer }))
            //mPartition.replicas = new ArrayList(WrapAsJava.seqAsJavaList(assignedReplicas.map { x => x : java.lang.Integer }))
            val leader = zkUtils.getLeaderForPartition(topic, partitionId)
            mPartition.leader = if (leader.isDefined) leader.get else null
            mTopic.partitions.add(mPartition)
          }
        case None =>
          logger.error("Topic " + topic + " doesn't exist!")
      }
      model.topics.add(mTopic)
    }
    zkUtils.close()
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

