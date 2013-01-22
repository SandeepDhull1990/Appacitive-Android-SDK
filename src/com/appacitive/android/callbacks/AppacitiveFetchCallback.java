package com.appacitive.android.callbacks;

import java.util.Map;

import com.appacitive.android.model.AppacitiveError;

public interface AppacitiveFetchCallback {
	public void onSuccess(Map<String, Object> response);
	public void onFailure(AppacitiveError error);
	
}
