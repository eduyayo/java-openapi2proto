package com.pigdroid.utils;

public class Strings {

	private Strings() {}

	public static String capitalize(String asText) {
		return asText.substring(0, 1).toUpperCase() + asText.substring(1);
	}

	public static String cleanFileName(String name) {
		final String ret = name;
		int index = ret.lastIndexOf('.');
		if (index < 0) {
			index = ret.length();
		}
		return ret.substring(0, index);
	}

}
