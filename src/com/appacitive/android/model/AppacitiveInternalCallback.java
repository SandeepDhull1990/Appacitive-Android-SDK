package com.appacitive.android.model;


interface AppacitiveInternalCallback<T> {

	public void done(T result);
	public void onFailed(AppacitiveError error);
	
}
