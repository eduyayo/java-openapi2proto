package com.pigdroid.openapi2proto.model.proto;

public class OptionBean {

	private final String key;
	private final String value;

	public OptionBean(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public void print(PrintContext printContext) {
		printContext.out().println(String.format("option %s =\"%s\";", key, value));
	}

}
