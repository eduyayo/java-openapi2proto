package com.pigdroid.openapi2proto.model.proto;

import java.util.ArrayList;
import java.util.List;

public class OneOfPropertyTypeBean extends AbstractPropertyTypeBean {

	private final List<MessageFieldBean> fields = new ArrayList<>();

	public OneOfPropertyTypeBean() {
		super("");
	}

	public void addField(MessageFieldBean value) {
		fields.add(value);
	}

	@Override
	public void printHead(PrintContext printContext) {
		printContext.out().print("oneof ");
	}


	@Override
	public void printBody(PrintContext printContext) {
		super.printHead(printContext);
		printContext.out().println(" { ");
		fields.stream().forEach(each -> {
			each.print(printContext);
		});
		printContext.out().println("}");
	}

}
