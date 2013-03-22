package com.appacitive.android.callbacks;

import java.util.List;

import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitivePagingInfo;

/**
 * Interface definition for callback to be invoked when executing the fetch operation.
 * @author Sandeep Dhull
 *
 */
public interface AppacitiveFetchCallback<T> {
	/**
	 * Called when the fetch is successful.
	 * @param response Map containing the fetched values as key value pair.
	 * @param 
	 */
	public void onSuccess(List<T> response, AppacitivePagingInfo pagingInfo);
	/**
	 * Called when the fetch has failed due to some reason.
	 * @param error APError containing the desription of the error.
	 */
	public void onFailure(AppacitiveError error);
	
}
