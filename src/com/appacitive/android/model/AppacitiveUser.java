package com.appacitive.android.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveAuthenticationCallback;
import com.appacitive.android.callbacks.AppacitiveSignUpCallback;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * AppacitiveUser represent your app's users whose management API's are provided
 * out of the box. They are internally simply articles of an inbuilt schema user
 * with added features of authorization, authentication , location tracking and
 * third-party social integration.
 * 
 * @author Sandeep Dhull
 */
public class AppacitiveUser extends AppacitiveObject {

	public String mUserToken;
	public static AppacitiveUser currentUser;
	private boolean mLoggedInWithFacebook;
	private boolean mLoggedInWithTwitter;

	private AppacitiveUser() {
		super("user");
		
	}

	/**
	 * Set the signed in user as current user.
	 * 
	 * @param user
	 *            AppacitiveUser current user.
	 */
	public static void setCurrentUser(AppacitiveUser user) {
		currentUser = user;
	}

	/**
	 * Returns the currently logged in user.
	 * 
	 * @return the current logged in user.
	 */
	public static AppacitiveUser getCurrentUser() {
		return currentUser;
	}

	/**
	 * Authenticate a user using username and password.
	 * 
	 * @param userName
	 *            Existing user username.
	 * @param password
	 *            user password required for authentication.
	 */
	public static void authenticate(String userName, String password) {
		authenticate(userName, password, null);
	}

	
	/**
	 * Returns the user id of the current user.
	 * @return user id.
	 */
	public long getUserId() {
		return this.mObjectId;
	}
	
	/**
	 * Returns the user name of the current user.
	 * @return user name.
	 */
	public String getUserName() {
		return (String)this.getProperty("username");
	}
	
	/**
	 * Returns the user's first name. 
	 * @return first name of the user.
	 * @throws NullPointerException
	 */
	public String getFirstName() {
		return (String)this.getProperty("firstname");
	}
	
	/**
	 * Returns the user's last name.
	 * @return last name of the user.
	 * @throws NullPointerException
	 */
	public String getLastName() {
		return (String)this.getProperty("lastname");
	}
	
	/**
	 * Returns the birth date of the user.
	 * @return The birth date of the user.
	 * @throws NullPointerException
	 */
	public String getBirthdate() {
		return (String)this.getProperty("birthdate");
	}
	
	/**
	 * Returns the phone number of the user.
	 * @return user phone number.
	 * @throws NullPointerException
	 */
	public String getPhoneNumber() {
		return (String)this.getProperty("phone");
	}
	
	/**
	 * Returns the location of the user.
	 * @return user's location.
	 * @throws NullPointerException
	 */
	public String getLocation() {
		return (String)this.getProperty("location");
	}
	
	/**
	 * Authenticate a user using username and password.
	 * 
	 * @param userName
	 *            Existing user username.
	 * @param password
	 *            user password.
	 * @param callback
	 *            callback invoked when the authentication is successful or
	 *            failed.
	 */
	public static void authenticate(final String userName, final String password, final AppacitiveAuthenticationCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		
		AppacitiveInternalCallback<AppacitiveUserJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveUserJsonModel>() {
			
			@Override
			public void onFailed(AppacitiveError error) {
				if(callback != null) {
					callback.onFailure(error);
				}
			}
			
			@Override
			public void done(AppacitiveUserJsonModel result) {
				if(callback != null) {
					if(!result.mStatus.getStatusCode().equals("200")) {
						callback.onFailure(result.mStatus);
					} else {
						AppacitiveUser.currentUser = result.user;
						AppacitiveUser.currentUser.mUserToken = result.mToken;
						callback.onSuccess();
					}
				}
			}
		};
		
		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveUserJsonModel> autenticateTask = new BackgroundTask<AppacitiveUserJsonModel>(internalCallback) {
				@Override
				public AppacitiveUserJsonModel run() {
					
					AppacitiveUserJsonModel response = null;
					AppacitiveError error;
					HashMap<String, String> requestMap = new HashMap<String, String>();
					requestMap.put("username", userName);
					requestMap.put("password", password);

					Gson gson = new Gson();
					String requestParams = gson.toJson(requestMap);

					String urlString = Constants.USER_URL + "authenticate";
					try {
						URL url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Content-Type", "application/json");
						connection.setRequestProperty("Content-Length", Integer.toString(requestParams.length()));
						AppacitiveHelper.addHeaders(connection);
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
							Reader reader = new InputStreamReader(inputStream);
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveUser.class, new AppacitiveUserTypeAdapter());
							gson = builder.create();
							response = gson.fromJson(reader, AppacitiveUserJsonModel.class);
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
			autenticateTask.execute();
		} else {
			Log.w("Appacitive","Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	/**
	 * Authenticate the user using user's facebook credentials.
	 * 
	 * @param userAccessToken
	 *            facebook user access token.
	 */
	public static void authenticateWithFacebook(String userAccessToken) {
		authenticateWithFacebook(userAccessToken, null);
	}

	/**
	 * Authenticate a user using user's facebook credentials.
	 * 
	 * @param userAccessToken
	 *            facebook user access token.
	 * @param callback
	 *            callback invoked when the authentication is successful or failed.
	 */
	public static void authenticateWithFacebook(final String userAccessToken, final AppacitiveAuthenticationCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		
		AppacitiveInternalCallback<AppacitiveUserJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveUserJsonModel>() {
			
			@Override
			public void onFailed(AppacitiveError error) {
				if(callback != null) {
					callback.onFailure(error);
				}
			}
			
			@Override
			public void done(AppacitiveUserJsonModel result) {
				if(callback != null) {
					if(!result.mStatus.getStatusCode().equals("200")) {
						callback.onFailure(result.mStatus);
					} else {
						AppacitiveUser.currentUser = result.user;
						AppacitiveUser.currentUser.mUserToken = result.mToken;
						AppacitiveUser.currentUser.setLoggedInWithFacebook(true);
						callback.onSuccess();
					}
				}
			}
		};
		
		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveUserJsonModel> autenticateTask = new BackgroundTask<AppacitiveUserJsonModel>(internalCallback) {

				@Override
				public AppacitiveUserJsonModel run() {

					AppacitiveError error;
					AppacitiveUserJsonModel response = null;
					
					String urlString = Constants.USER_URL + "authenticate";
					HashMap<String, Object> requestMap = new HashMap<String, Object>();
					requestMap.put("type", "facebook");
					requestMap.put("accesstoken", userAccessToken);
					requestMap.put("createnew", true);
					Gson gson = new Gson();
					String requestParams = gson.toJson(requestMap);
					try {
						URL url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
						AppacitiveHelper.addHeaders(connection);
						connection.setDoOutput(true);

						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();
						
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.d("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							Reader reader = new InputStreamReader(inputStream);
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveUser.class, new AppacitiveUserTypeAdapter());
							gson = builder.create();
							response = gson.fromJson(reader, AppacitiveUserJsonModel.class);
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
			autenticateTask.execute();
		} else {
			Log.w("Appacitive", "Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	/**
	 * Authenticate a user using user's twitter credentials.
	 * 
	 * @param oauthToken
	 *            Twitter oauthToken key.
	 * @param oauthSecret
	 *            Twitter oauthSecret key.
	 * @param consumerKey
	 *            Twitter consumer Key.
	 * @param consumerSecret
	 *            Twitter consumer secret key.
	 */
	public static void authenticateWithTwitter(String oauthToken, String oauthSecret, String consumerKey, String consumerSecret) {
		authenticateWithTwitter(oauthToken, oauthSecret, consumerKey,
				consumerSecret, null);
	}

	/**
	 * Authenticate a user using user's twitter credentials.
	 * 
	 * @param oauthToken
	 *            Twitter oauthToken key.
	 * @param oauthSecret
	 *            Twitter oauthSecret key.
	 * @param consumerKey
	 *            Twitter consumer Key.
	 * @param consumerSecret
	 *            Twitter consumer secret key.
	 * @param callback
	 *            callback invoked when the authentication is successful or
	 *            failed.
	 */
	public static void authenticateWithTwitter(final String oauthToken, final String oauthSecret, final String consumerKey,
			final String consumerSecret, final AppacitiveAuthenticationCallback callback) {
		
		final Appacitive appacitive = Appacitive.getInstance();
		
		AppacitiveInternalCallback<AppacitiveUserJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveUserJsonModel>() {
			
			@Override
			public void onFailed(AppacitiveError error) {
				if(callback != null) {
					callback.onFailure(error);
				}
			}
			
			@Override
			public void done(AppacitiveUserJsonModel result) {
				if(callback != null) {
					if(!result.mStatus.getStatusCode().equals("200")) {
						callback.onFailure(result.mStatus);
					} else {
						AppacitiveUser.currentUser = result.user;
						AppacitiveUser.currentUser.mUserToken = result.mToken;
						AppacitiveUser.currentUser.setLoggedInWithTwitter(true);
						callback.onSuccess();
					}
				}
			}
		};
		
		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveUserJsonModel> autenticateTask = new BackgroundTask<AppacitiveUserJsonModel>(internalCallback) {

				@Override
				public AppacitiveUserJsonModel run() {
					AppacitiveError error;
					AppacitiveUserJsonModel response = null;
					
					String urlString = Constants.USER_URL + "authenticate";
					HashMap<String, Object> requestMap = new HashMap<String, Object>();
					requestMap.put("type", "twitter");
					requestMap.put("createnew", true);
					requestMap.put("oauthtoken", oauthToken);
					requestMap.put("oauthtokensecret", oauthSecret);
					requestMap.put("consumerKey", consumerKey);
					requestMap.put("consumerSecret", consumerSecret);
					Gson gson = new Gson();
					String requestParams = gson.toJson(requestMap);
					try {
						URL url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Content-Type", "application/json");
						connection.setRequestProperty("Content-Length", Integer.toString(requestParams.length()));
						AppacitiveHelper.addHeaders(connection);
						connection.setDoOutput(true);

						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.d("TAG", "Error : " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							Reader reader = new InputStreamReader(inputStream);
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveUser.class, new AppacitiveUserTypeAdapter());
							gson = builder.create();
							response = gson.fromJson(reader, AppacitiveUserJsonModel.class);
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
			autenticateTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	/**
	 * Create a new user with the provided user details.
	 * 
	 * @param userDetails
	 *            User details.
	 * @param callback
	 *            callback invoked when the signup is successful or failed.
	 */
	public static void createUser(final AppacitiveUserDetail userDetails, final AppacitiveSignUpCallback callback) {

		final Appacitive sharedObject = Appacitive.getInstance();
		
		AppacitiveInternalCallback<AppacitiveUserJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveUserJsonModel>() {

			@Override
			public void done(AppacitiveUserJsonModel result) {
				if(callback != null) {
					if(result != null && result.mStatus.getStatusCode().equals("200")) {
						callback.onSuccess(result.user);
					} else {
						callback.onFailure(result.mStatus);
					}
				}
			}

			@Override
			public void onFailed(AppacitiveError error) {
				if(callback != null) {
					callback.onFailure(error);
				}
			}
		};
		
		if (sharedObject != null && sharedObject.getSessionId() != null) {

			BackgroundTask<AppacitiveUserJsonModel> createTask = new BackgroundTask<AppacitiveUserJsonModel>(internalCallback) {
				
				@Override
				public AppacitiveUserJsonModel run() {
					AppacitiveUserJsonModel response = null;
					AppacitiveError error;

					String urlString = Constants.USER_URL + "create";
					String requestParams = userDetails.createRequestParams();
					try {
						URL url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
						connection.setRequestProperty("Content-Type", "application/json");
						connection.setRequestProperty("Content-Length", Integer.toString(requestParams.length()));
						AppacitiveHelper.addHeaders(connection);
						connection.setDoOutput(true);

						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.d("TAG", "Error : " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							Reader reader = new InputStreamReader(inputStream);
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveUser.class, new AppacitiveUserTypeAdapter());
							Gson gson = builder.create();
							response = gson.fromJson(reader, AppacitiveUserJsonModel.class);
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
			createTask.execute();
		} else {
			Log.w("Appacitive", "Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	/**
	 * Returns the string representation of the object.
	 * 
	 * @return String representation of the object.
	 */
	@Override
	public String toString() {
		return "AppacitiveUser [mUserToken=" + mUserToken + "]" + super.toString();
	}

	public boolean isLoggedInWithFacebook() {
		return mLoggedInWithFacebook;
	}

	private void setLoggedInWithFacebook(boolean mLoggedInWithFacebook) {
		this.mLoggedInWithFacebook = mLoggedInWithFacebook;
	}

	public boolean isLoggedInWithTwitter() {
		return mLoggedInWithTwitter;
	}

	private void setLoggedInWithTwitter(boolean mLoggedInWithTwitter) {
		this.mLoggedInWithTwitter = mLoggedInWithTwitter;
	}

	private static class AppacitiveUserTypeAdapter extends TypeAdapter<AppacitiveUser> {

		@Override
		public AppacitiveUser read(JsonReader jsonReader) throws IOException {
			AppacitiveUser appacitiveUser = new AppacitiveUser();
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
				return null;
			} else {
				jsonReader.beginObject();
			}

			while (jsonReader.hasNext()) {
				if (jsonReader.peek() == JsonToken.NULL) {
					jsonReader.skipValue();
				}

				String name = jsonReader.nextName();

				if (name.equals("__id") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setObjectId(jsonReader.nextLong());
				} else if (name.equals("__schematype") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setSchemaType(jsonReader.nextString());
				} else if (name.equals("__schemaid") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setSchemaId(jsonReader.nextLong());
				} else if (name.equals("__revision") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setRevision(jsonReader.nextLong());
				} else if (name.equals("__createdby") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setCreatedBy(jsonReader.nextString());
				} else if (name.equals("__lastmodifiedby") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setLastModifiedBy(jsonReader.nextString());
				} else if (name.equals("__tags") && jsonReader.peek() != JsonToken.NULL) {
					fetchTags(jsonReader, appacitiveUser);
				} else if (name.equals("__utcdatecreated") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setUTCDateCreated(fromJsonResponse(jsonReader.nextString()));
				} else if (name.equals("__utclastupdateddate") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveUser.setUTCLastUpdatedDate(fromJsonResponse(jsonReader.nextString()));
				} else if (name.equals("__attributes") && jsonReader.peek() != JsonToken.NULL) {
					fetchAttributes(jsonReader, appacitiveUser);
				} else if (!name.equals("") && !name.startsWith("__")) {
					appacitiveUser.addProperty(name, jsonReader.nextString());
				} else {
					jsonReader.skipValue();
				}
			}
			jsonReader.endObject();
			return appacitiveUser;
		}

		private void fetchTags(JsonReader jsonReader, AppacitiveUser appacitiveUser) throws IOException {
			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				appacitiveUser.addTag(jsonReader.nextString());
			}
			jsonReader.endArray();
		}

		@SuppressLint("SimpleDateFormat")
		private Date fromJsonResponse(String dateString) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date date = null;
			try {
				date = formatter.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}

		private void fetchAttributes(JsonReader reader, AppacitiveUser appacitiveUser) throws IOException {
			reader.beginObject();
			while (reader.hasNext()) {
				if (reader.peek() == JsonToken.NULL) {
					reader.skipValue();
				}
				String key = reader.nextName();
				String value = reader.nextString();
				appacitiveUser.addAttribute(key, value);
			}
			reader.endObject();
		}

		@Override
		public void write(JsonWriter writer, AppacitiveUser user) throws IOException {
		}
	}

	public class AppacitiveUserJsonModel {
		@SerializedName("token")
		public String mToken;
		@SerializedName("user")
		AppacitiveUser user;
		@SerializedName("status")
		public AppacitiveError mStatus;
	}
}
