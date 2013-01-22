package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;

/**
 * Interface definition for a callback to be invoked when user makes an authentication request. 
 * @author Sandeep Dhull
 */
public interface AppacitiveAuthenticationCallback {

	/**
	 * Called when the user is successfully authenticated. 
	 */
	public void onSuccess();
	/**
	 * Called when the user is successfully authenticated. 
	 * @param error APError object is sent containing the description of the error.
	 */
	public void onFailure(AppacitiveError error);
	
}
