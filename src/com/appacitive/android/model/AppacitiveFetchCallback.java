package com.appacitive.android.model;

import java.util.Map;

public interface AppacitiveFetchCallback {
	public void onSuccess(Map<String, Object> response);
	public void onFailure(AppacitiveError error);
	
}
