package com.appacitive.android.callbacks;

import java.io.InputStream;

import com.appacitive.android.model.AppacitiveError;

public interface AppacitiveDownloadCallback {
	
	public void onSuccess(InputStream inputStream);
	/**
	 * Called when the user is successfully authenticated. 
	 * @param error APError object is sent containing the description of the error.
	 */
	public void onFailure(AppacitiveError error);

}
