package com.appacitive.android.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * An Appacitive object is the entry point to use the Appacitive SDK. All the
 * network requests are queued up here and sent to the remote service.
 * 
 */


public class Appacitive {

	/*
	 *  Session Id of the currently active session 
	 */
	@SerializedName("sessionkey")
	private String mSessionId;
	
	/*
	 * Appacitive Callback, to notify the user the progress of session initialization.
	 */
	private AppacitiveCallback mCallBack;
	
	private boolean enableDebugForEachRequest;
	
	/*
	 * Controls the current appacitive working environment.
	 */
	private boolean enableLiveEnvironment;
	
	/*
	 * Singleton instance of Appacitive.
	 */
	private static Appacitive mSharedInstance;
	
	/*
	 * Private Constructor to enforce singleton pattern.
	 */
	private Appacitive(String apiKey, AppacitiveCallback callback) {
		this.mCallBack = callback;
		this.enableLiveEnvironment = false;
		fetchSession(apiKey);
	}

	/**
	 * Creates a shared object.
	 * @param apiKey
	 *            Application API Key.
	 * @param callback
	 *            Callback to the caller indication whether session is properly
	 *            initialized or failed.
	 */
	
	public static void initializeAppacitive(String apiKey, AppacitiveCallback callback) {
		if (apiKey != null && !TextUtils.isEmpty(apiKey) && mSharedInstance == null) {
			synchronized (Appacitive.class) {
				if (apiKey != null && !TextUtils.isEmpty(apiKey) && mSharedInstance == null) {
					mSharedInstance = new Appacitive(apiKey, callback);
				}
			}
		} else {
			AppacitiveError error = new AppacitiveError();
			error.setStatusCode(Constants.APPACITIVE_ERROR_INVALID_API);
			error.setMessage("api key can't be null or empty.");
			if(callback != null) {
				callback.onFailure(error);
			}
		}
	}

	private void fetchSession(final String apiKey) {

		AppacitiveInternalCallback<AppacitiveJsonModel> callback = new AppacitiveInternalCallback<AppacitiveJsonModel>() {
			
			@Override
			public void done(AppacitiveJsonModel results) {
				if (!results.error.getStatusCode().equals("200")) {
					mCallBack.onFailure(results.error);
				} else {
					Appacitive.this.mSessionId = results.mAppacitiveObject.getSessionId();
					mCallBack.onSuccess();
				}
			}
			
			@Override
			public void onFailed(AppacitiveError error) {
				if(mCallBack != null) {
					mCallBack.onFailure(error);
				}
			}
		};
		
		BackgroundTask<AppacitiveJsonModel> backgroundTask = new BackgroundTask<AppacitiveJsonModel>(callback) {
			@Override
			public AppacitiveJsonModel run() {
				
				AppacitiveJsonModel response = null;
				AppacitiveError error;
				
				Map<String, Object> requestMap = new HashMap<String, Object>();
				requestMap.put("apikey", apiKey);
				requestMap.put("isnonsliding", false);
				requestMap.put("windowtime", 60);
				requestMap.put("usagecount", -1);
				Gson gson = new Gson();
				String requestParams = gson.toJson(requestMap);
				try {
					URL url = new URL(Constants.SESSION_URL);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
					connection.setRequestProperty("Content-Type","application/json");
					connection.setRequestProperty("Content-Length", Integer.toString(((requestParams.toString()).length())));
					connection.setDoOutput(true);

					OutputStream os = connection.getOutputStream();
					os.write((requestParams.toString()).getBytes());
					os.close();

					InputStream inputStream;
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.w("TAG", "Error : " + connection.getResponseMessage());
						error = new AppacitiveError();
						error.setStatusCode(connection.getResponseCode());
						error.setMessage(connection.getResponseMessage());
						this.setNetworkError(error);
					} else {
						inputStream = connection.getInputStream();
						GsonBuilder builder = new GsonBuilder();
						Reader reader = new InputStreamReader(inputStream);
						gson = builder.create();
				        response = gson.fromJson(reader, AppacitiveJsonModel.class);
				        inputStream.close();
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					error = new AppacitiveError();
					error.setMessage(e.getMessage());
					this.setNetworkError(error);
				}
				return response;
			}
		};
		backgroundTask.execute();
	}

	/**
	 * Returns the singleton instance of Appacitive. If instance is null, that means the
	 * session is not initialized.
	 * 
	 * @return Returns the shared instance of Appacitive.
	 */
	public static synchronized Appacitive getInstance() {
		return mSharedInstance;
	}

	/**
	 * End the currently active session. Any further request to appacitive won't
	 * be successful after ending session.
	 */
	public void endSession() {
		mSharedInstance.mSessionId = null;
		mSharedInstance = null;
	}

	/**
	 * @return Returns the currently active session key.
	 */
	public String getSessionId() {
		return mSessionId;
	}

	/**
	 * @return Returns true if debugging is enabled for each request, otherwise
	 *         returns false.
	 */
	public boolean isEnableDebugForEachRequest() {
		return enableDebugForEachRequest;
	}

	/**
	 * This method will enable/disable debugging each request made to appacitive
	 * hence.
	 * 
	 * @param enableDebugForEachRequest
	 *            Enable/Disable the Debugging for each request.
	 */
	public void setEnableDebugForEachRequest(boolean enableDebugForEachRequest) {
		this.enableDebugForEachRequest = enableDebugForEachRequest;
	}

	/**
	 * @return Return the current environment. True indicates that current
	 *         environment is live, and false means the current environment is
	 *         sandbox.
	 */
	public boolean isEnableLiveEnvironment() {
		return enableLiveEnvironment;
	}

	/**
	 * By default the environment is set to sandbox. To change the environment
	 * to live pass true as an argument.
	 * 
	 * @param enableLiveEnvironment
	 *            Enables/Disables live environment.
	 */
	public void setEnableLiveEnvironment(boolean enableLiveEnvironment) {
		this.enableLiveEnvironment = enableLiveEnvironment;
	}

	/**
	 * Returns "live" if the current environment is "live", otherwise returns
	 * "sandbox".
	 * 
	 * @return Returns the current Environment.
	 */
	public String getEnvironment() {
		if (enableLiveEnvironment) {
			return "live";
		}
		return "sandbox";
	}

	/*
	 * Helper inner class for parsing response to get the session key. 
	 *
	 */
	private class AppacitiveJsonModel {
		@SerializedName("session")
		public Appacitive mAppacitiveObject;
		
		@SerializedName("status")
		public AppacitiveError error;
	}
	
}
