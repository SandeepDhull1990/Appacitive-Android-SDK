package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;

public interface AppacitiveFetchUrlCallback {
	/**
	 * Called when the download operation has completed successfully.
	 * @param inputStream
	 */
	public void onSuccess(String url);
	/**
	 * Called when there's an error occured during the this call. 
	 * @param error APError object is sent containing the description of the error.
	 */
	public void onFailure(AppacitiveError error);
}
