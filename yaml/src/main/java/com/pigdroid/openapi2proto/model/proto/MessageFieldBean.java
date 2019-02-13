package com.pigdroid.openapi2proto.model.proto;

public class MessageFieldBean extends AbstractFieldBean {

	private final boolean repeated;

	private final AbstractPropertyTypeBean type;

	public MessageFieldBean(String name, AbstractPropertyTypeBean type, boolean repeated) {
		super(name);
		this.type = type;
		this.repeated = repeated;
	}

	@Override
	protected void print(PrintContext printContext) {
		if (repeated) {
			printContext.out().print("repeated ");
		}
		if (type != null) {
			type.printHead(printContext);
		} else {
			printContext.out().println("#NULLTYPEHEAD#");
		}
		printContext.out().print(" ");
		if (!(type instanceof OneOfPropertyTypeBean)) {
			super.print(printContext);
		} else {
			printContext.out().print(getName());
		}
		printContext.out().print(" ");

		if (type != null) {
			type.printBody(printContext);
		} else {
			printContext.out().println("#NULLTYPEBODY#");
		}
	}

}
