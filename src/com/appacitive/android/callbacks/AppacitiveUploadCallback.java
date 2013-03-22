package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;

public interface AppacitiveUploadCallback {

	/**
	 * Called when is operation is successful.
	 */
	public void onSuccess(String url);
	/**
	 * Called when the operation is failed
	 * @param error APError object is sent containing the description of the error.
	 */
	public void onFailure(AppacitiveError error);
	
	
}
