package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;

/**
 * Interface definition for a Appacitive callback operation.
 * @author Sandeep Dhull
 *
 */
public interface AppacitiveCallback {
	
	/**
	 * Called when is operation is successful.
	 */
	public void onSuccess();
	/**
	 * Called when the operation is failed
	 * @param error APError object is sent containing the description of the error.
	 */
	public void onFailure(AppacitiveError error);
	
}
