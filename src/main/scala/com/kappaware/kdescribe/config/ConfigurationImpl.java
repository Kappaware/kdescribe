package com.kappaware.kdescribe.config;

public class ConfigurationImpl implements Configuration {
	private Parameters parameters;
	
	public ConfigurationImpl(Parameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public String getZookeeper() {
		return parameters.getZookeeper();
	}

	@Override
	public OutputFormat getOutputFormat() {
		return parameters.getOutputFormat();
	}
	
	
	
	
}
