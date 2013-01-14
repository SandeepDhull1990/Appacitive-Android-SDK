package com.appacitive.android.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.AppacitiveUtility;
import com.appacitive.android.util.Constants;

public class AppacitiveConnection {

	private String mCreatedBy;
	private long mArticleAId;
	private long mArticleBId;
	private long mConnectionId;
	private String mLabelA;
	private String mLabelB;
	private long mRelationId;
	private String mRelationType;
	private String mLastModifiedBy;
	private Date mUtcDateCreated;
	private Date mUtcLastModifiedDate;
	private long mRevision;
	private HashMap<String, Object> mProperties;
	private HashMap<String, Object> mAttributes;
	private List<String> mTags;

	public AppacitiveConnection(String relationType) {
		this.mRelationType = relationType;
	}

	public void addAttribute(String key, Object value) {
		if (this.mAttributes == null) {
			this.mAttributes = new HashMap<String, Object>();
		}
		this.mAttributes.put(key, value);
	}

	public void addTag(String tag) {
		if (this.mTags == null) {
			this.mTags = new ArrayList<String>();
		}
		this.mTags.add(tag);
	}

	public String getCreatedBy() {
		return mCreatedBy;
	}

	public void setCreatedBy(String createdBy) {
		this.mCreatedBy = createdBy;
	}

	public long getArticleAId() {
		return mArticleAId;
	}

	public void setArticleAId(long articleAId) {
		this.mArticleAId = articleAId;
	}

	public long getArticleBId() {
		return mArticleBId;
	}

	public void setArticleBId(long articleBId) {
		this.mArticleBId = articleBId;
	}

	public long getConnectionId() {
		return mConnectionId;
	}

	public void setConnectionId(long objectId) {
		this.mConnectionId = objectId;
	}

	public String getLabelA() {
		return mLabelA;
	}

	public void setLabelA(String labelA) {
		this.mLabelA = labelA;
	}

	public String getLabelB() {
		return mLabelB;
	}

	public void setLabelB(String labelB) {
		this.mLabelB = labelB;
	}

	public long getRelationId() {
		return mRelationId;
	}

	public void setRelationId(long relationId) {
		this.mRelationId = relationId;
	}

	public String getRelationType() {
		return mRelationType;
	}

	public String getLastModifiedBy() {
		return mLastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.mLastModifiedBy = lastModifiedBy;
	}

	public Date getUtcDateCreated() {
		return mUtcDateCreated;
	}

	public void setUtcDateCreated(Date utcDateCreated) {
		this.mUtcDateCreated = utcDateCreated;
	}

	public Date getUtcLastModifiedDate() {
		return mUtcLastModifiedDate;
	}

	public void setUtcLastModifiedDate(Date utcLastModifiedDate) {
		this.mUtcLastModifiedDate = utcLastModifiedDate;
	}

	public long getRevision() {
		return mRevision;
	}

	public void setRevision(long revision) {
		this.mRevision = revision;
	}

	// create connection
	public void createConnection() {
		this.createConnection(null);
	}

	public void createConnection(final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> createTask = new BackgroundTask<Void>(null) {
				AppacitiveError appacitiveError;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					JSONObject requestParams = null;
					try {
						url = new URL(Constants.CONNECTION_URL
								+ AppacitiveConnection.this.mRelationType);
						requestParams = AppacitiveConnection.this
								.createRequestParams();
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.PUT
										.requestMethod());
						connection.setRequestProperty("Content-Type",
								"application/json");
						connection.setRequestProperty("Content-Length",
								Integer.toString(((requestParams.toString())
										.length())));
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setDoOutput(true);
						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.d("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(
									inputStream);
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
								JsonReader jsonReader = new JsonReader(reader);
								readResponse(jsonReader);
							} else {
								BufferedReader bufferedReader = new BufferedReader(
										reader);
								StringBuffer buffer = new StringBuffer();
								String response;
								while ((response = bufferedReader.readLine()) != null) {
									buffer.append(response);
								}
								JSONObject responseJsonObject;
								try {
									responseJsonObject = new JSONObject(
											buffer.toString());
									JSONObject statusObject = responseJsonObject
											.getJSONObject("status");
									appacitiveError = AppacitiveHelperMethods
											.checkForErrorInStatus(statusObject);
									if (appacitiveError == null) {
										readConnection(responseJsonObject
												.getJSONObject("connection"));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							inputStream.close();
						}
						if (callback != null) {
							if (appacitiveError == null) {
								callback.onSuccess();
							} else {
								callback.onFailure(appacitiveError);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}

				private void readResponse(JsonReader jsonReader)
						throws IOException {
					jsonReader.beginObject();
					String name;
					while (jsonReader.hasNext()) {
						name = jsonReader.nextName();
						if (name.equals("connection")
								&& jsonReader.peek() != JsonToken.NULL) {
							readAppacitiveConnection(jsonReader);
						} else if (name.equals("status")
								&& jsonReader.peek() != JsonToken.NULL) {
							appacitiveError = AppacitiveHelperMethods
									.checkForErrorInStatus(jsonReader);
						} else {
							jsonReader.skipValue();
						}
					}
				}
			};
			createTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}

	}

	public void createConnection(AppacitiveObject object1,
			AppacitiveObject object2) {
		this.createConnection(object1, object2, null);
	}

	public void createConnection(final AppacitiveObject objectA,
			final AppacitiveObject objectB, final AppacitiveCallback callback) {
		this.mArticleAId = objectA.getObjectId();
		this.mLabelA = objectA.getSchemaType();
		this.mArticleBId = objectB.getObjectId();
		this.mLabelB = objectB.getSchemaType();
		this.createConnection(callback);
	}

	public void deleteConnection() {
		deleteConnection(null);
	}

	public void deleteConnection(final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						String urlString = Constants.CONNECTION_URL
								+ AppacitiveConnection.this.mRelationType + "/"
								+ AppacitiveConnection.this.mConnectionId;
						url = new URL(urlString);

						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.DELETE
										.requestMethod());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(
									inputStream);
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
								JsonReader jsonReader = new JsonReader(reader);
								error = AppacitiveHelperMethods
										.checkForErrorInStatus(jsonReader);
							} else {
								BufferedReader bufferedReader = new BufferedReader(
										reader);
								StringBuffer buffer = new StringBuffer();
								String response;
								while ((response = bufferedReader.readLine()) != null) {
									buffer.append(response);
								}
								JSONObject responseJsonObject;
								try {
									responseJsonObject = new JSONObject(
											buffer.toString());
									error = AppacitiveHelperMethods
											.checkForErrorInStatus(responseJsonObject);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSuccess();
							} else {
								callback.onFailure(error);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					return null;
				}
			};
			deleteTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}

	}

	public static void deleteConnections(ArrayList<String> connectionsIds,
			String relationType) {
		deleteConnections(connectionsIds, relationType, null);
	}

	public static void deleteConnections(
			final ArrayList<String> connectionsIds, final String relationType,
			final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						url = new URL(Constants.CONNECTION_URL + relationType
								+ "/bulkdelete");
						JSONArray arrayIds = new JSONArray(connectionsIds);
						JSONObject requestObject = new JSONObject();
						requestObject.put("idlist", arrayIds);
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.POST
										.requestMethod());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
						connection.setRequestProperty("Content-Type",
								"application/json");
						connection.setRequestProperty("Content-Length",
								Integer.toString(((requestObject.toString())
										.length())));
						OutputStream os = connection.getOutputStream();
						os.write((requestObject.toString()).getBytes());
						os.close();
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(
									inputStream);
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
								JsonReader jsonReader = new JsonReader(reader);
								error = AppacitiveHelperMethods
										.checkForErrorInStatus(jsonReader);
							} else {
								BufferedReader bufferedReader = new BufferedReader(
										reader);
								StringBuffer buffer = new StringBuffer();
								String response;
								while ((response = bufferedReader.readLine()) != null) {
									buffer.append(response);
								}
								JSONObject responseJsonObject;
								try {
									responseJsonObject = new JSONObject(
											buffer.toString());
									error = AppacitiveHelperMethods
											.checkForErrorInStatus(responseJsonObject);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSuccess();
							} else {
								callback.onFailure(error);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			deleteTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	public void fetchConnection() {
		fetchConnection(null);
	}

	public void fetchConnection(final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> fetchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						String urlString = Constants.CONNECTION_URL
								+ AppacitiveConnection.this.mRelationType + "/"
								+ AppacitiveConnection.this.mConnectionId;
						url = new URL(urlString);

						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.GET
										.requestMethod());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.d("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(
									inputStream);
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
								JsonReader jsonReader = new JsonReader(reader);
								readResponse(jsonReader);
							} else {
								BufferedReader bufferedReader = new BufferedReader(
										reader);
								StringBuffer buffer = new StringBuffer();
								String response;
								while ((response = bufferedReader.readLine()) != null) {
									buffer.append(response);
								}
								JSONObject responseJsonObject;
								try {
									responseJsonObject = new JSONObject(
											buffer.toString());
									JSONObject statusObject = responseJsonObject
											.getJSONObject("status");
									error = AppacitiveHelperMethods
											.checkForErrorInStatus(statusObject);
									if (error == null) {
										readConnection(responseJsonObject
												.getJSONObject("connection"));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSuccess();
							} else {
								callback.onFailure(error);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}

				private void readResponse(JsonReader jsonReader)
						throws IOException {
					jsonReader.beginObject();
					String name;
					while (jsonReader.hasNext()) {
						name = jsonReader.nextName();
						if (name.equals("connection")
								&& jsonReader.peek() != JsonToken.NULL) {
							readAppacitiveConnection(jsonReader);
						} else if (name.equals("status")
								&& jsonReader.peek() != JsonToken.NULL) {
							error = AppacitiveHelperMethods
									.checkForErrorInStatus(jsonReader);
						} else {
							jsonReader.skipValue();
						}
					}
				}
			};
			fetchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}

	}

	public static void fetchConnections(final ArrayList<String> connectionIds,
			final String relationType) {
		AppacitiveConnection
				.fetchConnections(connectionIds, relationType, null);
	}

	public static void fetchConnections(final ArrayList<String> connectionIds,
			final String relationType, final FetchCallback callback) {

		if (connectionIds == null) {
			AppacitiveError error = new AppacitiveError();
			error.setMessage("ConnectionId's list is empty. Pass the list of connectionId which you want to fetch.");
			Log.w("TAG", error.getMessage());
			if (callback != null) {
				callback.onFailure(error);
			}
		}

		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> fetchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						StringBuffer queryParams = null;
						for (String id : connectionIds) {
							if (queryParams == null) {
								queryParams = new StringBuffer();
								queryParams.append(id);
							} else {
								queryParams.append("," + id);
							}

						}
						url = new URL(Constants.CONNECTION_URL + relationType
								+ "/multiget" + "/" + queryParams);
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.GET
										.requestMethod());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							return null;
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
							JSONObject responseJsonObject;
							try {
								responseJsonObject = new JSONObject(
										buffer.toString());
								error = AppacitiveHelperMethods
										.checkForErrorInStatus(responseJsonObject
												.getJSONObject("status"));
								if (callback != null) {
									if (error == null) {
										callback.onSuccess(responseJsonObject);
									} else {
										callback.onFailure(error);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						inputStream.close();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			fetchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	public static void searchAllConnections(String relationType) {
		searchConnections(relationType, null, null);
	}

	public static void searchAllConnections(String relationType,
			FetchCallback callback) {
		searchConnections(relationType, null, callback);
	}

	public static void searchConnections(final String relationType,
			final String query, final FetchCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> searchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;
				AppacitivePagingInfo pagingInfo;

				@Override
				public Void run() throws AppacitiveException {
					URL url = null;
					String urlString = Constants.CONNECTION_URL + relationType
							+ "/find/all";
					try {
						if (query != null) {
							urlString = urlString + "?" + query;
						}
						url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.GET
										.requestMethod());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
						InputStream inputStream;

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							return null;
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
							JSONObject responseJsonObject;
							try {
								responseJsonObject = new JSONObject(
										buffer.toString());
								error = AppacitiveHelperMethods
										.checkForErrorInStatus(responseJsonObject
												.getJSONObject("status"));
								if (callback != null) {
									if (error == null) {
										callback.onSuccess(responseJsonObject);
									} else {
										callback.onFailure(error);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			searchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	private void readConnection(JSONObject jsonObject) throws IOException,
			JSONException {
		this.mConnectionId = jsonObject.getLong("__id");
		this.mRelationType = jsonObject.getString("__relationtype");
		this.mRelationId = jsonObject.getLong("__relationid");
		this.mCreatedBy = jsonObject.getString("__createdby");
		this.mLastModifiedBy = jsonObject.getString("__lastmodifiedby");
		this.mRevision = jsonObject.getLong("__revision");
		JSONObject endPointA = jsonObject.getJSONObject("__endpointa");
		this.mLabelA = endPointA.getString("label");
		this.mArticleAId = Long.parseLong(endPointA.getString("articleid"));
		JSONObject endPointB = jsonObject.getJSONObject("__endpointb");
		this.mLabelA = endPointB.getString("label");
		this.mArticleAId = Long.parseLong(endPointB.getString("articleid"));

		try {
			this.mUtcDateCreated = fromJsonResponse(jsonObject
					.getString("__utcdatecreated"));
			this.mUtcLastModifiedDate = fromJsonResponse(jsonObject
					.getString("__utclastupdateddate"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void readAppacitiveConnection(JsonReader jsonReader)
			throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			String name = jsonReader.nextName();
			if (name.equals("__id") && jsonReader.peek() != JsonToken.NULL) {
				this.mConnectionId = jsonReader.nextLong();
			} else if (name.equals("__relationtype")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mRelationType = jsonReader.nextString();
			} else if (name.equals("__relationid")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mRelationId = jsonReader.nextLong();
			} else if (name.equals("__revision")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mRevision = jsonReader.nextLong();
			} else if (name.equals("__createdby")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mCreatedBy = jsonReader.nextString();
			} else if (name.equals("__lastmodifiedby")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mLastModifiedBy = jsonReader.nextString();
			} else if (name.equals("__utcdatecreated")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					this.mUtcDateCreated = fromJsonResponse(jsonReader
							.nextString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__utclastupdateddate")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					this.mUtcLastModifiedDate = fromJsonResponse(jsonReader
							.nextString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__endpointa")
					&& jsonReader.peek() != JsonToken.NULL) {
				readEndPointA(jsonReader);
			} else if (name.equals("__endpointb")
					&& jsonReader.peek() != JsonToken.NULL) {
				readEndPointB(jsonReader);
			} else {
				jsonReader.skipValue();
			}
		}
		jsonReader.endObject();
	}

	private void readEndPointA(JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
			}
			String name = jsonReader.nextName();
			if (name.equals("label") && jsonReader.peek() != JsonToken.NULL) {
				this.mLabelA = jsonReader.nextString();
			} else if (name.equals("articleid")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mArticleAId = jsonReader.nextLong();
			} else {
				jsonReader.skipValue();
			}
		}
	}

	private void readEndPointB(JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
			}
			String name = jsonReader.nextName();
			if (name.equals("label") && jsonReader.peek() != JsonToken.NULL) {
				this.mLabelB = jsonReader.nextString();
			} else if (name.equals("articleid")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mArticleBId = jsonReader.nextLong();
			} else {
				jsonReader.skipValue();
			}
		}
	}

	private JSONObject createRequestParams() {
		HashMap<String, Object> requestParams = new HashMap<String, Object>();
		if (this.mRelationType != null) {
			requestParams.put("__relationtype", this.mRelationType);
		}
		if (this.mAttributes != null) {
			requestParams.put("__attributes", this.mAttributes);
		}
		if (this.mTags != null) {
			requestParams.put("__tags", this.mTags);
		}
		HashMap<String, Object> endPointA = new HashMap<String, Object>();
		endPointA.put("label", this.mLabelA);
		endPointA.put("articleid", this.mArticleAId + "");
		HashMap<String, Object> endPointB = new HashMap<String, Object>();
		endPointB.put("label", this.mLabelB);
		endPointB.put("articleid", this.mArticleBId + "");
		requestParams.put("__endpointa", endPointA);
		requestParams.put("__endpointb", endPointB);
		JSONObject object = null;
		try {
			object = AppacitiveUtility.toJSON(requestParams);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}

	private Date fromJsonResponse(String dateString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date date = formatter.parse(dateString);
		return date;
	}

	@Override
	public String toString() {
		return "AppacitiveConnection [mCreatedBy=" + mCreatedBy
				+ ", mArticleAId=" + mArticleAId + ", mArticleBId="
				+ mArticleBId + ", mObjectId=" + mConnectionId + ", mLabelA="
				+ mLabelA + ", mLabelB=" + mLabelB + ", mRelationId="
				+ mRelationId + ", mRelationType=" + mRelationType
				+ ", mLastModifiedBy=" + mLastModifiedBy + ", mUtcDateCreated="
				+ mUtcDateCreated + ", mUtcLastModifiedDate="
				+ mUtcLastModifiedDate + ", mRevision=" + mRevision
				+ ", mProperties=" + mProperties + ", mAttributes="
				+ mAttributes + ", mTags=" + mTags + "]";
	}
}
