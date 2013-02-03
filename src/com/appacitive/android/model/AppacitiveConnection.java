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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.callbacks.AppacitiveFetchCallback;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A Connection is represents an edge in a graph and is used to connect two
 * AppacitiveObjects. A Connection itself can store data in its properties and
 * attributes fields.
 * 
 * @author Sandeep Dhull
 */

// TODO : 1: Fix the null values in Error object
// TODO : 2: Implement a strong checking, so that only valid network request
// could be made.
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

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<Void> createTask = new BackgroundTask<Void>() {
				Map<String, Object> responseMap = null;
				AppacitiveError appacitiveError;

				@Override
				public Void run() {
					URL url;
					String requestParams = null;
					requestParams = AppacitiveConnection.this
							.createRequestParams();
					try {
						url = new URL(Constants.CONNECTION_URL
								+ AppacitiveConnection.this.mRelationType);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString(((requestParams.toString())
										.length())));
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setDoOutput(true);

						OutputStream os = connection.getOutputStream();
						os.write((requestParams.toString()).getBytes());
						os.close();

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.e("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							appacitiveError = new AppacitiveError();
							appacitiveError.setStatusCode(connection.getResponseCode() + "");
							appacitiveError.setMessage(connection.getResponseMessage());
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
							appacitiveError = AppacitiveHelper.checkForErrorInStatus(responseMap);
							if (appacitiveError == null) {
								readAppacitiveConnection(responseMap);
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
			};
			createTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode("8002");
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
	public void createConnection(AppacitiveObject objectA,
			AppacitiveObject objectB) {
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
	public void createConnection(AppacitiveObject objectA,
			AppacitiveObject objectB, AppacitiveCallback callback) {
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

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>() {
				AppacitiveError error;
				Map<String, Object> responseMap = null;

				@Override
				public Void run() {
					URL url;
					try {
						String urlString = Constants.CONNECTION_URL
								+ AppacitiveConnection.this.mRelationType + "/"
								+ AppacitiveConnection.this.mConnectionId;
						url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.DELETE.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "+ connection.getResponseMessage());
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
							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
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
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode("8002");
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
	public static void deleteConnections(ArrayList<String> connectionsIds,
			String relationType) {
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
	public static void deleteConnections(
			final ArrayList<String> connectionsIds, final String relationType,
			final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>() {
				AppacitiveError error;

				@Override
				public Void run() {
					URL url;
					Gson gson = new Gson();
					HashMap<String, Object> requestMap = new HashMap<String, Object>();
					requestMap.put("idlist", connectionsIds);
					String requestJsonString = gson.toJson(requestMap);
					try {
						url = new URL(Constants.CONNECTION_URL + relationType + "/bulkdelete");
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",
								Integer.toString((requestJsonString.length())));

						OutputStream os = connection.getOutputStream();
						os.write(requestJsonString.getBytes());
						os.close();

						InputStream inputStream;
						Map<String, Object> responseMap = null;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode()+ "");
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
							Type typeOfClass = new TypeToken<Map<String, Object>>() {}.getType();
							responseMap = gson.fromJson(buffer.toString(),typeOfClass);
							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
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
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode("8002");
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

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<Void> fetchTask = new BackgroundTask<Void>() {
				AppacitiveError error;

				@Override
				public Void run() {
					URL url;
					try {
						String urlString = Constants.CONNECTION_URL
								+ AppacitiveConnection.this.mRelationType + "/"
								+ AppacitiveConnection.this.mConnectionId;
						url = new URL(urlString);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
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
							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
							if (error == null) {
								readAppacitiveConnection(responseMap);
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
			fetchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode("8002");
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
		AppacitiveConnection
				.fetchConnections(connectionIds, relationType, null);
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
	public static void fetchConnections(final ArrayList<String> connectionIds,
			final String relationType, final AppacitiveFetchCallback callback) {

		if (connectionIds == null) {
			AppacitiveError error = new AppacitiveError();
			error.setMessage("ConnectionId's list is empty. Pass an list of connection Id's to fetch.");
			Log.w("TAG", error.getMessage());
			if (callback != null) {
				callback.onFailure(error);
			}
			return;
		}

		final Appacitive appacitive = Appacitive.getInstance();

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<Void> fetchTask = new BackgroundTask<Void>() {
				AppacitiveError error;
				Map<String, Object> responseMap = null;

				@Override
				public Void run() {
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
						url = new URL(Constants.CONNECTION_URL + relationType
								+ "/multiget" + "/" + queryParams);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed " + connection.getResponseMessage());
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
							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSuccess(responseMap);
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
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode("8002");
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
	public static void searchAllConnections(String relationType,
			AppacitiveFetchCallback callback) {
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
	public static void searchConnections(final String relationType,
			final String query, final AppacitiveFetchCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<Void> searchTask = new BackgroundTask<Void>() {
				AppacitiveError error;
				Map<String, Object> responseMap = null;

				@Override
				public Void run() {
					URL url = null;
					String urlString = Constants.CONNECTION_URL + relationType
							+ "/find/all";
					if (query != null) {
						urlString = urlString + "?" + query;
					}
					try {
						url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed " + connection.getResponseMessage());
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
							Type typeOfClass = new TypeToken<Map<String, Object>>() {
							}.getType();
							responseMap = gson.fromJson(buffer.toString(),
									typeOfClass);
							error = AppacitiveHelper
									.checkForErrorInStatus(responseMap);
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSuccess(responseMap);
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
			searchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode("8002");
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
	public static void searchForConnectedArticles(final String relationType,
			final long articleId, final AppacitiveFetchCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<Void> searchTask = new BackgroundTask<Void>() {
				AppacitiveError error;
				Map<String, Object> responseMap = null;

				@Override
				public Void run() {
					URL url = null;
					String urlString = Constants.CONNECTION_URL + relationType
							+ "/" + articleId + "/find";
					try {
						url = new URL(urlString);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed " + connection.getResponseMessage());
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
							responseMap = gson.fromJson(buffer.toString(), typeOfClass);
							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
							inputStream.close();
						}
						if (callback != null) {
							if (error == null) {
								callback.onSuccess(responseMap);
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
			searchTask.execute();
		} else {
			Log.w("Appacitive",
					"Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode("8002");
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readAppacitiveConnection(Map<String, Object> responseMap) {
		Map<String, Object> connectionMap = (Map<String, Object>) responseMap
				.get("connection");
		this.mConnectionId = new Long((String) connectionMap.get("__id"));
		this.mRelationType = (String) connectionMap.get("__relationtype");
		this.mCreatedBy = (String) connectionMap.get("__createdby");
		this.mLastModifiedBy = (String) connectionMap.get("__lastmodifiedby");
		this.mRevision = new Long((String) connectionMap.get("__revision"));
		this.mAttributes = (Map<String, Object>) connectionMap
				.get("__attributes");
		this.mTags = (List<String>) connectionMap.get("__tags");
		this.mProperties = AppacitiveHelper.getProperties(connectionMap);
		try {
			this.mUtcDateCreated = fromJsonResponse((String) connectionMap
					.get("__utcdatecreated"));
			this.mUtcLastModifiedDate = fromJsonResponse((String) connectionMap
					.get("__utclastupdateddate"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Map<String, Object> endpointA = (Map<String, Object>) connectionMap
				.get("__endpointa");
		Map<String, Object> endpointB = (Map<String, Object>) connectionMap
				.get("__endpointb");
		this.mLabelA = (String) endpointA.get("label");
		this.mLabelB = (String) endpointB.get("label");
		this.mArticleAId = new Long((String) endpointA.get("articleid"));
		this.mArticleBId = new Long((String) endpointB.get("articleid"));
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

	private Date fromJsonResponse(String dateString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
		Date date = formatter.parse(dateString);
		return date;
	}

	/**
	 * @return The user name who created this Connection.
	 */
	public String getCreatedBy() {
		return mCreatedBy;
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
	 * Returns the relation type.
	 * 
	 * @return The relation type.
	 */
	public String getRelationType() {
		return mRelationType;
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
	 * Returns the date on which the article is created.
	 * 
	 * @return Date of creation of the connection.
	 */
	public Date getUtcDateCreated() {
		return mUtcDateCreated;
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
	 * Returns the revision number.
	 * 
	 * @return Returns the revision number.
	 */
	public long getRevision() {
		return mRevision;
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