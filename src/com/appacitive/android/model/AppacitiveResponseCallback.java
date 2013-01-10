package com.appacitive.android.model;

public interface AppacitiveResponseCallback {
	
	public void onSucess();
	public void onFailure(AppacitiveError error);
	
}
