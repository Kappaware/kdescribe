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
package com.kappaware.kdescribe.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class Parameters {
	static Logger log = LoggerFactory.getLogger(Parameters.class);

	private String zookeeper;
	private OutputFormat outputFormat;
	private boolean includeAll;
	private boolean partitions;
	private boolean ts;
	private List<String> properties;
	private boolean forceProperties;

	static OptionParser parser = new OptionParser();
	static {
		parser.formatHelpWith(new BuiltinHelpFormatter(120,2));
	}

	static OptionSpec<String> ZOOKEEPER_OPT = parser.accepts("zookeeper", "Comma separated values of Zookeeper nodes").withRequiredArg().describedAs("zk1:2181,ek2:2181").ofType(String.class).required();
	static OptionSpec<OutputFormat> OUTPUT_FORMAT_OPT = parser.accepts("outputFormat", "Output format").withRequiredArg().describedAs("text|json|yaml").ofType(OutputFormat.class).defaultsTo(OutputFormat.text);
	static OptionSpec<Void> INCLUDE_ALL_OPT = parser.accepts("includeAll", "All topics, including systems ones");
	static OptionSpec<Void> PARTITIONS_OPT = parser.accepts("partitions", "List topic partitions");
	static OptionSpec<Void> TS_OPT = parser.accepts("ts", "List topic partitions with timestamp");
	static OptionSpec<String> CONSUMER_PROPERTY_OPT = parser.accepts("property", "Consumer property (May be specified several times)").withRequiredArg().describedAs("prop=val").ofType(String.class);
	static OptionSpec<Void> FORCE_PROPERTIES_OPT = parser.accepts("forceProperties", "Force unsafe properties");

	@SuppressWarnings("serial")
	private static class MyOptionException extends Exception {
		public MyOptionException(String message) {
			super(message);
		}
	}

	
	public Parameters(String[] argv) throws ConfigurationException {
		try {
			OptionSet result = parser.parse(argv);
			if (result.nonOptionArguments().size() > 0 && result.nonOptionArguments().get(0).toString().trim().length() > 0) {
				throw new MyOptionException(String.format("Unknow option '%s'", result.nonOptionArguments().get(0)));
			}
			// Mandatories parameters
			this.zookeeper = result.valueOf(ZOOKEEPER_OPT);
			this.outputFormat = result.valueOf(OUTPUT_FORMAT_OPT);
			this.includeAll = result.has(INCLUDE_ALL_OPT);
			this.partitions = result.has(PARTITIONS_OPT);
			this.ts = result.has(TS_OPT);
			this.properties = result.valuesOf(CONSUMER_PROPERTY_OPT);
			this.forceProperties = result.has(FORCE_PROPERTIES_OPT);

		} catch (OptionException | MyOptionException t) {
			throw new ConfigurationException(usage(t.getMessage()));
		}
	}

	private static String usage(String err) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		if (err != null) {
			pw.print(String.format("\n\n * * * * * ERROR: %s\n\n", err));
		}
		try {
			parser.printHelpOn(pw);
		} catch (IOException e) {
		}
		pw.flush();
		pw.close();
		return baos.toString();
	}

	// --------------------------------------------------------------------------

	public String getZookeeper() {
		return zookeeper;
	}

	public OutputFormat getOutputFormat() {
		return outputFormat;
	}

	public boolean isIncludeAll() {
		return includeAll;
	}

	public boolean isPartitions() {
		return partitions;
	}

	public boolean isTs() {
		return ts;
	}

	public boolean isForceProperties() {
		return this.forceProperties;
	}

	public List<String> getProperties() {
		return this.properties;
	}


}
