package com.pigdroid.openapi2proto.model.proto;

public class RPCBean {

	private final String name;
	private final AbstractDefinitionBean requestType;
	private final AbstractDefinitionBean responseType;

	public RPCBean(String name, MessageDefinitionBean request, MessageDefinitionBean response) {
		this.name = name;
		this.requestType = request;
		this.responseType = response;
	}

	public void print(PrintContext printContext) {
		printContext.out().println(String.format("    rpc %s(%s) returns (%s) {}", name, requestType.getName(), responseType.getName()));
	}

}
