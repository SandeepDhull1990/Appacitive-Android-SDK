package com.appacitive.android.util;

public enum AppacitiveRequestMethods {
	GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"), OPTION("OPTION"), HEAD(
			"HEAD");

	private final String mRequestMethod;

	private AppacitiveRequestMethods(String requestMethod) {
		mRequestMethod = requestMethod;
	}

	public String requestMethod() {
		return mRequestMethod;
	}

}
