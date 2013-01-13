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

// TODO : Do the proper Documentation
public class AppacitiveObject {

	private String mCreatedBy;
	private String mSchemaType;
	private String mLastModifiedBy;
	private long mObjectId;
	private long mRevision = -999999;
	private long mSchemaId;
	private HashMap<String, Object> mProperties;
	private HashMap<String, Object> mAttributes;
	private List<String> mTags;
	private Date mUTCDateCreated;
	private Date mUTCLastUpdatedDate;

	/**
	 * Adds a property to the APObject
	 * 
	 * @param key
	 *            The Field For which the value needs to be set
	 * @param value
	 *            The Value which is to be set
	 */
	public void addProperty(String key, Object value) {
		if (this.mProperties == null) {
			this.mProperties = new HashMap<String, Object>();
		}
		this.mProperties.put(key, value);
	}

	/**
	 * Adds a attruibute to an APObject
	 * 
	 * @param key
	 *            The Field For which the value needs to be set
	 * @param value
	 *            The Value which is to be set
	 */
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

	// Setters and Getters
	public String getCreatedBy() {
		return mCreatedBy;
	}

	public void setCreatedBy(String createdBy) {
		this.mCreatedBy = createdBy;
	}

	public String getSchemaType() {
		return mSchemaType;
	}

	public void setSchemaType(String schemaType) {
		this.mSchemaType = schemaType;
	}

	public String getLastModifiedBy() {
		return mLastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.mLastModifiedBy = lastModifiedBy;
	}

	public long getObjectId() {
		return mObjectId;
	}

	public void setObjectId(long objectId) {
		this.mObjectId = objectId;
	}

	public long getRevision() {
		return mRevision;
	}

	public void setRevision(long revision) {
		this.mRevision = revision;
	}

	public long getSchemaId() {
		return mSchemaId;
	}

	public void setSchemaId(long schemaId) {
		this.mSchemaId = schemaId;
	}

	public List<String> getTags() {
		return mTags;
	}

	public void setTags(List<String> tags) {
		this.mTags = tags;
	}

	public Date getUTCDateCreated() {
		return mUTCDateCreated;
	}

	public void setUTCDateCreated(Date utcDateCreated) {
		this.mUTCDateCreated = utcDateCreated;
	}

	public Date getUTCLastUpdatedDate() {
		return mUTCLastUpdatedDate;
	}

	public void setUTCLastUpdatedDate(Date utcLastUpdatedDate) {
		this.mUTCLastUpdatedDate = utcLastUpdatedDate;
	}

	private JSONObject createPostParameters() {
		// Create the required JSON over here
		HashMap<String, Object> requestParams = new HashMap<String, Object>();
		if (this.mAttributes != null) {
			requestParams.put("__attributes", this.mAttributes);
		}
		if (this.mCreatedBy != null) {
			requestParams.put("__createdby", this.mCreatedBy);
		}
		if (this.mSchemaType != null) {
			requestParams.put("__schematype", mSchemaType);
		}
		if (this.mTags != null) {
			requestParams.put("__tags", this.mTags);
		}
		if (this.mRevision != -999999) {
			requestParams.put("__revision", this.mRevision);
		}
		if (this.mProperties != null) {
			for (String key : this.mProperties.keySet()) {
				requestParams.put(key, this.mProperties.get(key));
			}
		}
		JSONObject object = null;
		try {
			object = AppacitiveUtility.toJSON(requestParams);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * Save the article on the remote server.
	 * 
	 * This method will save an article in the background. If save is successful
	 * the properties will be updated and no callback will be invoked.
	 */
	public void saveObject() {
		saveObject(null);
	}

	/**
	 * Save the article on the remote server.
	 * 
	 * This method will save an article in the background. If save is successful
	 * the properties will be updated and the success callback will be invoked.
	 * If not the failure callback is invoked.
	 */
	public void saveObject(final AppacitiveResponseCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();

		if (appacitive != null) {
			BackgroundTask<Void> saveTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() {
					URL url;

					try {
						url = new URL(Constants.ARTICLE_URL
								+ AppacitiveObject.this.mSchemaType);
						JSONObject requestParams = AppacitiveObject.this
								.createPostParameters();
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
								readResponse(jsonReader);
							} else {
								BufferedReader bufferedReader = new BufferedReader(
										reader);
								StringBuffer buffer = new StringBuffer();
								String response;
								while ((response = bufferedReader.readLine()) != null) {
									buffer.append(response);
								}
								JSONObject responseJsonObject = new JSONObject(
										buffer.toString());
								JSONObject statusObject = responseJsonObject
										.getJSONObject("status");
								error = AppacitiveHelperMethods
										.checkForErrorInStatus(statusObject);
								if (error == null) {
									readArticle(responseJsonObject
											.getJSONObject("article"));
								}
							}
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSucess();
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

				private void readResponse(JsonReader jsonReader)
						throws IOException {
					jsonReader.beginObject();
					String name;
					while (jsonReader.hasNext()) {
						if (jsonReader.peek() == JsonToken.NULL) {
							jsonReader.skipValue();
						}
						name = jsonReader.nextName();
						if (name.equals("article")
								&& jsonReader.peek() != JsonToken.NULL) {
							readArticle(jsonReader);
						} else if (name.equals("status")
								&& jsonReader.peek() != JsonToken.NULL) {
							error = AppacitiveHelperMethods
									.checkForErrorInStatus(jsonReader);
						}
					}
				}
			};
			saveTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	/**
	 * Deletes the article on the remote server.
	 * 
	 * This method will delete an article in the background. If deletion is
	 * successful or unsuccessful no callback will be invoked.
	 */
	public void deleteObject() {
		deleteObjectWithConnections(false, null);
	}

	/**
	 * Delete the article on the remote server.
	 * 
	 * This method will delete an article in the background. If deletion is
	 * successful the success callback will be invoked. If not the failure
	 * callback is invoked.
	 */
	public void deleteObjectWithConnections(final boolean deleteConnections,
			final AppacitiveResponseCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						String urlString = Constants.ARTICLE_URL
								+ AppacitiveObject.this.mSchemaType + "/"
								+ AppacitiveObject.this.mObjectId;
						if(deleteConnections) {
							urlString = urlString + "?deleteconnections=true";
						}
						url = new URL(urlString);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.DELETE.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							return null;
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(inputStream);
							if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
								JsonReader jsonReader = new JsonReader(reader);
								error = AppacitiveHelperMethods.checkForErrorInStatus(jsonReader);
							} else {
								BufferedReader bufferedReader = new BufferedReader(reader);
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
								callback.onSucess();
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

	public static void deleteObjectsWithIds(final ArrayList<String> objectIds,
			final String schemaName, final AppacitiveResponseCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						url = new URL(Constants.ARTICLE_URL + schemaName
								+ "/bulkdelete");
						JSONArray arrayIds = new JSONArray(objectIds);
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
								callback.onSucess();
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

	public void fetchObject(final AppacitiveResponseCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> fetchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						url = new URL(Constants.ARTICLE_URL
								+ AppacitiveObject.this.mSchemaType + "/"
								+ AppacitiveObject.this.mObjectId);
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
								JsonReader jsonReader = new JsonReader(reader);
								jsonReader.beginObject();
								String name;
								while (jsonReader.hasNext()) {
									if (jsonReader.peek() == null) {
										jsonReader.skipValue();
									}
									name = jsonReader.nextName();
									if (name.equals("article")
											&& jsonReader.peek() != JsonToken.NULL) {
										readArticle(jsonReader);
									} else if (name.equals("status")
											&& jsonReader.peek() != JsonToken.NULL) {
										error = AppacitiveHelperMethods
												.checkForErrorInStatus(jsonReader);
									} else {
										jsonReader.skipValue();
									}
								}
								jsonReader.endObject();
								jsonReader.close();
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
											.checkForErrorInStatus(responseJsonObject
													.getJSONObject("status"));
									if (error == null) {
										readArticle(responseJsonObject
												.getJSONObject("article"));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSucess();
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
			fetchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	public static void fetchObjectsWithIds(final ArrayList<String> ids,
			final String schemaName, final FetchCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> fetchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						StringBuffer queryParams = null;
						for (String id : ids) {
							if (queryParams == null) {
								queryParams = new StringBuffer();
								queryParams.append(id);
							} else {
								queryParams.append("," + id);
							}

						}
						url = new URL(Constants.ARTICLE_URL + schemaName
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
								// TODO : Ask ali whehter to return JSON
								// Response or an arraylist of the
								// apobject
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

	public static void searchAllObjects(String schemaName,
			final FetchCallback callback) {
		searchObjects(schemaName, null, callback);
	}

	public static void searchObjects(final String schemaName,
			final String filter, final FetchCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> searchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					String urlString = Constants.ARTICLE_URL + schemaName
							+ "/find/all";
					if (filter != null) {
						urlString = urlString + "?" + filter;
					}
					Log.d("TAG", "" + urlString);
					try {
						url = new URL(urlString.replace(" ", "%20"));
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
								// One question whether the paging is to be
								// handled by the api or the developer
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
							inputStream.close();
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

	private void readArticle(JsonReader jsonReader) throws IOException {
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
			}
			String name = jsonReader.nextName();
			if (name.equals("__id") && jsonReader.peek() != JsonToken.NULL) {
				this.mObjectId = jsonReader.nextLong();
			} else if (name.equals("__schemaid")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mSchemaId = jsonReader.nextLong();
			} else if (name.equals("__createdby")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mCreatedBy = jsonReader.nextString();
			} else if (name.equals("__lastmodifiedby")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mLastModifiedBy = jsonReader.nextString();
			} else if (name.equals("__revision")
					&& jsonReader.peek() != JsonToken.NULL) {
				this.mRevision = jsonReader.nextLong();
			} else if (name.equals("__utcdatecreated")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					this.mUTCDateCreated = fromJsonResponse(jsonReader
							.nextString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__utclastupdateddate")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					this.mUTCLastUpdatedDate = fromJsonResponse(jsonReader
							.nextString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				jsonReader.skipValue();
			}
		}
		jsonReader.endObject();
	}

	private void readArticle(JSONObject jsonObject) throws IOException,
			JSONException {
		this.mObjectId = jsonObject.getLong("__id");
		this.mSchemaId = jsonObject.getLong("__schemaid");
		this.mCreatedBy = jsonObject.getString("__createdby");
		this.mLastModifiedBy = jsonObject.getString("__lastmodifiedby");
		this.mRevision = jsonObject.getLong("__revision");
		try {
			this.mUTCDateCreated = fromJsonResponse(jsonObject
					.getString("__utcdatecreated"));
			this.mUTCLastUpdatedDate = fromJsonResponse(jsonObject
					.getString("__utclastupdateddate"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private Date fromJsonResponse(String dateString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date date = formatter.parse(dateString);
		return date;
	}

	@Override
	public String toString() {
		return "AppacitiveObject :--> mCreatedBy=" + mCreatedBy
				+ ", mSchemaType=" + mSchemaType + ", mLastModifiedBy="
				+ mLastModifiedBy + ", mObjectId=" + mObjectId + ", mRevision="
				+ mRevision + ", mSchemaId=" + mSchemaId + ", mUTCDateCreated="
				+ mUTCDateCreated + ", mUTCLastUpdatedDate="
				+ mUTCLastUpdatedDate + "]";
	}

}
