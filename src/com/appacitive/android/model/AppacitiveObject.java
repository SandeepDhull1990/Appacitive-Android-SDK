package com.appacitive.android.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
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
 * An AppacitiveObject is a basic unit to store information in. It represents an
 * instance of a schema. Data can be stored in key-value pairs in the properties
 * and attributes fields.
 * 
 */
@SuppressLint("SimpleDateFormat")
public class AppacitiveObject {
	protected String mCreatedBy;
	protected String mSchemaType;
	protected String mLastModifiedBy;
	protected long mObjectId;
	protected long mRevision = -999999;
	protected long mSchemaId;
	protected Map<String, Object> mProperties;
	protected Map<String, Object> mAttributes;
	protected List<String> mTags;
	protected Date mUTCDateCreated;
	protected Date mUTCLastUpdatedDate;

	/**
	 * Initialize an AppacitiveObject with the provided schema name.
	 * 
	 * @param schemaType
	 *            The schema this article represents.
	 */
	public AppacitiveObject(String schemaType) {
		this.mSchemaType = schemaType;
	}

	AppacitiveObject() {
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
	 *            key of the data item to be stored.
	 * @return value Corresponding value to the key.
	 */
	public Object getProperty(String key) {
		if (this.mProperties != null) {
			return this.mProperties.get(key);
		}
		return null;
	}

	/**
	 * Adds a attruibute to an AppacitiveObject.
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

	/**
	 * Method used to get an attribute from the AppacitiveObject.
	 * 
	 * @param key
	 *            key of the data item to be stored.
	 * @return value Corresponding value to the key.
	 */
	public Object getAttribute(String key) {
		if (this.mProperties != null) {
			return this.mAttributes.get(key);
		}
		return null;
	}

	/**
	 * Method used to add a tag to the AppacitiveObject. On the basis tags, the
	 * article could be searched.
	 * 
	 * @param tag
	 *            The tag to be added to the AppacitiveObject.
	 */
	public void addTag(String tag) {
		if (this.mTags == null) {
			this.mTags = new ArrayList<String>();
		}
		this.mTags.add(tag);
	}

	private String createPostParameters() {
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
		Gson gson = new Gson();
		String jsonRequestString = gson.toJson(requestParams);
		return jsonRequestString;
	}

	/*
	 * Updates the appacitive object properties by setting its new values
	 */
	private void updateAppacitiveObjectFields(AppacitiveObject object) {
		this.mObjectId = object.mObjectId;
		this.mSchemaType = object.mSchemaType;
		this.mSchemaId = object.mSchemaId;
		this.mCreatedBy = object.mCreatedBy;
		this.mUTCDateCreated = object.mUTCDateCreated;
		this.mUTCLastUpdatedDate = object.mUTCLastUpdatedDate;
		this.mLastModifiedBy = object.mLastModifiedBy;
		this.mRevision = object.mRevision;
		this.mProperties = object.mProperties;
		this.mAttributes = object.mAttributes;
		this.mTags = object.mTags;
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
	 * Saves the article on the remote server.
	 * 
	 * This method will save an article in the background. If save is successful
	 * the properties will be updated and the success callback will be invoked.
	 * If not the failure callback is invoked.
	 * 
	 * @param callback
	 *            Callback invoked when the save operation is successful or
	 *            failed.
	 */
	public void saveObject(final AppacitiveCallback callback) {

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
				if(callback != null) {
					callback.onFailure(error);
				}
			}
			
		};

		if (appacitive != null && appacitive.getSessionId() != null) {

			BackgroundTask<AppacitiveError> saveTask = new BackgroundTask<AppacitiveError>(internalCallback) {
				AppacitiveError error;

				@Override
				public AppacitiveError run() {
					URL url;
					String requestParams = AppacitiveObject.this.createPostParameters();
					try {
						url = new URL(Constants.ARTICLE_URL + AppacitiveObject.this.mSchemaType);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length", Integer.toString((requestParams.length())));
						AppacitiveHelper.addHeaders(connection);
						connection.setDoOutput(true);

						OutputStream os = connection.getOutputStream();
						os.write(requestParams.getBytes());
						os.close();

						InputStream inputStream;

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error : " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveObject.class, new AppacitiveObjectTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							AppacitiveObjectJsonModel response = gson.fromJson(reader, AppacitiveObjectJsonModel.class);
							inputStream.close();
							if (!response.mStatus.getStatusCode().equals("200")) {
								error = response.mStatus;
							} else {
								AppacitiveObject.this.updateAppacitiveObjectFields(response.mAppacitiveObject);
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
			saveTask.execute();
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
	 * Deletes the article on the remote server.
	 * 
	 * This method will delete an article in the background. If deletion is
	 * successful or unsuccessful a callback will be invoked.
	 */
	public void deleteObject(AppacitiveCallback callback) {
		deleteObjectWithConnections(false, callback);
	}

	/**
	 * Delete the article on the remote server.
	 * 
	 * This method will delete an article in the background. If deletion is
	 * successful the success callback will be invoked. If not the failure
	 * callback is invoked.
	 * 
	 * @param deleteConnection
	 *            If this is true then all the APConnection associated with this
	 *            AppacitiveObject also get detleted.
	 * @param callback
	 *            Callback invoked when the delete operation is successful or
	 *            failed.
	 */
	public void deleteObjectWithConnections(final boolean deleteConnections,
			final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveError> internalCallback = new AppacitiveInternalCallback<AppacitiveError>() {

			@Override
			public void done(AppacitiveError error) {
				if (callback != null) {
					if (error.mStatusCode.equals("200")) {
						callback.onSuccess();
					} else {
						callback.onFailure(error);
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

		if (appacitive != null && appacitive.getSessionId() != null) {

			BackgroundTask<AppacitiveError> deleteTask = new BackgroundTask<AppacitiveError>(internalCallback) {
				AppacitiveError error;

				@Override
				public AppacitiveError run() {

					URL url;
					String urlString = Constants.ARTICLE_URL + AppacitiveObject.this.mSchemaType + "/"
							+ AppacitiveObject.this.mObjectId;

					if (deleteConnections) {
						urlString = urlString + "?deleteconnections=true";
					}
					try {
						url = new URL(urlString);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.DELETE.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(inputStream);
							Gson gson = new Gson();
							error = gson.fromJson(reader, AppacitiveError.class);
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
	 * Deletes multiple AppacitiveObjects from the remote server.
	 * 
	 * @param objectIds
	 *            The ids of the objects to delete.
	 * @param schemaName
	 *            The schema that the objects belong to.
	 * @param callback
	 *            Callback invoked when the delete operation is successful or
	 *            failed.
	 */
	public static void deleteObjectsWithIds(final ArrayList<String> objectIds,
			final String schemaName, final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveError> internalCallback = new AppacitiveInternalCallback<AppacitiveError>() {

			@Override
			public void done(AppacitiveError error) {
				if (callback != null) {
					if (error == null) {
						callback.onSuccess();
					} else {
						callback.onFailure(error);
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

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveError> deleteTask = new BackgroundTask<AppacitiveError>(internalCallback) {

				AppacitiveError error;

				@Override
				public AppacitiveError run() {
					URL url;
					Gson gson = new Gson();
					HashMap<String, List<String>> requestMap = new HashMap<String, List<String>>();

					requestMap.put("idlist", objectIds);
					String requestParams = gson.toJson(requestMap);

					try {
						url = new URL(Constants.ARTICLE_URL + schemaName + "/bulkdelete");
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						AppacitiveHelper.addHeaders(connection);
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString((requestParams.length())));

						OutputStream os = connection.getOutputStream();
						os.write(requestParams.getBytes());
						os.close();

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							Reader reader = new InputStreamReader(inputStream,Charset.defaultCharset());
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
	 * Fetches all the properties of an AppacitiveConnection object on which
	 * this method is invoked.
	 * 
	 * @param callback
	 *            Callback invoked when the fetch operation is successful or
	 *            failed.
	 */
	public void fetchObject(final AppacitiveCallback callback) {

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
				if(callback != null) {
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
						url = new URL(Constants.ARTICLE_URL + AppacitiveObject.this.mSchemaType + "/"
								+ AppacitiveObject.this.mObjectId);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveObject.class, new AppacitiveObjectTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							AppacitiveObjectJsonModel response = gson.fromJson(reader, AppacitiveObjectJsonModel.class);
							inputStream.close();

							if (!response.mStatus.getStatusCode().equals("200")) {
								error = response.mStatus;
							} else {
								AppacitiveObject.this.updateAppacitiveObjectFields(response.mAppacitiveObject);
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
	 * Retrieves multiple AppacitiveObject of a particular schema.
	 * 
	 * @param ids
	 *            The ids of the objects.
	 * @param schemaName
	 *            The schema name the objects belong to.
	 * @param callback
	 *            Callback invoked when the fetch operation is successful or
	 *            failed.
	 */
	public static void fetchObjectsWithIds(final ArrayList<String> ids,
			final String schemaName, final AppacitiveFetchCallback<AppacitiveObject> callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveObjectJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveObjectJsonModel>() {

			@Override
			public void done(AppacitiveObjectJsonModel result) {
				if (result.mStatus.getStatusCode().equals("200")) {
					 callback.onSuccess(result.mAppacitiveObjects, result.mPagingInfo);
				} else {
					callback.onFailure(result.mStatus);
				}
			}
			
			@Override
			public void onFailed(AppacitiveError error) {
				if(callback != null) {
					callback.onFailure(error);
				}
			}
			
		};

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveObjectJsonModel> fetchTask = new BackgroundTask<AppacitiveObjectJsonModel>(internalCallback) {

				AppacitiveError error;

				@Override
				public AppacitiveObjectJsonModel run() {
					URL url;
					AppacitiveObjectJsonModel response = null;
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
						url = new URL(Constants.ARTICLE_URL + schemaName + "/multiget" + "/" + queryParams);
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
							builder.registerTypeAdapter(AppacitiveObject.class, new AppacitiveObjectTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							response = gson.fromJson(reader, AppacitiveObjectJsonModel.class);
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
	 * Searches for all AppacitiveObjects of a particular schema.
	 * 
	 * @param schemaName
	 *            The schema name the objects belong to.
	 * @param callback
	 *            Callback invoked when the search operation is successful or
	 *            failed.
	 */
	public static void searchAllObjects(String schemaName, final AppacitiveFetchCallback<AppacitiveObject> callback) {
		searchObjects(schemaName, null, callback);
	}

	/**
	 * Searches for AppacitiveObjects and filters the results according to the
	 * query string.
	 * 
	 * @param schemaName
	 *            The schema name the objects belong to.
	 * @param query
	 *            SQL kind of query to search for specific objects. For more
	 *            info http://appacitive.com
	 * @param callback
	 *            Callback invoked when the search operation is successful or
	 *            failed.
	 */
	public static void searchObjects(final String schemaName, final String query, final AppacitiveFetchCallback<AppacitiveObject> callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveObjectJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveObjectJsonModel>() {

			@Override
			public void done(AppacitiveObjectJsonModel result) {
				if (result.mStatus.getStatusCode().equals("200")) {
					 callback.onSuccess(result.mAppacitiveObjects, result.mPagingInfo);
				} else {
					callback.onFailure(result.mStatus);
				}
			}
			
			@Override
			public void onFailed(AppacitiveError error) {
				if(callback != null) {
					callback.onFailure(error);
				}
			}
			
		};
		
		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveObjectJsonModel> searchTask = new BackgroundTask<AppacitiveObjectJsonModel>(internalCallback) {

				AppacitiveError error;
				AppacitiveObjectJsonModel response;
				
				@Override
				public AppacitiveObjectJsonModel run() {
					URL url;
					String urlString = Constants.ARTICLE_URL + schemaName
							+ "/find/all";
					if (query != null) {
						urlString = urlString + "?" + query;
					}
					try {
						url = new URL(urlString.replace(" ", "%20"));
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
							builder.registerTypeAdapter(AppacitiveObject.class,new AppacitiveObjectTypeAdapter());
							Gson gson = builder.create();

							Reader reader = new InputStreamReader(inputStream);
							response = gson.fromJson(reader, AppacitiveObjectJsonModel.class);
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
	 * Updates an AppacitiveObject.
	 */
	public void updateObject() {
		updateObject(null);
	}

	/**
	 * Updates an AppacitiveObject.
	 * 
	 * @param callback
	 *            Callback invoked when the search operation is successful or
	 *            failed.
	 */
	public void updateObject(final AppacitiveCallback callback) {
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
				if(callback != null) {
					callback.onFailure(error);
				}
			}
			
		};

		if (appacitive != null && appacitive.getSessionId() != null) {

			BackgroundTask<AppacitiveError> updateTask = new BackgroundTask<AppacitiveError>(internalCallback) {
				AppacitiveError error;

				@Override
				public AppacitiveError run() {
					URL url;
					String requestParams = AppacitiveObject.this
							.createPostParameters();

					try {
						url = new URL(Constants.ARTICLE_URL + AppacitiveObject.this.mSchemaType + "/"
								+ AppacitiveObject.this.mObjectId);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Content-Type", "application/json");
						connection.setRequestProperty("Content-Length", Integer.toString((requestParams.length())));
						AppacitiveHelper.addHeaders(connection);
						connection.setDoOutput(true);

						OutputStream os = connection.getOutputStream();
						os.write(requestParams.getBytes());
						os.close();

						InputStream inputStream;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode());
							this.setNetworkError(error);
						} else {
							inputStream = connection.getInputStream();
							GsonBuilder builder = new GsonBuilder();
							builder.registerTypeAdapter(AppacitiveObject.class, new AppacitiveObjectTypeAdapter());
							Gson gson = builder.create();
							Reader reader = new InputStreamReader(inputStream);
							AppacitiveObjectJsonModel response = gson.fromJson(reader, AppacitiveObjectJsonModel.class);
							inputStream.close();
							if (!response.mStatus.getStatusCode().equals("200")) {
								error = response.mStatus;
							} else {
								AppacitiveObject.this.updateAppacitiveObjectFields(response.mAppacitiveObject);
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
			updateTask.execute();
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

	// Setters and Getters
	public String getCreatedBy() {
		return mCreatedBy;
	}

	public String getSchemaType() {
		return mSchemaType;
	}

	void setSchemaType(String schemaType) {
		this.mSchemaType = schemaType;
	}

	public String getLastModifiedBy() {
		return mLastModifiedBy;
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

	public long getSchemaId() {
		return mSchemaId;
	}

	public void setSchemaId(long schemaId) {
		this.mSchemaId = schemaId;
	}

	public void setCreatedBy(String createdBy) {
		this.mCreatedBy = createdBy;
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

	public void setLastModifiedBy(String lastModifiedBy) {
		this.mLastModifiedBy = lastModifiedBy;
	}

	public void setRevision(long revision) {
		this.mRevision = revision;
	}

	public void setUTCDateCreated(Date UTCDateCreated) {
		this.mUTCDateCreated = UTCDateCreated;
	}

	public void setUTCLastUpdatedDate(Date UTCLastUpdatedDate) {
		this.mUTCLastUpdatedDate = UTCLastUpdatedDate;
	}

	public Date getUTCLastUpdatedDate() {
		return mUTCLastUpdatedDate;
	}

	@Override
	public String toString() {
		return "AppacitiveObject :--> mCreatedBy=" + mCreatedBy
				+ ", mSchemaType=" + mSchemaType + ", mLastModifiedBy="
				+ mLastModifiedBy + ", mObjectId=" + mObjectId + ", mRevision="
				+ mRevision + ", mSchemaId=" + mSchemaId + ", mUTCDateCreated="
				+ mUTCDateCreated + ", mUTCLastUpdatedDate="
				+ mUTCLastUpdatedDate + "] -- " + mProperties
				+ " -- " + mTags+ " -- " + mAttributes;
	}

	/*
	 * AppacitiveObjectJsonModel class for parsing response to Get required
	 * Property
	 */
	private static class AppacitiveObjectJsonModel {
		@SerializedName("articles")
		public ArrayList<AppacitiveObject> mAppacitiveObjects;
		@SerializedName("paginginfo")
		public AppacitivePagingInfo mPagingInfo;
		@SerializedName("status")
		public AppacitiveError mStatus;
		@SerializedName("article")
		public AppacitiveObject mAppacitiveObject;
	}

	/*
	 * AppacitiveObjectTypeAdapter for reading and parsing the json by json
	 * reader
	 */
	private static class AppacitiveObjectTypeAdapter extends TypeAdapter<AppacitiveObject> {

		@Override
		public AppacitiveObject read(JsonReader reader) throws IOException {
			AppacitiveObject appacitiveObject = new AppacitiveObject();
			if (reader.peek() == JsonToken.NULL) {
				reader.skipValue();
				return null;
			} else {
				reader.beginObject();
			}
			while (reader.hasNext()) {
				if (reader.peek() == JsonToken.NULL) {
					reader.skipValue();
				}
				String name = reader.nextName();
				if (name.equals("__id") && reader.peek() != JsonToken.NULL) {
					appacitiveObject.setObjectId(reader.nextLong());
				} else if (name.equals("__schematype") && reader.peek() != JsonToken.NULL) {
					appacitiveObject.setSchemaType(reader.nextString());
				} else if (name.equals("__schemaid") && reader.peek() != JsonToken.NULL) {
					appacitiveObject.setSchemaId(reader.nextLong());
				} else if (name.equals("__createdby") && reader.peek() != JsonToken.NULL) {
					appacitiveObject.setCreatedBy(reader.nextString());
				} else if (name.equals("__lastmodifiedby") && reader.peek() != JsonToken.NULL) {
					appacitiveObject.setLastModifiedBy(reader.nextString());
				} else if (name.equals("__revision") && reader.peek() != JsonToken.NULL) {
					appacitiveObject.setRevision(reader.nextLong());
				} else if (name.equals("__utcdatecreated") && reader.peek() != JsonToken.NULL) {
					Date utcDateCreated = fromJsonResponse(reader.nextString());
					appacitiveObject.setUTCDateCreated(utcDateCreated);
				} else if (name.equals("__utclastupdateddate") && reader.peek() != JsonToken.NULL) {
					Date utcLastUpdatedDate = fromJsonResponse(reader.nextString());
					appacitiveObject.setUTCLastUpdatedDate(utcLastUpdatedDate);
				} else if (name.equals("__tags")) {
					getTags(reader, appacitiveObject);
				} else if (name.equals("__attributes")) {
					getAttributes(reader, appacitiveObject);
				} else if (!name.equals("") && !name.startsWith("__")) {
					appacitiveObject.addProperty(name, reader.nextString());
				}
			}
			reader.endObject();
			return appacitiveObject;
		}

		private void getTags(JsonReader jsonReader,
				AppacitiveObject appacitiveObject) throws IOException {
			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				appacitiveObject.addTag(jsonReader.nextString());
			}
			jsonReader.endArray();
		}

		private void getAttributes(JsonReader reader, AppacitiveObject object)
				throws IOException {
			reader.beginObject();
			while (reader.hasNext()) {
				if (reader.peek() == JsonToken.NULL) {
					reader.skipValue();
				}
				String key = reader.nextName();
				String value = reader.nextString();
				object.addAttribute(key, value);
			}
			reader.endObject();
		}

		private Date fromJsonResponse(String dateString) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
			Date date = null;
			try {
				date = formatter.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}

		@Override
		public void write(JsonWriter writter, AppacitiveObject object)
				throws IOException {
		}
	}
}