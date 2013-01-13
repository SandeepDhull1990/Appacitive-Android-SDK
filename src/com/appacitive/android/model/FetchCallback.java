package com.appacitive.android.model;

import org.json.JSONObject;

public interface FetchCallback {
	public void onSuccess(JSONObject response);
	public void onFailure(AppacitiveError error);
	
}
