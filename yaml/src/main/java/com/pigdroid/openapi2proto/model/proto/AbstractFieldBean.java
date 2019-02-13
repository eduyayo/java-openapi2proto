package com.pigdroid.openapi2proto.model.proto;

public abstract class AbstractFieldBean {

	private final String name;

	protected AbstractFieldBean(String name) {
		this.name = name;
	}

	protected void print(PrintContext printContext) {
		printContext.out().print(name);
		printContext.out().println(String.format(" = %d;", printContext.inc()));
	}

	protected String getName() {
		return name;
	}


}
