package com.pigdroid.openapi2proto.model.proto;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class ProtoBean {

//	private String packageName = "com.inditex.grpc";
//	private String syntax = "proto3";
//	private List<OptionBean> options = new ArrayList<>(Arrays.asList(new OptionBean("java_multiple_files", "true")));
//	private List<String> imports = new ArrayList<>();
//
//	private List<AbstractDefinitionBean> definitions = new ArrayList<>();
//	private ServiceBean service;

	private final String packageName = "com.inditex.grpc";
	private final String syntax = "proto3";
	private final Set<OptionBean> options = new HashSet<>();
	private final Set<String> imports = new HashSet<>();

	private final Set<AbstractDefinitionBean> definitions = new HashSet<>();
	private final ServiceBean service;

	public ProtoBean(String name) {
		service = new ServiceBean(name);
	}

	public void addDefinition(AbstractDefinitionBean definition) {
		definitions.add(definition);
	}

	public void write(OutputStream stream) {
		try (PrintStream out = new PrintStream(stream)) {
			print(out);
		}
	}

	public void print(PrintStream out) {
		final PrintContext printContext = new PrintContext(out);
		printContext.out().println("syntax = \"proto3\";");
		printContext.out().println();
		options.stream().forEach(each -> {
			each.print(printContext);
		});
		imports.stream().forEach(each -> {
			printContext.out().println(String.format("import \"%s\";", each));
		});
		out.println();
		definitions.forEach(each -> {
			each.print(printContext);
		});
		service.print(printContext);
	}

	public void addImport(String include) {
		imports.add(include);
	}

	public void addOption(String key, String value) {
		addOption(new OptionBean(key, value));
	}

	private void addOption(OptionBean optionBean) {
		options.add(optionBean);
	}

	public void addOperation(RPCBean rpc) {
		service.addRPC(rpc);
	}

}
