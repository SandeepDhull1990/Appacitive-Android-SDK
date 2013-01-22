package com.appacitive.android.callbacks;

import java.util.Map;

import com.appacitive.android.model.AppacitiveError;

/**
 * Interface definition for callback to be invoked when executing the fetch operation.
 * @author Sandeep Dhull
 *
 */
public interface AppacitiveFetchCallback {
	/**
	 * Called when the fetch is successful.
	 * @param response Map containing the fetched values as key value pair.
	 */
	public void onSuccess(Map<String, Object> response);
	/**
	 * Called when the fetch has failed due to some reason.
	 * @param error APError containing the desription of the error.
	 */
	public void onFailure(AppacitiveError error);
	
}
