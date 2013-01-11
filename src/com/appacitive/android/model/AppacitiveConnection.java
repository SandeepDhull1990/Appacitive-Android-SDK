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
	private long mObjectId;
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

	public long getObjectId() {
		return mObjectId;
	}

	public void setObjectId(long objectId) {
		this.mObjectId = objectId;
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

	public void setRelationType(String relationType) {
		this.mRelationType = relationType;
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
		this.createConnection(null, null, null);
	}

	public void createConnection(AppacitiveObject object1,
			AppacitiveObject object2) {
		this.createConnection(object1, object2, null);
	}

	public void createConnection(final AppacitiveObject object1,
			final AppacitiveObject object2,
			final AppacitiveResponseCallback callback) {

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
						if (object1 != null && object2 != null) {
							requestParams = AppacitiveConnection.this
									.createRequestParams(object1, object2);
						} else if (object1 == null || object2 == null) {
							requestParams = AppacitiveConnection.this
									.createRequestParams();
						}

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
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
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
								callback.onSucess();
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
						if (jsonReader.peek() == JsonToken.NULL) {
							jsonReader.skipValue();
						}
						name = jsonReader.nextName();
						if (name.equals("connection")
								&& jsonReader.peek() != JsonToken.NULL) {
							readConnection(jsonReader);
						} else if (name.equals("status")
								&& jsonReader.peek() != JsonToken.NULL) {
							appacitiveError = AppacitiveHelperMethods
									.checkForErrorInStatus(jsonReader);
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

	protected JSONObject createRequestParams(AppacitiveObject object1,
			AppacitiveObject object2) {

		// Create the required JSON over here
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
		// endPointA.put("label", this.mLabelA);
		// endPointA.put("articleid", this.mArticleAId+"");
		endPointA.put("label", object1.getSchemaType());
		endPointA.put("articleid", object1.getObjectId() + "");
		HashMap<String, Object> endPointB = new HashMap<String, Object>();
		// endPointB.put("label", this.mLabelB);
		// endPointB.put("articleid", this.mArticleBId+"");
		endPointB.put("label", object2.getSchemaType());
		endPointB.put("articleid", object2.getObjectId() + "");
		requestParams.put("__endpointa", endPointA);
		requestParams.put("__endpointb", endPointB);
		JSONObject object = null;

		try {
			object = AppacitiveUtility.toJSON(requestParams);
			Log.d("TAG", "The request Object ");
			Log.d("TAG", "" + object.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}

	public void searchConnectionWithRelationTypeAndQueryString(
			String relationType, final String query,
			final AppacitiveResponseCallback callback) {
		this.mRelationType = relationType;
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> searchTask = new BackgroundTask<Void>(null) {
				AppacitiveError appacitiveError;
				AppacitivePagingInfo pagingInfo;

				@Override
				public Void run() throws AppacitiveException {
					URL url = null;
					try {
						if (query == null) {
							url = new URL(Constants.CONNECTION_URL
									+ AppacitiveConnection.this.mRelationType
									+ "/find/all");
						} else if (query != null) {
							// append to request params
							url = new URL(Constants.CONNECTION_URL
									+ AppacitiveConnection.this.mRelationType
									+ "/find/all" + "&%@" + query);
						}
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
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
								readResponse(reader);
							} else { // versions before gingerbread
								BufferedReader bufferedReader = new BufferedReader(
										reader);
								StringBuffer buffer = new StringBuffer();
								String response;
								while ((response = bufferedReader.readLine()) != null) {
									buffer.append(response);
								}
								Log.d("TAG", "The object is ");
								Log.d("TAG", " " + buffer.toString());
								JSONObject responseJsonObject;
								try {
									responseJsonObject = new JSONObject(
											buffer.toString());
									appacitiveError = AppacitiveHelperMethods
											.checkForErrorInStatus(responseJsonObject
													.getJSONObject("status"));
									if (appacitiveError == null) {
										readConnections(responseJsonObject
												.getJSONArray("connections"));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							inputStream.close();

						}

						if (callback != null) {
							if (appacitiveError == null) {
								callback.onSucess();
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

				private void readResponse(InputStreamReader reader)
						throws IOException {
					JsonReader jsonReader = new JsonReader(reader);
					jsonReader.beginObject();
					String name;
					while (jsonReader.hasNext()) {
						if (jsonReader.peek() == null) {
							jsonReader.skipValue();
							continue;
						}
						name = jsonReader.nextName();
						if (name.equals("paginginfo")
								&& jsonReader.peek() != JsonToken.NULL) {
							pagingInfo = AppacitiveHelperMethods
									.readPagingInfo(jsonReader);
						} else if (name.equals("connections")
								&& jsonReader.peek() != JsonToken.NULL) {
							readConnections(jsonReader);
						} else if (name.equals("status")
								&& jsonReader.peek() != JsonToken.NULL) {
							appacitiveError = AppacitiveHelperMethods
									.checkForErrorInStatus(jsonReader);
						} else {
							jsonReader.skipValue();
						}
					}
					jsonReader.endObject();
					jsonReader.close();
				}

				private void readConnections(JsonReader jsonReader)
						throws IOException {
					// TODO Auto-generated method stub
					List<AppacitiveConnection> connections = new ArrayList<AppacitiveConnection>();
					jsonReader.beginArray();
					while (jsonReader.hasNext()) {
						connections.add(readConnection(jsonReader));
					}
					jsonReader.endArray();
				}

				private void readConnections(JSONArray jsonArray)
						throws JSONException, IOException {
					List<AppacitiveConnection> connections = new ArrayList<AppacitiveConnection>();
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject appacitiveConnectionJsonObject = jsonArray
								.getJSONObject(i);
						AppacitiveConnection connection = readConnection(appacitiveConnectionJsonObject);
						connections.add(connection);
					}
				}
			};
			searchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	public void searchForAllConnectionsWithRelationType(String relationType) {
		this.searchConnectionWithRelationTypeAndQueryString(relationType, null,
				null);
	}

	private AppacitiveConnection readConnection(JSONObject jsonObject)
			throws IOException, JSONException {
		this.mObjectId = jsonObject.getLong("__id");
		this.mRelationType = jsonObject.getString("__relationtype");
		this.mRelationId = jsonObject.getLong("__relationid");
		this.mCreatedBy = jsonObject.getString("__createdby");
		this.mLastModifiedBy = jsonObject.getString("__lastmodifiedby");
		// TODO : Uncomment the mRevision number and after the revision number
		// is provided by the service in the response
		// this.mRevision = jsonObject.getLong("__revision");
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
		return this;
	}

	protected AppacitiveConnection readConnection(JsonReader jsonReader)
			throws IOException {
		AppacitiveConnection connection = new AppacitiveConnection();
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
			}
			String name = jsonReader.nextName();
			if (name.equals("__id") && jsonReader.peek() != JsonToken.NULL) {
				connection.mObjectId = jsonReader.nextLong();
			} else if (name.equals("__relationtype")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mRelationType = jsonReader.nextString();
			} else if (name.equals("__relationid")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mRelationId = jsonReader.nextLong();
			} else if (name.equals("__revision")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mRevision = jsonReader.nextLong();
			} else if (name.equals("__createdby")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mCreatedBy = jsonReader.nextString();
			} else if (name.equals("__lastmodifiedby")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mLastModifiedBy = jsonReader.nextString();
			} else if (name.equals("__utcdatecreated")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					connection.mUtcDateCreated = fromJsonResponse(jsonReader
							.nextString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__utclastupdateddate")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					connection.mUtcLastModifiedDate = fromJsonResponse(jsonReader
							.nextString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__endpointa")
					&& jsonReader.peek() != JsonToken.NULL) {
				readEndPointA(connection, jsonReader);
			} else if (name.equals("__endpointb")
					&& jsonReader.peek() != JsonToken.NULL) {
				readEndPointB(connection, jsonReader);
			} else {
				jsonReader.skipValue();
			}
		}
		jsonReader.endObject();
		return connection;
	}

	private void readEndPointA(AppacitiveConnection connection,
			JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
			}
			String name = jsonReader.nextName();
			if (jsonReader.equals("label")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mLabelA = jsonReader.nextString();
			} else if (jsonReader.equals("articleid")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mArticleAId = jsonReader.nextLong();
			} else {
				jsonReader.skipValue();
			}
		}
		jsonReader.endObject();
	}

	private void readEndPointB(AppacitiveConnection connection,
			JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
			}
			String name = jsonReader.nextName();
			if (jsonReader.equals("label")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mLabelB = jsonReader.nextString();
			} else if (jsonReader.equals("articleid")
					&& jsonReader.peek() != JsonToken.NULL) {
				connection.mArticleBId = jsonReader.nextLong();
			} else {
				jsonReader.skipValue();
			}
		}
		jsonReader.endObject();
	}

	protected JSONObject createRequestParams() {
		AppacitiveObject object1 = new AppacitiveObject();
		object1.setSchemaType(this.mLabelA);
		object1.setObjectId(this.mArticleAId);
		AppacitiveObject object2 = new AppacitiveObject();
		object2.setSchemaType(this.mLabelB);
		object2.setObjectId(this.mArticleBId);
		return this.createRequestParams(object1, object2);
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
				+ mArticleBId + ", mObjectId=" + mObjectId + ", mLabelA="
				+ mLabelA + ", mLabelB=" + mLabelB + ", mRelationId="
				+ mRelationId + ", mRelationType=" + mRelationType
				+ ", mLastModifiedBy=" + mLastModifiedBy + ", mUtcDateCreated="
				+ mUtcDateCreated + ", mUtcLastModifiedDate="
				+ mUtcLastModifiedDate + ", mRevision=" + mRevision
				+ ", mProperties=" + mProperties + ", mAttributes="
				+ mAttributes + ", mTags=" + mTags + "]";
	}
}
