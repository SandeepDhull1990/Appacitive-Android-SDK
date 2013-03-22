package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitiveUser;

/**
 * Interface definition for a callback to be invoked when a view is clicked.
 * @author Sandeep Dhull
 */
public interface AppacitiveSignUpCallback {
	/**
	 * Called when the user has successfully signed up. 
	 * @param user The AppacitiveUser object, containing the user information.
	 */
	public void onSuccess(AppacitiveUser user);
	/**
	 * Called the user signup operation is failed.
	 * @param error The AppacitiveError containing the description of the error.
	 */
	public void onFailure(AppacitiveError error);
}
