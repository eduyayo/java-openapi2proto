package com.pigdroid.openapi2proto.model.proto;

import java.util.ArrayList;
import java.util.List;

public class ServiceBean {

	private final String name;

	public ServiceBean(String name) {
		this.name = name;
	}

	private final List<RPCBean> rpcs = new ArrayList<>();

	public void addRPC(RPCBean rpc) {
		rpcs.add(rpc);
	}

	public void print(PrintContext printContext) {
		printContext.out().println(String.format("service %s {", name));

		rpcs.stream().forEach(each -> each.print(printContext));

		printContext.out().println("}");
	}

}
