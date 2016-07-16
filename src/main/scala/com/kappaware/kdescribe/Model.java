package com.kappaware.kdescribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
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
	
	
	public static class Broker {
		// For YAML generation
		Broker() {
		}

		/*
		 * We pass through an intermediate map, in order to be loosely coupled between the zookeeper description and the displayed one  
		 */
		@SuppressWarnings("unchecked")
		Broker(Integer id, String jsonString) throws JSONObjectException, IOException {
			this.id = id;
			Map<String, Object> m;
			m = JSON.std.mapFrom(jsonString);
			this.version = (Integer) m.get("version");
			this.host = (String) m.get("host");
			this.port = (Integer) m.get(port);
			this.jmx_port = (Integer) m.get("jmx_port");
			this.timestamp = (String) m.get("timestamp");
			this.endpoints = (List<String>) m.get("endpoints");
			this.rack = (String) m.get("rack");
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
			return timestamp;
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
		
		public static class Partition {
			public Integer id;
			public Integer leader;
			public ArrayList<Integer> replicas;
			public ArrayList<Integer> inSyncReplica;
			public ArrayList<Integer> unsyncReplica;
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
