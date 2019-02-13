package com.pigdroid.openapi2proto.model.proto;

public abstract class AbstractPropertyTypeBean {

	private final String name;

	protected AbstractPropertyTypeBean(String name) {
		this.name = name;
	}

	public void printHead(PrintContext printContext) {
		printContext.out().print(name);
	}

	public void printBody(PrintContext printContext) {

	}

	public String getName() {
		return name;
	}

}
