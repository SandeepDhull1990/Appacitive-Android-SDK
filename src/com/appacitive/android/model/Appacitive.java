package com.appacitive.android.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;

public class Appacitive {

	private Context mContext;
	private String mSessionId;
	private String mApiKey;
	private String mDeploymentId;
	private AppacitiveResponseCallback mCallBack;
	private boolean enableDebugForEachRequest;
	private boolean enableLiveEnvironment;
	static private Appacitive mSharedInstance;

	private Appacitive(Context context, String apiKey, String deploymentId) {
		this.mApiKey = apiKey;
		this.mContext = context;
		this.mDeploymentId = deploymentId;
		fetchSession(apiKey, deploymentId);
	}

	public static void initializeAppacitive(Context context, String apiKey,
			String deploymentId, AppacitiveResponseCallback callback) {
		if (apiKey != null && deploymentId != null && !apiKey.isEmpty()
				&& !deploymentId.isEmpty()) {
			mSharedInstance = new Appacitive(context, apiKey, deploymentId);
			mSharedInstance.mCallBack = callback;
		}
	}

	private void fetchSession(final String apiKey, String deploymentId) {
		// Fetching the session on the background task
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
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					} else {
						inputStream = connection.getInputStream();
						readSessionInformation(inputStream);
						inputStream.close();
						if (error != null) {
							mCallBack.onFailure(error);
						} else {
							mCallBack.onSucess();
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			private void readSessionInformation(InputStream inputStream)
					throws IOException {
				InputStreamReader in = new InputStreamReader(inputStream);
				JsonReader reader = new JsonReader(in);
				reader.beginObject();
				while (reader.hasNext()) {
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					}
					String name = reader.nextName();
					if (name.equals("session")
							&& reader.peek() != JsonToken.NULL) {
						readSessionKey(reader);
					} else if (name.equals("status")
							&& reader.peek() != JsonToken.NULL) {
						error = AppacitiveHelperMethods
								.checkForErrorInStatus(reader);
					}
				}
				reader.endObject();
				reader.close();
			}

			private void readSessionKey(JsonReader reader) throws IOException {
				reader.beginObject();
				String name;
				while (reader.hasNext()) {
					if (reader.peek() == JsonToken.NULL) {
						reader.skipValue();
					}
					name = reader.nextName();
					if (name.equals("sessionkey") && reader.peek() != JsonToken.NULL) {
						String sessionKey = reader.nextString();
						Appacitive.this.mSessionId = sessionKey;
					} else {
						reader.skipValue();
					}
				}
				reader.endObject();
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
