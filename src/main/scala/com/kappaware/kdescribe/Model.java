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
package com.kappaware.kdescribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.kappaware.kdescribe.config.Configuration;
import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class Model {
	public ArrayList<Broker> brokers;
	public Integer controllerId;

	public ArrayList<Topic> topics;

	static YamlConfig yamlConfig = new YamlConfig();
	static {
		yamlConfig.writeConfig.setWriteRootTags(false);
		yamlConfig.writeConfig.setWriteRootElementTags(false);
		yamlConfig.setPropertyElementType(Model.class, "brokers", Broker.class);
		yamlConfig.setPropertyElementType(Model.class, "topics", Topic.class);
		yamlConfig.setPropertyElementType(Topic.class, "partitions", Topic.Partition.class);
	}
	static JSON djson = JSON.std.with(Feature.PRETTY_PRINT_OUTPUT);

	String toYaml() {
		return Misc.toYamlString(this, Model.yamlConfig);
	}

	String toJson() throws JSONObjectException, IOException {
		return djson.asString(this);
	}

	String toHuman(Configuration config) {
		StringBuffer sb = new StringBuffer();
		sb.append("Brokers:\n");
		for (Broker broker : this.brokers) {
			sb.append(broker.toHuman());
		}
		sb.append("Topics:\n");
		for (Topic topic : this.topics) {
			sb.append(topic.toHuman(config));
		}
		return sb.toString();
	}

	public static class Broker {
		// For YAML generation
		Broker() {
		}

		/*
		 * We pass through an intermediate map, in order to be loosely coupled
		 * between the zookeeper description and the displayed one
		 */
		@SuppressWarnings("unchecked")
		Broker(Integer id, String jsonString) throws JSONObjectException, IOException {
			this.id = id;
			Map<String, Object> m;
			m = JSON.std.mapFrom(jsonString);
			this.version = (Integer) m.get("version");
			this.host = (String) m.get("host");
			this.port = (Integer) m.get("port");
			this.jmx_port = (Integer) m.get("jmx_port");
			this.timestamp = Misc.printIsoDateTime(Misc.parseLong((String) m.get("timestamp")));
			this.endpoints = (List<String>) m.get("endpoints");
			this.rack = (String) m.get("rack");
		}

		String toHuman() {
			return (String.format("\thosts:%s   id:%d\n", this.host, this.id));
		}

		public Integer id;
		public Integer version;
		public String host;
		public Integer port;
		public Integer jmx_port;
		public String timestamp;
		public List<String> endpoints;
		public String rack;

		public Integer getId() {
			return id;
		}

		public Integer getVersion() {
			return version;
		}

		public String getHost() {
			return host;
		}

		public Integer getPort() {
			return port;
		}

		public Integer getJmx_port() {
			return jmx_port;
		}

		public String getTimestamp() {
			return this.timestamp;
		}

		public List<String> getEndpoints() {
			return endpoints;
		}

		public String getRack() {
			return rack;
		}
	}

	public static class Topic {
		public String name;
		public Integer replicationFactor;
		public Integer partitionFactor;
		public Properties properties;
		public Boolean deleted;
		public ArrayList<Partition> partitions = new ArrayList<Partition>();
		public String comment;

		String toHuman(Configuration config) {
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("\tname:%-16s  #partition:%2d  #replicats:%2d %s\n", //
					this.name, //
					this.partitionFactor, //
					this.replicationFactor, //
					(this.deleted != null && this.deleted) ? "DELETED" : "" //
			));
			if (config.isPartitions() || config.isTs()) {
				for (Topic.Partition partition : this.partitions) {
					sb.append(partition.toHuman());
				}
			}
			return sb.toString();
		}

		public static class Partition {
			public Integer id;
			public Integer leader;
			public ArrayList<Integer> replicas;
			public ArrayList<Integer> inSyncReplica;
			public ArrayList<Integer> unsyncReplica;
			public Position start;
			public Position end;

			public String toHuman() {
				StringBuffer sb = new StringBuffer();
				sb.append(String.format("\t\tid:%2d  leaderId:%2d  #isr:%2d  #usr:%2d", this.id, this.leader,
						this.inSyncReplica.size(), this.unsyncReplica.size()));
				sb.append(String.format("  first:%8d", this.start.offset));
				sb.append(String.format("  last:%8d", this.end.offset));
				if (this.start.timestamp != null) {
					sb.append(String.format(" firstTs: %s", this.start.timestamp));
				}
				if (this.end.timestamp != null) {
					sb.append(String.format(" lastTs: %s", this.end.timestamp));
				}
				sb.append("\n");
				return sb.toString();
			}

			public Integer getId() {
				return id;
			}

			public Integer getLeader() {
				return leader;
			}

			public ArrayList<Integer> getReplicas() {
				return replicas;
			}

			public ArrayList<Integer> getInSyncReplica() {
				return inSyncReplica;
			}

			public ArrayList<Integer> getUnsyncReplica() {
				return unsyncReplica;
			}

			public Position getStart() {
				return start;
			}

			public Position getEnd() {
				return end;
			}

			public static class Position {
				public Long offset;
				public String timestamp;

				public Position() {
				}

				public Position(Long offset, String timestamp) {
					this.offset = offset;
					this.timestamp = timestamp;
				}

				public Long getOffset() {
					return offset;
				}

				public String getTimestamp() {
					return timestamp;
				}
			}

		}

		public String getName() {
			return name;
		}

		public Integer getReplicationFactor() {
			return replicationFactor;
		}

		public Integer getPartitionFactor() {
			return partitionFactor;
		}

		public Properties getProperties() {
			return properties;
		}

		public Boolean getDeleted() {
			return deleted;
		}

		public ArrayList<Partition> getPartitions() {
			return partitions;
		}

		public String getComment() {
			return comment;
		}

	}

	public ArrayList<Broker> getBrokers() {
		return brokers;
	}

	public Integer getControllerId() {
		return controllerId;
	}

	public ArrayList<Topic> getTopics() {
		return topics;
	}

}
