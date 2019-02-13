package com.pigdroid.openapi2proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class OpenAPI2Proto {

	public OpenAPI2Proto(String sourceFileName) throws FileNotFoundException {
		this(new File(sourceFileName));
	}

	public OpenAPI2Proto(File sourceFile) throws FileNotFoundException {
		this(new FileInputStream(sourceFile));
	}

	public OpenAPI2Proto(InputStream inputStream) {

	}

}
