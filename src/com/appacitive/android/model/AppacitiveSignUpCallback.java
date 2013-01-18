package com.appacitive.android.model;

public interface AppacitiveSignUpCallback {
	public void onSuccess(AppacitiveUser user);
	public void onFailure(AppacitiveError error);
}
