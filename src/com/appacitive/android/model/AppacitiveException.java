package com.appacitive.android.model;

public class AppacitiveException extends Exception{

	private static final long serialVersionUID = 1L;
	private int mCode;
	
	public AppacitiveException(int code, String message) {
		super(message);
		this.mCode = code;
	}
	
}
