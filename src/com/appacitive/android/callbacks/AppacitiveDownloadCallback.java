package com.appacitive.android.callbacks;

import java.io.InputStream;

import com.appacitive.android.model.AppacitiveError;

/**
 * Interface definition for a callback to be invoked when download operation is called. 
 */
public interface AppacitiveDownloadCallback {
	
	/**
	 * Called when the download operation has completed successfully.
	 * @param inputStream
	 */
	public void onSuccess(InputStream inputStream);
	/**
	 * Called when there's an error occured during the this call. 
	 * @param error APError object is sent containing the description of the error.
	 */
	public void onFailure(AppacitiveError error);

}
