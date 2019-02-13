package com.pigdroid.openapi2proto.model.proto;

import java.io.PrintStream;

public class PrintContext {

	private final PrintStream printStream;
	private int count = 0;

	public PrintContext(PrintStream printStream) {
		this.printStream = printStream;
	}

	public int inc() {
		return count++;
	}

	public int reset() {
		return count = 0;
	}

	public PrintStream out() {
		return printStream;
	}
}
