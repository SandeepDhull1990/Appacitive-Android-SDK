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

import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

	public AppacitiveObject(String schemaType) {
		this.mSchemaType = schemaType;
	}

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
	public void saveObject(final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();

		if (appacitive != null) {
			BackgroundTask<Void> saveTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() {
					URL url;
					String requestParams = AppacitiveObject.this.createPostParameters();

					try {
						url = new URL(Constants.ARTICLE_URL + AppacitiveObject.this.mSchemaType);
						
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString((requestParams.length())));
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setDoOutput(true);
						
						OutputStream os = connection.getOutputStream();
						os.write(requestParams.getBytes());
						os.close();
						
						InputStream inputStream;
						Map<String, Object> responseMap = null;
						
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode()+"");
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
						if (callback != null) {
							if (error == null) {
								readArticle(responseMap);
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
			saveTask.execute();
		} else {
			Log.w("Appacitive","Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
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
			final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						String urlString = Constants.ARTICLE_URL + AppacitiveObject.this.mSchemaType + "/" + AppacitiveObject.this.mObjectId;
						if (deleteConnections) {
							urlString = urlString + "?deleteconnections=true";
						}
						url = new URL(urlString);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.DELETE.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						
						InputStream inputStream;
						Map<String, Object> responseMap = null;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode()+"");
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
							Type typeOfClass = new TypeToken<Map<String,Object>>(){}.getType();
							responseMap = gson.fromJson(buffer.toString(), typeOfClass);
							error = AppacitiveHelperMethods.checkForErrorInStatus(responseMap);
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

	public static void deleteObjectsWithIds(final ArrayList<String> objectIds, final String schemaName, final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> deleteTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					Gson gson = new Gson();
					HashMap<String, List<String>> requestMap = new HashMap<String, List<String>>();
					requestMap.put("idlist", objectIds);
					String requestParams = gson.toJson(requestMap);
					try {
						url = new URL(Constants.ARTICLE_URL + schemaName + "/bulkdelete");
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.POST.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setRequestProperty("Content-Type","application/json");
						connection.setRequestProperty("Content-Length",Integer.toString((requestParams.length())));

						OutputStream os = connection.getOutputStream();
						os.write(requestParams.getBytes());
						os.close();

						InputStream inputStream;
						Map<String, Object> responseMap = null;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Request failed " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode() + "");
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(inputStream);
							BufferedReader bufferedReader = new BufferedReader(reader);
							StringBuffer buffer = new StringBuffer();
							String response;
							while ((response = bufferedReader.readLine()) != null) {
								buffer.append(response);
							}
							Type typeOfClass = new TypeToken<Map<String,Object>>(){}.getType();
							responseMap = gson.fromJson(buffer.toString(), typeOfClass);
							error = AppacitiveHelperMethods.checkForErrorInStatus(responseMap);
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
			Log.w("Appacitive","Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	public void fetchObject(final AppacitiveFetchCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> fetchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					try {
						url = new URL(Constants.ARTICLE_URL + AppacitiveObject.this.mSchemaType + "/" + AppacitiveObject.this.mObjectId);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						
						InputStream inputStream;
						Map<String,Object>  responseMap = null;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Request failed " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setMessage(connection.getResponseMessage());
							error.setStatusCode(connection.getResponseCode() + "");
						} else {
							inputStream = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(inputStream);
							BufferedReader bufferedReader = new BufferedReader(reader);
							StringBuffer response = new StringBuffer();
							String buffer;
							while((buffer = bufferedReader.readLine()) != null) {
								response.append(buffer);
							}
							Gson gson = new Gson();
							Type typeOfClass = new TypeToken<Map<String,Object>>(){}.getType();
							responseMap = gson.fromJson(response.toString(), typeOfClass);
							error = AppacitiveHelperMethods.checkForErrorInStatus(responseMap);
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
		}
	}

	public static void fetchObjectsWithIds(final ArrayList<String> ids,
			final String schemaName, final AppacitiveFetchCallback callback) {
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
						url = new URL(Constants.ARTICLE_URL + schemaName + "/multiget" + "/" + queryParams);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						InputStream inputStream;
						Map<String, Object> responseMap = null;
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Request failed " + connection.getResponseMessage());
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
		}
	}

	public static void searchAllObjects(String schemaName,
			final AppacitiveFetchCallback callback) {
		searchObjects(schemaName, null, callback);
	}

	public static void searchObjects(final String schemaName, final String query, final AppacitiveFetchCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive != null) {
			BackgroundTask<Void> searchTask = new BackgroundTask<Void>(null) {
				AppacitiveError error;

				@Override
				public Void run() throws AppacitiveException {
					URL url;
					String urlString = Constants.ARTICLE_URL + schemaName + "/find/all";
					if (query != null) {
						urlString = urlString + "?" + query;
					}
					try {
						url = new URL(urlString.replace(" ", "%20"));
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
							error = AppacitiveHelperMethods.checkForErrorInStatus(responseMap);
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
			Log.w("Appacitive","Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
		}
	}

	private void readArticle(Map<String,Object> responseMap) {
		@SuppressWarnings("unchecked")
		Map<String,Object> articleMap = (Map<String, Object>) responseMap.get("article");
		this.mObjectId = new Long((String)articleMap.get("__id"));
		this.mSchemaId = new Long((String) articleMap.get("__schemaid"));
		this.mCreatedBy = (String) articleMap.get("__createdby");
		this.mLastModifiedBy = (String) articleMap.get("__lastmodifiedby");
		this.mRevision = new Long((String) articleMap.get("__revision"));
		try {
			this.mUTCDateCreated = fromJsonResponse((String) articleMap.get("__utcdatecreated"));
			this.mUTCLastUpdatedDate = fromJsonResponse((String) articleMap.get("__utclastupdateddate"));
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
	
	@Override
	public String toString() {
		return "AppacitiveObject :--> mCreatedBy=" + mCreatedBy
				+ ", mSchemaType=" + mSchemaType + ", mLastModifiedBy="
				+ mLastModifiedBy + ", mObjectId=" + mObjectId + ", mRevision="
				+ mRevision + ", mSchemaId=" + mSchemaId + ", mUTCDateCreated="
				+ mUTCDateCreated + ", mUTCLastUpdatedDate="
				+ mUTCLastUpdatedDate + "] -- " + mProperties.toString() ;
	}

}
