package com.appacitive.android.model;

import android.os.AsyncTask;

import com.appacitive.android.callbacks.AppacitiveCallback;

//TODO : Rethink this class Design
abstract class BackgroundTask<T> extends AsyncTask<Void, Void, Void> {
	private AppacitiveCallback callback;
	private T result;

	BackgroundTask(AppacitiveCallback theCallback) {
		this.result = null;
		this.callback = theCallback;
	}

	public abstract T run();

	protected Void doInBackground(Void[] params) {
		this.result = run();
		return null;
	}

	// On post execute sending the callback
	protected void onPostExecute(Void v) {
		// if (this.callback != null)
		// this.callback.internalDone(this.result, this.exception);
	}

	void executeInThisThread() {
		doInBackground(new Void[0]);
		onPostExecute(null);
	}

	static int executeTask(BackgroundTask<?> task) {
		task.execute(new Void[0]);
		return 0;
	}
}