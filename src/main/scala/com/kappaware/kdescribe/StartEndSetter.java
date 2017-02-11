package com.kappaware.kdescribe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kappaware.kdescribe.config.Configuration;

public class StartEndSetter {
	static Logger log = LoggerFactory.getLogger(StartEndSetter.class);

	public static void enrich(Model model, Configuration configuration) {
		// First, build the brokers connection String
		StringBuffer sb = new StringBuffer();
		String sep = "";
		for (Model.Broker broker : model.brokers) {
			//sb.append(String.format("%s%s:%s", sep, broker.host, broker.port));
			sb.append(sep + broker.endpoints.get(0));
			sep = ",";
		}
		String brokers = sb.toString();
		log.debug("Brokers:" + brokers);
		Properties consumerProperties = configuration.getConsumerProperties();
		if(consumerProperties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG) == null) {
			// Will find it from endoint
			String[] sa = brokers.split(":");
			String sec = sa[0];
			if("PLAINTEXTSASL".equals(sec)) {	// Don't kno why Hortonwworks set this value!!
				sec = "SASL_PLAINTEXT";		
			}
			log.debug(String.format("Will set %s to %s", CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, sec));
			consumerProperties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, sec);
		}
		consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
		consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "kdescribe");
		consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		//consumerProperties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1);
		KafkaConsumer<?, ?> consumer = new KafkaConsumer<byte[], byte[]>(consumerProperties, new ByteArrayDeserializer(), new ByteArrayDeserializer());

		Map<String, List<PartitionInfo>> topics = consumer.listTopics();
		if (model.topics != null) {
			for (Model.Topic topic : model.topics) {
				List<PartitionInfo> partitionInfo = topics.get(topic.name);
				if (partitionInfo == null) {
					topic.comment = "No access."; // May occurs due to access right
				} else {
					if (partitionInfo.size() != topic.partitions.size()) {
						topic.comment = String.format("WARNING: topic '%s': partition size missmatch (%d != %d)", topic.name, partitionInfo.size(), topic.partitions.size());
					} else {
						for (int p = 0; p < topic.partitions.size(); p++) {
							TopicPartition topicPartition = new TopicPartition(topic.name, p);
							List<TopicPartition> partAsList = Arrays.asList(new TopicPartition[] { topicPartition });
							consumer.assign(partAsList);
							consumer.seekToBeginning(partAsList);
							long firstOffset = consumer.position(topicPartition); // Never fail, as 0 if empty
							consumer.seekToEnd(partAsList);
							long lastOffset = consumer.position(topicPartition) - 1;
							//log.debug(String.format("Topic: %s -  first offset:%d last offset: %d", topic.name, firstOffset, lastOffset));
							if (lastOffset < firstOffset || !configuration.isTs()) {
								// Partition is empty
								topic.partitions.get(p).start = new Model.Topic.Partition.Position(firstOffset, null);
								topic.partitions.get(p).end = new Model.Topic.Partition.Position(lastOffset, null);
							} else {
								consumer.seekToBeginning(partAsList);
								ConsumerRecord<?, ?> firstRecord = fetch(consumer);
								consumer.seek(topicPartition, lastOffset);
								ConsumerRecord<?, ?> lastRecord = fetch(consumer);
								topic.partitions.get(p).start = new Model.Topic.Partition.Position(firstOffset, Misc.printSimpleIsoDateTime(firstRecord.timestamp()));
								topic.partitions.get(p).end = new Model.Topic.Partition.Position(lastOffset-1, Misc.printSimpleIsoDateTime(lastRecord.timestamp()));
							}
						}
					}

				}

			}
		}
		consumer.close();
	}

	// We need some polling time, as even if there is some messages, polling with a short value may return an empty set.
	static private ConsumerRecord<?, ?> fetch(KafkaConsumer<?, ?> consumer) {
		ConsumerRecords<?, ?> records = consumer.poll(2000);
		if (records.count() == 0) {
			return null;
		} else {
			return records.iterator().next();
		}
	}

}
