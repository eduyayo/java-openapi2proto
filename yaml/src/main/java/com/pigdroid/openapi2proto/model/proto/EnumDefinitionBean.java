package com.pigdroid.openapi2proto.model.proto;

public class EnumDefinitionBean extends AbstractDefinitionBean<EnumFieldBean> {

	public EnumDefinitionBean(String name) {
		super(name);
	}

	@Override
	protected void printHeader(PrintContext printContext) {
		printContext.out().print("enum");
	}

}
