package com.appacitive.android.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.util.Log;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class AppacitiveUser extends AppacitiveObject {

	public AppacitiveUser(String schemaType) {
		super(schemaType);
	}

	public String mUserToken;
	public static AppacitiveUser currentUser;

	public static void setCurrentUser(AppacitiveUser user) {
		currentUser = user;
	}

	public static AppacitiveUser getCurrentUser() {
		return currentUser;
	}
	
	public static void authenticate(String userName, String password) {
		authenticate(userName, password, null);
	}
	
	public static void authenticate(final String userName, final String password, final AppacitiveAuthenticationCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if(appacitive != null) {
			BackgroundTask<Void> autenticateTask = new BackgroundTask<Void>(null) {
				@Override
				public Void run() throws AppacitiveException {
					String urlString = Constants.USER_URL + "authenticate";
					HashMap<String,String> requestMap = new HashMap<String, String>();
					requestMap.put("username", userName);
					requestMap.put("password", password);
					Gson gson = new Gson();
					String requestParams = gson.toJson(requestMap);
					try {
						URL url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setDoOutput(true);
						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
							JsonReader reader = new JsonReader(inputStreamReader);
							currentUser = new AppacitiveUser("user");
							AppacitiveError error = currentUser.setNewPropertyValue(reader);
							if(callback != null) {
								if(error == null) {
									callback.onSuccess();
								} else {
									callback.onFailure(error);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			autenticateTask.execute();
		} else {
			Log.w("Appacitive","Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}
	
	public static void authenticateWithFacebook(String userAccessToken) {
		authenticateWithFacebook(userAccessToken, null);
	}
	
	public static void authenticateWithFacebook(final String userAccessToken, final AppacitiveAuthenticationCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if(appacitive != null) {
			BackgroundTask<Void> autenticateTask = new BackgroundTask<Void>(null) {
				@Override
				public Void run() throws AppacitiveException {
					String urlString = Constants.USER_URL + "authenticate";
					HashMap<String,String> requestMap = new HashMap<String, String>();
					requestMap.put("type", "facebook");
					requestMap.put("accesstoken", userAccessToken);
					requestMap.put("createNew", "true");
					Gson gson = new Gson();
					String requestParams = gson.toJson(requestMap);
					try {
						URL url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setDoOutput(true);
						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
							JsonReader reader = new JsonReader(inputStreamReader);
							currentUser = new AppacitiveUser("user");
							AppacitiveError error = currentUser.setNewPropertyValue(reader);
							if(callback != null) {
								if(error == null) {
									callback.onSuccess();
								} else {
									callback.onFailure(error);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			autenticateTask.execute();
		} else {
			Log.w("Appacitive","Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}
	
	public static void authenticateWithTwitter(String oauthToken, String oauthSecret, String consumerKey,String consumerSecret) {
		authenticateWithTwitter(oauthToken, oauthSecret, consumerKey, consumerSecret, null);
	}
	
	public static void authenticateWithTwitter(final String oauthToken, final String oauthSecret, final String consumerKey, final String consumerSecret, final AppacitiveAuthenticationCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if(appacitive != null) {
			BackgroundTask<Void> autenticateTask = new BackgroundTask<Void>(null) {
				@Override
				public Void run() throws AppacitiveException {
					String urlString = Constants.USER_URL + "authenticate";
					HashMap<String,String> requestMap = new HashMap<String, String>();
					requestMap.put("type", "twitter");
					requestMap.put("createNew", "true");
					requestMap.put("oauthtoken", oauthToken);
					requestMap.put("oauthtokensecret", oauthSecret);
					Gson gson = new Gson();
					String requestParams = gson.toJson(requestMap);
					try {
						URL url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setDoOutput(true);
						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
							JsonReader reader = new JsonReader(inputStreamReader);
							currentUser = new AppacitiveUser("user");
							AppacitiveError error = currentUser.setNewPropertyValue(reader);
							if(callback != null) {
								if(error == null) {
									callback.onSuccess();
								} else {
									callback.onFailure(error);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			autenticateTask.execute();
		} else {
			Log.w("Appacitive","Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}
	
	public static void createUser(final AppacitiveUserDetail userDetails,
			final AppacitiveSignUpCallback callback) {

		final Appacitive sharedObject = Appacitive.getInstance();
		if (sharedObject != null) {

			BackgroundTask<Void> createTask = new BackgroundTask<Void>(null) {

				@Override
				public Void run() throws AppacitiveException {
					String urlString = Constants.USER_URL + "create";
					try {
						URL url = new URL(urlString);
						String requestParams = userDetails.createRequestParams();
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString(requestParams.length()));
						connection.setRequestProperty("Appacitive-Session",sharedObject.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",sharedObject.getEnvironment());
						connection.setDoOutput(true);
						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
							JsonReader reader = new JsonReader(inputStreamReader);
							AppacitiveUser user = new AppacitiveUser("user");
							AppacitiveError error = user.setNewPropertyValue(reader);
							if(callback != null) {
								if(error == null) {
									callback.onSuccess(user);
								} else {
									callback.onFailure(error);
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			createTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	public AppacitiveError setNewPropertyValue(JsonReader reader) throws IOException {
		AppacitiveError error = null;
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("user") && reader.peek() != JsonToken.NULL) {
				this.readUser(reader);
			} else if (name.equals("status") && reader.peek() != JsonToken.NULL) {
				error = AppacitiveHelperMethods.checkForErrorInStatus(reader);
			} else if (name.equals("token") && reader.peek() != JsonToken.NULL) {
				this.mUserToken = reader.nextString();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return error;
	}

	private void readUser(JsonReader reader) throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("__id") && reader.peek() != JsonToken.NULL) {
				this.setObjectId(reader.nextLong());
			} else if (name.equals("__createdby")
					&& reader.peek() != JsonToken.NULL) {
				this.setCreatedBy(reader.nextString());
			} else if (name.equals("__lastmodifiedby")
					&& reader.peek() != JsonToken.NULL) {
				this.setLastModifiedBy(reader.nextString());
			} else if (name.equals("__revision")
					&& reader.peek() != JsonToken.NULL) {
				this.setRevision(reader.nextLong());
			} else if (name.equals("__schemaid")
					&& reader.peek() != JsonToken.NULL) {
				this.setSchemaId(reader.nextLong());
			} else if (name.equals("__utcdatecreated")
					&& reader.peek() != JsonToken.NULL) {
				try {
					this.setUTCDateCreated(fromJsonResponse(reader.nextString()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__utclastupdateddate")
					&& reader.peek() != JsonToken.NULL) {
				try {
					this.setUTCLastUpdatedDate(fromJsonResponse(reader
							.nextString()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__attributes")
					&& reader.peek() != JsonToken.NULL) {
				reader.beginObject();
				while (reader.hasNext()) {
					reader.beginObject();
					while (reader.hasNext()) {
						String key = reader.nextName();
						if (reader.peek() != JsonToken.NULL) {
							this.addAttribute(key, reader.nextString());
						} else {
							reader.skipValue();
						}
					}
					reader.endObject();
				}
				reader.endObject();
			} else if (name.equals("__tags") && reader.peek() != JsonToken.NULL) {
				reader.beginArray();
				while (reader.hasNext()) {
					this.addTag(reader.nextString());
				}
				reader.endArray();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
	}

	private Date fromJsonResponse(String dateString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date date = formatter.parse(dateString);
		return date;
	}

	@Override
	public String toString() {
		return "AppacitiveUser [mUserToken=" + mUserToken + "]"
				+ super.toString();
	}
}
