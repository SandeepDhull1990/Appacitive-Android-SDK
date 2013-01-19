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
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Appacitive {

	// Context will be used if we broadcast that the session is created
	// private Context mContext;
	private String mSessionId;
	private String mApiKey;
	private String mDeploymentId;
	private AppacitiveCallback mCallBack;
	private boolean enableDebugForEachRequest;
	private boolean enableLiveEnvironment;
	static private Appacitive mSharedInstance;

	private Appacitive(Context context, String apiKey, String deploymentId) {
		this.mApiKey = apiKey;
		// Context is required in case we are sending the broadcast
		// this.mContext = context;
		this.mDeploymentId = deploymentId;
		fetchSession(apiKey, deploymentId);
	}

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
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
					connection.setRequestProperty("Content-Type","application/json");
					connection.setRequestProperty("Content-Length", Integer.toString(((requestParams.toString()).length())));
					connection.setDoOutput(true);

					OutputStream os = connection.getOutputStream();
					os.write((requestParams.toString()).getBytes());
					os.close();

					InputStream inputStream;
					Map<String, Object> responseMap = null;
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						Log.w("TAG","Request failed " + connection.getResponseMessage());
						error = new AppacitiveError();
						error.setStatusCode(connection.getResponseCode() + "");
						error.setMessage(connection.getResponseMessage());
					} else {
						inputStream = connection.getInputStream();
						InputStreamReader reader = new InputStreamReader(inputStream);
						BufferedReader bufferedReader = new BufferedReader(reader);
						StringBuffer buffer = new StringBuffer();
						String response;
						while ((response = bufferedReader.readLine()) != null) {
							buffer.append(response);
						}
						Gson gson = new Gson();
						Type typeOfClass = new TypeToken<Map<String, Object>>() {}.getType();
						responseMap = gson.fromJson(buffer.toString(),typeOfClass);
						error = AppacitiveHelperMethods.checkForErrorInStatus(responseMap);
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
				Map<String,String> sessionMap = (Map<String, String>) response.get("session");
				Appacitive.this.mSessionId = (String) sessionMap.get("sessionkey");
			}
		};
		backgroundTask.execute();
	}

	public static Appacitive getInstance() {
		return mSharedInstance;
	}

	public static void endSession() {
		mSharedInstance = null;
	}

	public String getSessionId() {
		return mSessionId;
	}

	public boolean isEnableDebugForEachRequest() {
		return enableDebugForEachRequest;
	}

	public void setEnableDebugForEachRequest(boolean enableDebugForEachRequest) {
		this.enableDebugForEachRequest = enableDebugForEachRequest;
	}

	public boolean isEnableLiveEnvironment() {
		return enableLiveEnvironment;
	}

	public void setEnableLiveEnvironment(boolean enableLiveEnvironment) {
		this.enableLiveEnvironment = enableLiveEnvironment;
	}

	public String getDeploymentId() {
		return this.mDeploymentId;
	}

	public String getEnvironment() {
		if (enableLiveEnvironment) {
			return "live";
		}
		return "sandbox";
	}

}
