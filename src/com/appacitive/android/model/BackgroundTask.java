package com.appacitive.android.model;

import android.os.AsyncTask;

abstract class BackgroundTask<T> extends AsyncTask<Void, Void, Void>
{
  private AppacitiveResponseCallback callback;
  private T result;
  private AppacitiveException exception;

  BackgroundTask(AppacitiveResponseCallback theCallback)
  {
    this.result = null;
    this.exception = null;
    this.callback = theCallback;
  }

  public abstract T run() throws AppacitiveException;

  protected Void doInBackground(Void[] params) {
    try
    {
      this.result = run();
      return null;
    } catch (AppacitiveException e) {
      this.exception = e;
    }
	   return null;
  }

//  On post execute sending the callback
  protected void onPostExecute(Void v) {
//    if (this.callback != null)
//      this.callback.internalDone(this.result, this.exception);
  }

  void executeInThisThread()
  {
    doInBackground(new Void[0]);
    onPostExecute(null);
  }

  static int executeTask(BackgroundTask<?> task)
  {
    task.execute(new Void[0]);
    return 0;
  }
}