package com.pigdroid.openapi2proto.model.proto;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDefinitionBean<F extends AbstractFieldBean> {

	private final List<F> fields = new ArrayList<>();

	private final String name;

	protected AbstractDefinitionBean(String name) {
		this.name = name;
	}

	public void addField(F field) {
		fields.add(field);
	}

	protected void print(PrintContext printContext) {
		printHeader(printContext);
		printContext.out().println(String.format(" %s {", name));
		fields.stream().forEach(each -> {
			printContext.out().print(" ");
			each.print(printContext);
		});
		printContext.out().println(String.format(" }"));
		printContext.out().println();
		printContext.reset();
	}

	protected abstract void printHeader(PrintContext printContext);

	@Override
	public String toString() {
		return "AbstractDefinitionBean [name=" + name + "]";
	}

	public String getName() {
		return name;
	}

}
