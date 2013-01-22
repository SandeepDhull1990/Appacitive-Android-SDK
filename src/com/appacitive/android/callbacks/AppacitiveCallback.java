package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;

public interface AppacitiveCallback {
	
	public void onSuccess();
	public void onFailure(AppacitiveError error);
	
}
