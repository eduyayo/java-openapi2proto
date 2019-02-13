package com.pigdroid.openapi2proto.model.proto;

public class MessageDefinitionBean extends AbstractDefinitionBean<MessageFieldBean> {

	public MessageDefinitionBean(String name) {
		super(name);
	}

	@Override
	protected void print(PrintContext printContext) {
		printContext.inc();
		super.print(printContext);
	}

	@Override
	protected void printHeader(PrintContext printContext) {
		printContext.out().print("message");
	}

}
