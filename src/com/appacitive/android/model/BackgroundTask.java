package com.appacitive.android.model;

import android.os.AsyncTask;

//TODO : Rethink this class Design
abstract class BackgroundTask<T> extends AsyncTask<Void, Void, Void> {

	public abstract T run();

	protected Void doInBackground(Void... params) {
		run();
		return null;
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