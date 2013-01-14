package com.appacitive.android.model;

public interface AppacitiveCallback {
	
	public void onSuccess();
	public void onFailure(AppacitiveError error);
	
}
