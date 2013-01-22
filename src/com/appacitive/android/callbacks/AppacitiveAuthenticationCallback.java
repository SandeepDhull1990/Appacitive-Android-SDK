package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;

public interface AppacitiveAuthenticationCallback {

	public void onSuccess();
	public void onFailure(AppacitiveError error);
	
}
