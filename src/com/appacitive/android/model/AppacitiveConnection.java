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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.callbacks.AppacitiveFetchCallback;
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
 * A Connection is represents an edge in a graph and is used to connect two
 * AppacitiveObjects. A Connection itself can store data in its properties and
 * attributes fields.
 * 
 * @author Sandeep Dhull
 */

@SuppressLint("SimpleDateFormat")
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
	private Map<String, Object> mProperties;
	private Map<String, Object> mAttributes;
	private List<String> mTags;

	/*
	 * Private Connection
	 */
	private AppacitiveConnection() {
	}

	/**
	 * Initialize an Connection object of the provided relation type.
	 * 
	 * @param relationType
	 *            The name of the relation. This is specified while creating the
	 *            schema.
	 */
	public AppacitiveConnection(String relationType) {
		this.mRelationType = relationType;
	}
	
	/**
	 * Method used to add an attibute to the Connection. Attributes are used to
	 * store extra information.
	 * 
	 * @param key
	 *            key of the data item to be stored.
	 * @param value
	 *            Corresponding value to the key.
	 */
	public void addAttribute(String key, Object value) {
		if (this.mAttributes == null) {
			this.mAttributes = new HashMap<String, Object>();
		}
		this.mAttributes.put(key, value);
	}

	/**
	 * Method used to get an attribute from the AppacitiveObject.
	 * 
	 * @param key key of the data item stored.
	 * @return value Corresponding value to the key.
	 */
	public Object getAttribute(String key) {
		if (this.mAttributes != null) {
			return this.mAttributes.get(key);
		}
		return null;
	}

	/**
	 * Method used to add a tag to the Connection.
	 * 
	 * @param tag
	 *            The tag to be added to the Connection.
	 */
	public void addTag(String tag) {
		if (this.mTags == null) {
			this.mTags = new ArrayList<String>();
		}
		this.mTags.add(tag);
	}
	
	/**
	 * Returns the list of tags with which object is tagged.
	 */
	public List<String> getTags() {
		return this.mTags;
	}

	/**
	 * Method used to add an property to the AppacitiveObject. Properties are
	 * used to store extra information.
	 * 
	 * @param key
	 *            key of the data item to be stored.
	 * @param value
	 *            Corresponding value to the key.
	 */
	public void addProperty(String key, Object value) {
		if (this.mProperties == null) {
			this.mProperties = new HashMap<String, Object>();
		}
		this.mProperties.put(key, value);
	}

	/**
	 * Method used to get an property from the AppacitiveObject.
	 * 
	 * @param key
	 *            key of the data item stored.
	 * @return value Corresponding value to the key.
	 */
	public Object getProperty(String key) {
		if (this.mProperties != null) {
			return this.mProperties.get(key);
		}
		return null;
	}

	public void updateConnectionFields(AppacitiveConnection connection) {
		this.mConnectionId = connection.mConnectionId;
		this.mCreatedBy = connection.mCreatedBy;
		this.mLastModifiedBy = connection.mLastModifiedBy;
		this.mArticleAId = connection.mArticleAId;
		this.mArticleBId = connection.mArticleBId;
		this.mLabelA = connection.mLabelA;
		this.mLabelB = connection.mLabelB;
		this.mAttributes = connection.mAttributes;
		this.mProperties = connection.mProperties;
		this.mRelationId = connection.mRelationId;
		this.mRelationType = connection.mRelationType;
		this.mRevision = connection.mRevision;
		this.mTags = connection.mTags;
		this.mUtcDateCreated = connection.mUtcDateCreated;
		this.mUtcLastModifiedDate = connection.mUtcLastModifiedDate;
	}

	/**
	 * Create an Connection between two aricles on the remote server. This
	 * method requires the articleAId, articleBId, labelA and labelB properties
	 * to be set.
	 */
	public void createConnection() {
		this.createConnection(null);
	}

	/**
	 * Create an Connection between two articles on the remote server. This
	 * method requires the articleAId, articleBId, labelA and labelB properties
	 * to be set.
	 * 
	 * @param callback
	 *            Callback invoked when the create operation is successful or
	 *            failed.
	 */
	public void createConnection(final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveError> internalCallback = new AppacitiveInternalCallback<AppacitiveError>() {

			@Override
			public void done(AppacitiveError error) {
				if (error == null) {
					callback.onSuccess();
				} else {
					callback.onFailure(error);
				}
			}

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}
		};

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveError> createTask = new BackgroundTask<AppacitiveError>(internalCallback) {
				AppacitiveError error;

				@Override
				public AppacitiveError run() {
					URL url;
					String requestParams = null;
					requestParams = AppacitiveConnection.this.createRequestParams();
					try {
						url = new URL(Constants.CONNECTION_URL + AppacitiveConnection.this.mRelationType);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
						connection.setRequestProperty("Content-Type", "application/json");
						connection.setRequestProperty("Content-Length",Integer.toString(((requestParams.toString()).length())));
						AppacitiveHelper.addHeaders(connection);
						connection.setDoOutput(true);

						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							Reader reader = new InputStreamReader(inputStream);
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveConnection.class, new AppacitiveConnectionTypeAdapter());
							Gson gson = builder.create();
							AppacitiveConnectionJsonModel response = gson.fromJson(reader,AppacitiveConnectionJsonModel.class);
							inputStream.close();

							if (!response.mStatus.getStatusCode().equals("200")) {
								error = response.mStatus;
							} else {
								AppacitiveConnection.this.updateConnectionFields(response.mAppacitiveConnection);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						error = new AppacitiveError();
						error.setMessage(e.getMessage());
						this.setNetworkError(error);
					}
					return error;
				}
			};
			createTask.execute();
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
	 * Creates an Connection between two articles.
	 * 
	 * @param objectA
	 *            The article to create a connection from.
	 * @param objectB
	 *            The article to create a connection to.
	 */
	public void createConnection(AppacitiveObject objectA, AppacitiveObject objectB) {
		this.createConnection(objectA, objectB, null);
	}

	/**
	 * Creates an Connection between two articles.
	 * 
	 * @param objectA
	 *            The article to create a connection from.
	 * @param objectB
	 *            The article to create a connection to.
	 * @param callback
	 *            Callback invoked when the create operation is successful or
	 *            failed.
	 */
	public void createConnection(AppacitiveObject objectA, AppacitiveObject objectB, AppacitiveCallback callback) {
		this.mArticleAId = objectA.getObjectId();
		this.mLabelA = objectA.getSchemaType();
		this.mArticleBId = objectB.getObjectId();
		this.mLabelB = objectB.getSchemaType();
		this.createConnection(callback);
	}

	/**
	 * Deletes the AppacitiveConnection.
	 */
	public void deleteConnection() {
		deleteConnection(null);
	}

	/**
	 * Deletes the Connection.
	 * 
	 * @param callback
	 *            Callback invoked when the delete operation is successful or
	 *            failed.
	 */
	public void deleteConnection(final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveError> internalCallback = new AppacitiveInternalCallback<AppacitiveError>() {

			@Override
			public void done(AppacitiveError error) {
				if (error == null) {
					callback.onSuccess();
				} else {
					callback.onFailure(error);
				}
			}

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}

		};

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveError> deleteTask = new BackgroundTask<AppacitiveError>(internalCallback) {
				AppacitiveError error;

				@Override
				public AppacitiveError run() {
					URL url;
					try {
						String urlString = Constants.CONNECTION_URL + AppacitiveConnection.this.mRelationType + "/"
								+ AppacitiveConnection.this.mConnectionId;
						url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.DELETE.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveConnection.class, new AppacitiveConnectionTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							AppacitiveError response = gson.fromJson(reader, AppacitiveError.class);
							inputStream.close();
							if (!response.getStatusCode().equals("200")) {
								error = response;
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						error = new AppacitiveError();
						error.setMessage(e.getMessage());
						this.setNetworkError(error);
					}
					return error;
				}
			};
			deleteTask.execute();
		} else {
			Log.w("Appacitive", "Appacitive Object is uninitialized. " +
					"Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}

	}

	/**
	 * Deletes multiple Connection objects which are passed in the list as an
	 * argument.
	 * 
	 * @param connectionsIds
	 *            List containing connection's id's of existing connections on
	 *            the remote server.
	 * @param relationType
	 *            The type of Connection.
	 */
	public static void deleteConnections(ArrayList<String> connectionsIds, String relationType) {
		deleteConnections(connectionsIds, relationType, null);
	}

	/**
	 * Deletes multiple Connection objects which are passed in the list as an
	 * argument.
	 * 
	 * @param connectionsIds
	 *            List containing connection's id's of existing connections on
	 *            the remote server.
	 * @param relationType
	 *            The type of Connection.
	 * @param callback
	 *            Callback invoked when the delete operation is successful or
	 *            failed.
	 */
	public static void deleteConnections(final ArrayList<String> connectionsIds, final String relationType,
			final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveError> internalCallback = new AppacitiveInternalCallback<AppacitiveError>() {

			@Override
			public void done(AppacitiveError error) {
				if (error == null) {
					callback.onSuccess();
				} else {
					callback.onFailure(error);
				}
			}

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}
		};

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveError> deleteTask = new BackgroundTask<AppacitiveError>(internalCallback) {
				AppacitiveError error;

				@Override
				public AppacitiveError run() {
					URL url;
					Gson gson = new Gson();
					HashMap<String, Object> requestMap = new HashMap<String, Object>();
					requestMap.put("idlist", connectionsIds);
					String requestJsonString = gson.toJson(requestMap);
					try {
						url = new URL(Constants.CONNECTION_URL + relationType + "/bulkdelete");
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						AppacitiveHelper.addHeaders(connection);
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString((requestJsonString.length())));

						OutputStream os = connection.getOutputStream();
						os.write(requestJsonString.getBytes());
						os.close();

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							Reader reader = new InputStreamReader(inputStream);
							AppacitiveError response = gson.fromJson(reader,AppacitiveError.class);
							inputStream.close();
							if (!response.mStatusCode.equals("200")) {
								error = response;
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						error = new AppacitiveError();
						error.setMessage(e.getMessage());
						this.setNetworkError(error);
					}
					return error;
				}
			};
			deleteTask.execute();
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
	 * Fetch all the properties of the Connection object on which this method is
	 * invoked.
	 */
	public void fetchConnection() {
		fetchConnection(null);
	}

	/**
	 * Fetch all the properties of the Connection object on which this method is
	 * invoked.
	 * 
	 * @param callback
	 *            Callback invoked when the fetch operation is successful or
	 *            failed.
	 */
	public void fetchConnection(final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveError> internalCallback = new AppacitiveInternalCallback<AppacitiveError>() {

			@Override
			public void done(AppacitiveError error) {
				if (error == null) {
					callback.onSuccess();
				} else {
					callback.onFailure(error);
				}
			}

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}

		};

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveError> fetchTask = new BackgroundTask<AppacitiveError>(internalCallback) {
				AppacitiveError error;

				@Override
				public AppacitiveError run() {
					URL url;
					try {
						String urlString = Constants.CONNECTION_URL
								+ AppacitiveConnection.this.mRelationType + "/" + AppacitiveConnection.this.mConnectionId;
						url = new URL(urlString);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);
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
							builder.registerTypeAdapter(AppacitiveConnection.class, new AppacitiveConnectionTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							AppacitiveConnectionJsonModel response = gson.fromJson(reader,AppacitiveConnectionJsonModel.class);
							inputStream.close();

							if (!response.mStatus.getStatusCode().equals("200")) {
								error = response.mStatus;
							} else {
								AppacitiveConnection.this.updateConnectionFields(response.mAppacitiveConnection);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						error = new AppacitiveError();
						error.setMessage(e.getMessage());
						this.setNetworkError(error);
					}
					return error;
				}
			};
			fetchTask.execute();
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
	 * Fetches multiple Connections from the remote server.
	 * 
	 * @param connectionIds
	 *            An array of objectIds. Connections with these object id's are
	 *            fetched.
	 * @param relationType
	 *            The name of the relation. Connections of this relation are
	 *            retrieved.
	 */
	public static void fetchConnections(final ArrayList<String> connectionIds,
			final String relationType) {
		AppacitiveConnection.fetchConnections(connectionIds, relationType, null);
	}

	/**
	 * Fetches multiple Connections from the remote server.
	 * 
	 * @param connectionIds
	 *            An array of connection Id's. Connections with these object
	 *            id's are fetched.
	 * @param relationType
	 *            The name of the relation. Connections of this relation are
	 *            retrieved.
	 * @param callback
	 *            Callback invoked when the fetch operation is successful or
	 *            failed.
	 */
	public static void fetchConnections(final ArrayList<String> connectionIds, final String relationType,
			final AppacitiveFetchCallback<AppacitiveConnection> callback) {

		if (connectionIds == null) {
			AppacitiveError error = new AppacitiveError();
			error.setMessage("ConnectionId's list is empty. Pass an list of connection Id's to fetch.");
			Log.w("TAG", error.getMessage());
			if (callback != null) {
				callback.onFailure(error);
			}
			return;
		}

		AppacitiveInternalCallback<AppacitiveConnectionJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveConnection.AppacitiveConnectionJsonModel>() {

			@Override
			public void done(AppacitiveConnectionJsonModel result) {
				if (result.mStatus.getStatusCode().equals("200")) {
					callback.onSuccess(result.mAppacitiveConnections, result.mPagingInfo);
				} else {
					callback.onFailure(result.mStatus);
				}
			}

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}

		};

		final Appacitive appacitive = Appacitive.getInstance();

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveConnectionJsonModel> fetchTask = new BackgroundTask<AppacitiveConnectionJsonModel>(
					internalCallback) {

				AppacitiveConnectionJsonModel response;
				AppacitiveError error;

				@Override
				public AppacitiveConnectionJsonModel run() {
					URL url;
					StringBuffer queryParams = null;
					for (String id : connectionIds) {
						if (queryParams == null) {
							queryParams = new StringBuffer();
							queryParams.append(id);
						} else {
							queryParams.append("," + id);
						}
					}
					try {
						url = new URL(Constants.CONNECTION_URL + relationType + "/multiget" + "/" + queryParams);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveConnection.class, new AppacitiveConnectionTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							response = gson.fromJson(reader, AppacitiveConnectionJsonModel.class);
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
			fetchTask.execute();
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
	 * Search for all Connections of a particular relation type.
	 * 
	 * @param relationType
	 *            The relation type that the connections should belong to.
	 */
	public static void searchAllConnections(String relationType) {
		searchConnections(relationType, null, null);
	}

	/**
	 * Search for all Connections of a particular relation type.
	 * 
	 * @param relationType
	 *            The relation type that the connections should belong to.
	 * @param callback
	 *            Callback invoked when the search operation is successful or
	 *            failed.
	 */
	public static void searchAllConnections(String relationType, AppacitiveFetchCallback<AppacitiveConnection> callback) {
		searchConnections(relationType, null, callback);
	}

	/**
	 * Searches for Connections and filters the result according to the query
	 * provided.
	 * 
	 * @param relationType
	 *            The relation type that the connections should belong to.
	 * @param query
	 *            SQL kind of query to search for specific objects. For more
	 *            info http://appacitive.com
	 * @param callback
	 *            Callback invoked when the search operation is successful or
	 *            failed.
	 */
	public static void searchConnections(final String relationType, final String query,
			final AppacitiveFetchCallback<AppacitiveConnection> callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveConnectionJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveConnectionJsonModel>() {

			@Override
			public void done(AppacitiveConnectionJsonModel result) {
				if (result.mStatus.getStatusCode().equals("200")) {
					callback.onSuccess(result.mAppacitiveConnections, result.mPagingInfo);
				} else {
					callback.onFailure(result.mStatus);
				}
			}

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}
		};

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveConnectionJsonModel> searchTask = new BackgroundTask<AppacitiveConnectionJsonModel>(
					internalCallback) {

				@Override
				public AppacitiveConnectionJsonModel run() {
					AppacitiveError error;
					AppacitiveConnectionJsonModel response = null;
					URL url = null;
					
					String urlString = Constants.CONNECTION_URL + relationType + "/find/all";
					if (query != null) {
						urlString = urlString + "?query=" + query;
					}
					try {
						url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveConnection.class, new AppacitiveConnectionTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							response = gson.fromJson(reader,AppacitiveConnectionJsonModel.class);
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
			searchTask.execute();
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
	 * @see searchForConnectedArticles(String relationType, long articleId,
	 *      AppacitiveFetchCallback callback)
	 * @param relationType
	 * @param articleId
	 */
	public static void searchForConnectedArticles(final String relationType,
			final long articleId) {
		searchForConnectedArticles(relationType, articleId, null);
	}

	/**
	 * Searches for all the connections from the specified articleId, and
	 * returns the list of all the connections of the specified relationType
	 * from that article along with the oher endpoints article.
	 * 
	 * @param relationType
	 *            The name of the relation. Connections of this relation are
	 *            retrieved.
	 * @param articleId
	 *            The aritcleId of any one endpoint.
	 * @param callback
	 *            Callback invoked when the search operation is successful or
	 *            failed.
	 */
	public static void searchForConnectedArticles(final String relationType, final long articleId, 
			final AppacitiveFetchCallback<AppacitiveObject> callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveConnectionJsonModelForConnectedArticles> internalCallback = new AppacitiveInternalCallback<AppacitiveConnectionJsonModelForConnectedArticles>() {
			
			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}
			
			@Override
			public void done(AppacitiveConnectionJsonModelForConnectedArticles result) {
				if (result.mStatus.getStatusCode().equals("200")) {
					callback.onSuccess(result.mAppacitiveObjects, result.mPagingInfo);
				} else {
					callback.onFailure(result.mStatus);
				}
			}
		};
		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveConnectionJsonModelForConnectedArticles> searchTask = new BackgroundTask<AppacitiveConnectionJsonModelForConnectedArticles>(internalCallback) {
				@Override
				public AppacitiveConnectionJsonModelForConnectedArticles run() {
					URL url = null;
					AppacitiveError error;
					AppacitiveConnectionJsonModelForConnectedArticles response = null;
					String urlString = Constants.CONNECTION_URL + relationType + "/" + articleId + "/find";
					try {
						url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveObject.class, new AppacitiveConnectedArticleTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
					        response = gson.fromJson(reader, AppacitiveConnectionJsonModelForConnectedArticles.class);
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
			searchTask.execute();
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


	private String createRequestParams() {
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
		Gson gson = new Gson();
		String requestJsonString = gson.toJson(requestParams);
		return requestJsonString;
	}

	/**
	 * @return The user name who created this Connection.
	 */

	public String getCreatedBy() {
		return mCreatedBy;
	}

	/**
	 * Sets createdBy
	 * 
	 * @param createdBy
	 */

	public void setCreatedBy(String createdBy) {
		this.mCreatedBy = createdBy;
	}

	/**
	 * @return Returns articleA id.
	 */
	public long getArticleAId() {
		return mArticleAId;
	}

	/**
	 * Sets the article A id from where to create a connection from.
	 * 
	 * @param articleAId
	 *            .
	 */
	public void setArticleAId(long articleAId) {
		this.mArticleAId = articleAId;
	}

	/**
	 * @return Returns articleB id.
	 */
	public long getArticleBId() {
		return mArticleBId;
	}

	/**
	 * Sets the article B id to which the Connection is to be created.
	 * 
	 * @param articleBId
	 *            .
	 */
	public void setArticleBId(long articleBId) {
		this.mArticleBId = articleBId;
	}

	/**
	 * @return Returns the connection Id.
	 */
	public long getConnectionId() {
		return mConnectionId;
	}

	/**
	 * Sets the connection ID.
	 * 
	 * @param objectId
	 *            .
	 */
	public void setConnectionId(long objectId) {
		this.mConnectionId = objectId;
	}

	/**
	 * Returns schema of article A.
	 * 
	 * @return Returns schema of article A.
	 */
	public String getLabelA() {
		return mLabelA;
	}

	/**
	 * Sets the schema of article A.
	 * 
	 * @param labelA
	 *            schema name.
	 */
	public void setLabelA(String labelA) {
		this.mLabelA = labelA;
	}

	/**
	 * Returns schema of article B.
	 * 
	 * @return Returns schema of article B.
	 */
	public String getLabelB() {
		return mLabelB;
	}

	/**
	 * Sets the schema of article B.
	 * 
	 * @param labelB
	 *            schema name.
	 */
	public void setLabelB(String labelB) {
		this.mLabelB = labelB;
	}

	/**
	 * Return the relationdID.
	 * 
	 * @return relation ID.
	 */
	public long getRelationId() {
		return mRelationId;
	}

	/**
	 * Sets the relation ID
	 * 
	 * @param relationId
	 */
	public void setRelationId(long relationId) {
		this.mRelationId = relationId;
	}

	/**
	 * Returns the relation type.
	 * 
	 * @return The relation type.
	 */
	public String getRelationType() {
		return mRelationType;
	}

	/**
	 * Sets the relation type
	 * 
	 * @param relation
	 *            type
	 */
	public void setRelationType(String relationType) {
		this.mRelationType = relationType;
	}

	/**
	 * Returns the user name who has modified the connection last.
	 * 
	 * @return Last modified date.
	 */
	public String getLastModifiedBy() {
		return mLastModifiedBy;
	}

	/**
	 * Sets the user name who has modified the connection last
	 * 
	 * @param Last
	 *            modified date
	 */
	public void setLastModifiedBy(String lastModifiedBy) {
		this.mLastModifiedBy = lastModifiedBy;
	}

	/**
	 * Returns the date on which the article is created.
	 * 
	 * @return Date of creation of the connection.
	 */
	public Date getUtcDateCreated() {
		return mUtcDateCreated;
	}

	/**
	 * Sets the date on which article is created
	 * 
	 * @param Date
	 *            of creation of the connection.
	 */
	public void setUtcDateCreated(Date UtcDateCreated) {
		this.mUtcDateCreated = UtcDateCreated;
	}

	/**
	 * Returns the date on which the article is modified last.
	 * 
	 * @return Last Date of modification of the connection.
	 */
	public Date getUtcLastModifiedDate() {
		return mUtcLastModifiedDate;
	}

	/**
	 * Sets the date on which the article is modified last.
	 * 
	 * @param Last
	 *            Date of modification of the connection.
	 */
	public void setUtcLastModifiedDate(Date UtcLastModifiedDate) {
		this.mUtcLastModifiedDate = UtcLastModifiedDate;
	}

	/**
	 * Returns the revision number.
	 * 
	 * @return Returns the revision number.
	 */
	public long getRevision() {
		return mRevision;
	}

	/**
	 * Sets the revision number.
	 * 
	 * @param Returns
	 *            the revision number.
	 */
	public void setRevision(long revision) {
		this.mRevision = revision;
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

	private class AppacitiveConnectionJsonModel {

		@SerializedName("connections")
		public ArrayList<AppacitiveConnection> mAppacitiveConnections;

		@SerializedName("connection")
		public AppacitiveConnection mAppacitiveConnection;

		@SerializedName("status")
		public AppacitiveError mStatus;

		@SerializedName("paginginfo")
		public AppacitivePagingInfo mPagingInfo;
	}

	private class AppacitiveConnectionJsonModelForConnectedArticles {
		@SerializedName("connections")
		public ArrayList<AppacitiveObject> mAppacitiveObjects;
		
		@SerializedName("status")
		public AppacitiveError mStatus;
		
		@SerializedName("paginginfo")
		public AppacitivePagingInfo mPagingInfo;
	}


	private static class AppacitiveConnectionTypeAdapter extends
			TypeAdapter<AppacitiveConnection> {

		@Override
		public AppacitiveConnection read(JsonReader jsonReader)
				throws IOException {
			AppacitiveConnection appacitiveConnection = new AppacitiveConnection();
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
					appacitiveConnection.setConnectionId(jsonReader.nextLong());
				} else if (name.equals("__relationtype") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveConnection.setRelationType(jsonReader.nextString());
				} else if (name.equals("__relationid") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveConnection.setRelationId(jsonReader.nextLong());
				} else if (name.equals("__revision")
						&& jsonReader.peek() != JsonToken.NULL) {
					appacitiveConnection.setRevision(jsonReader.nextLong());
				} else if (name.equals("__endpointa")
						&& jsonReader.peek() != JsonToken.NULL) {
					getEndpoint(jsonReader, appacitiveConnection, name);
				} else if (name.equals("__endpointb")
						&& jsonReader.peek() != JsonToken.NULL) {
					getEndpoint(jsonReader, appacitiveConnection, name);
				} else if (name.equals("__createdby")
						&& jsonReader.peek() != JsonToken.NULL) {
					appacitiveConnection.setCreatedBy(jsonReader.nextString());
				} else if (name.equals("__lastmodifiedby") && jsonReader.peek() != JsonToken.NULL) {
					appacitiveConnection.setLastModifiedBy(jsonReader.nextString());
				} else if (name.equals("__tags") && jsonReader.peek() != JsonToken.NULL) {
					getTags(jsonReader, appacitiveConnection);
				} else if (name.equals("__utcdatecreated")
						&& jsonReader.peek() != JsonToken.NULL) {
					appacitiveConnection.setUtcDateCreated(fromJsonResponse(jsonReader.nextString()));
				} else if (name.equals("__utclastupdateddate")
						&& jsonReader.peek() != JsonToken.NULL) {
					appacitiveConnection.setUtcLastModifiedDate(fromJsonResponse(jsonReader.nextString()));
				} else if (name.equals("__attributes")
						&& jsonReader.peek() != JsonToken.NULL) {
					getAttributes(jsonReader, appacitiveConnection);
				} else if (!name.equals("")) {
					appacitiveConnection.addProperty(name, jsonReader.nextString());
				} else {
					jsonReader.skipValue();
				}
			}
			jsonReader.endObject();
			return appacitiveConnection;
		}

		private void getEndpoint(JsonReader jsonReader,
				AppacitiveConnection appacitiveConnection, String name)
				throws IOException {
			jsonReader.beginObject();
			while (jsonReader.hasNext()) {
				if (jsonReader.peek() == JsonToken.NULL) {
					jsonReader.skipValue();
				}
				if (name.equals("__endpointa")) {
					String nameForEndpoint = jsonReader.nextName();
					if (nameForEndpoint.equals("label") && jsonReader.peek() != JsonToken.NULL) {
						appacitiveConnection.setLabelA(jsonReader.nextString());
					} else if (nameForEndpoint.equals("articleid") && jsonReader.peek() != JsonToken.NULL) {
						appacitiveConnection.setArticleAId(Long.parseLong(jsonReader.nextString()));
					} else if (nameForEndpoint.equals("article") && jsonReader.peek() != JsonToken.NULL) {
						jsonReader.skipValue();
					} else {
						jsonReader.skipValue();
					}
				} else if (name.equals("__endpointb")) {
					String nameForEndpoint = jsonReader.nextName();
					if (nameForEndpoint.equals("label")
							&& jsonReader.peek() != JsonToken.NULL) {
						appacitiveConnection.setLabelB(jsonReader.nextString());
					} else if (nameForEndpoint.equals("articleid") && jsonReader.peek() != JsonToken.NULL) {
						appacitiveConnection.setArticleBId(Long.parseLong(jsonReader.nextString()));
					}
				}
			}
			jsonReader.endObject();
		}

		private void getTags(JsonReader jsonReader,
				AppacitiveConnection appacitiveConnection) throws IOException {
			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				appacitiveConnection.addTag(jsonReader.nextString());
			}
			jsonReader.endArray();
		}

		private void getAttributes(JsonReader reader,
				AppacitiveConnection appacitiveConnection) throws IOException {
			reader.beginObject();
			while (reader.hasNext()) {
				if (reader.peek() == JsonToken.NULL) {
					reader.skipValue();
				}
				String key = reader.nextName();
				String value = reader.nextString();
				appacitiveConnection.addAttribute(key, value);
			}
			reader.endObject();
		}

		private Date fromJsonResponse(String dateString) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
			Date date = null;
			try {
				date = formatter.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}

		@Override
		public void write(JsonWriter arg0, AppacitiveConnection arg1) throws IOException {
		}
	}

	private static class AppacitiveConnectedArticleTypeAdapter extends TypeAdapter<AppacitiveObject> {

		@Override
		public AppacitiveObject read(JsonReader jsonReader) throws IOException {
			AppacitiveObject appacitiveObject = null;
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
				if (name.equals("__endpointa") || name.equals("__endpointb") && jsonReader.peek() != JsonToken.NULL) {
					jsonReader.beginObject();
					while (jsonReader.hasNext()) {
						if (jsonReader.peek() == JsonToken.NULL) {
							jsonReader.skipValue();
						}
						String nameForEndPoint = jsonReader.nextName();
						if (nameForEndPoint.equals("article")) {
							appacitiveObject = AppacitiveHelper.readArticleFromStream(jsonReader);
						} else {
							jsonReader.skipValue();
						}
					}
					jsonReader.endObject();
				} else {
					jsonReader.skipValue();
				}
			}
			jsonReader.endObject();
			return appacitiveObject;
		}

		@Override
		public void write(JsonWriter writter, AppacitiveObject object) throws IOException {
		}
	}
}