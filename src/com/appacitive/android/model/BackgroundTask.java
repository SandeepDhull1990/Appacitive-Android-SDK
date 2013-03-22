package com.appacitive.android.model;

import android.os.AsyncTask;


abstract class BackgroundTask<T> extends AsyncTask<Void, Void, T> {
	
	private AppacitiveInternalCallback<T> mCallback;
	private AppacitiveError mNetworkError;
	
	public BackgroundTask(AppacitiveInternalCallback<T> callback) {
		this.mCallback = callback;
	}
	
	public abstract T run();

	protected T doInBackground(Void... params) {
		return run();
	}

	@Override
	protected void onPostExecute(T result) {
		if(mNetworkError == null) {
			mCallback.done(result);
		} else {
			mCallback.onFailed(mNetworkError);
		}
	}
	
	public void setNetworkError(AppacitiveError error) {
		this.mNetworkError = error;
	}
}