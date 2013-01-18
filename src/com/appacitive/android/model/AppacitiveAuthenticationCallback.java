package com.appacitive.android.model;

public interface AppacitiveAuthenticationCallback {

	public void onSuccess();
	public void onFailure(AppacitiveError error);
	
}
