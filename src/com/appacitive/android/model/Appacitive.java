package com.appacitive.android.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.util.AppacitiveHelperMethods;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * An Appacitive object is the entry point to use the Appacitive SDK. All the
 * network requests are queued up here and sent to the remote service.
 * 
 * @author Sandeep Dhull
 * 
 */

public class Appacitive {

	private String mSessionId;
	private String mApiKey;
	private String mDeploymentId;
	private AppacitiveCallback mCallBack;
	private boolean enableDebugForEachRequest;
	private boolean enableLiveEnvironment;
	private static Appacitive mSharedInstance;

	private Appacitive(Context context, String apiKey, String deploymentId) {
		this.mApiKey = apiKey;
		// Context is required in case we are sending the broadcast
		// this.mContext = context;
		this.mDeploymentId = deploymentId;
		this.enableLiveEnvironment = false;
		fetchSession(apiKey, deploymentId);
	}

	/**
	 * Creates a shared object.
	 * 
	 * @param context
	 *            Handle To the context.
	 * @param apiKey
	 *            Application API Key.
	 * @param deploymentId
	 *            Application delploymentId Key.
	 * @param callback
	 *            Callback to the caller indication whether session is properly
	 *            initialized or failed.
	 */
	public static void initializeAppacitive(Context context, String apiKey,
			String deploymentId, AppacitiveCallback callback) {
		if (apiKey != null && deploymentId != null && !apiKey.isEmpty()
				&& !deploymentId.isEmpty()) {
			mSharedInstance = new Appacitive(context, apiKey, deploymentId);
			mSharedInstance.mCallBack = callback;
		}
	}

	private void fetchSession(final String apiKey, String deploymentId) {
		BackgroundTask<Void> backgroundTask = new BackgroundTask<Void>(null) {
			AppacitiveError error;

			@Override
			public Void run() {
				JSONObject requestParams = new JSONObject();
				try {
					requestParams.put("apikey", apiKey);
					requestParams.put("isnonsliding", false);
					requestParams.put("windowtime", 60);
					requestParams.put("usagecount", -1);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					URL url = new URL(Constants.SESSION_URL);
					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();
					connection.setRequestMethod(AppacitiveRequestMethods.PUT
							.requestMethod());
					connection.setRequestProperty("Content-Type",
							"application/json");
					connection.setRequestProperty("Content-Length", Integer
							.toString(((requestParams.toString()).length())));
					connection.setDoOutput(true);

					OutputStream os = connection.getOutputStream();
					os.write((requestParams.toString()).getBytes());
					os.close();

					InputStream inputStream;
					Map<String, Object> responseMap = null;
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.w("TAG",
								"Request failed "
										+ connection.getResponseMessage());
						error = new AppacitiveError();
						error.setStatusCode(connection.getResponseCode() + "");
						error.setMessage(connection.getResponseMessage());
					} else {
						inputStream = connection.getInputStream();
						InputStreamReader reader = new InputStreamReader(
								inputStream);
						BufferedReader bufferedReader = new BufferedReader(
								reader);
						StringBuffer buffer = new StringBuffer();
						String response;
						while ((response = bufferedReader.readLine()) != null) {
							buffer.append(response);
						}
						Gson gson = new Gson();
						Type typeOfClass = new TypeToken<Map<String, Object>>() {
						}.getType();
						responseMap = gson.fromJson(buffer.toString(),
								typeOfClass);
						error = AppacitiveHelperMethods
								.checkForErrorInStatus(responseMap);
						inputStream.close();
					}
					if (error != null) {
						mCallBack.onFailure(error);
					} else {
						readSessionKey(responseMap);
						mCallBack.onSuccess();
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			private void readSessionKey(Map<String, Object> response) {
				@SuppressWarnings("unchecked")
				Map<String, String> sessionMap = (Map<String, String>) response
						.get("session");
				Appacitive.this.mSessionId = (String) sessionMap
						.get("sessionkey");
			}
		};
		backgroundTask.execute();
	}

	/**
	 * @return Returns the shared instance of Appacitive.
	 */
	public static Appacitive getInstance() {
		return mSharedInstance;
	}

	/**
	 * End the currently active session. Any further request to appactive wont
	 * be made after the session is ended.
	 */
	public static void endSession() {
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
	 * This method will enable/disable debugging each request made to
	 * appacitive.
	 * 
	 * @param enableDebugForEachRequest
	 *            Enable/Disable the Debugging for each request.
	 */
	public void setEnableDebugForEachRequest(boolean enableDebugForEachRequest) {
		this.enableDebugForEachRequest = enableDebugForEachRequest;
	}

	/**
	 * @return Return the current environment. True indicates that current
	 *         environment is live, else the current environment is false.
	 */
	public boolean isEnableLiveEnvironment() {
		return enableLiveEnvironment;
	}

	/**
	 * By default the environment is set to sandbox. To change to live set the
	 * enableLiveEnvironment property of the Appacitive object.
	 * 
	 * @param enableLiveEnvironment
	 *            Enables/Disables live environment.
	 */
	public void setEnableLiveEnvironment(boolean enableLiveEnvironment) {
		this.enableLiveEnvironment = enableLiveEnvironment;
	}

	/**
	 * @return Return the deployment id.
	 */
	public String getDeploymentId() {
		return this.mDeploymentId;
	}

	/**
	 * @return Returns the current Environment.
	 */
	public String getEnvironment() {
		if (enableLiveEnvironment) {
			return "live";
		}
		return "sandbox";
	}

}
