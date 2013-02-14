package com.appacitive.android.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveDownloadCallback;
import com.appacitive.android.callbacks.AppacitiveUploadCallback;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * AppacitiveFile, provides the helper method to upload and download files from
 * appacitive.
 * 
 */

public class AppacitiveFile {

	/**
	 * Saves the files on the appacitive remote server.
	 * 
	 * @param fileName
	 * @param contentType
	 * @param validity
	 * @param data
	 * @param callback
	 */

	public static void uploadData(final String fileName,
			final String contentType, final int validity, final byte[] data,
			final AppacitiveUploadCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		BackgroundTask<Void> uploadTask = new BackgroundTask<Void>() {

			@Override
			public Void run() {
				AppacitiveError error;
				Map<String, Object> responseMap;
				String publicUrl = null;
				if (appacitive != null && appacitive.getSessionId() != null) {
					StringBuffer urlString = new StringBuffer(
							Constants.FILE_UPLOAD_URL);
					urlString = urlString.append("?filename=" + fileName
							+ "&expires=" + Long.MAX_VALUE);
					if (contentType != null) {
						urlString.append("&contenttype=" + contentType);
					}
					try {
						URL url = new URL(urlString.toString());
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Request failed " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode() + "");
							error.setMessage(connection.getResponseMessage());
						} else {
							InputStream is = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(is);
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
							is.close();

							if (error == null) {
								String uplaodUrlString = (String) responseMap.get("url");
								URL uploadUrl = new URL(uplaodUrlString);
								connection = (HttpURLConnection) uploadUrl.openConnection();
								if (contentType == null) {
									connection.setRequestProperty( "Content-Type", "application/octet-stream");
								} else {
									connection.setRequestProperty("Content-Type", contentType);
								}
								connection.setRequestProperty("Content-Length", data.length + "");
								connection.setRequestMethod(AppacitiveRequestMethods.PUT.requestMethod());
								connection.setDoOutput(true);

								OutputStream os = connection.getOutputStream();
								os.write(data);
								os.close();

								if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
									Log.w("TAG", "Request failed " + connection.getResponseMessage());
									error = new AppacitiveError();
									error.setStatusCode(connection.getResponseCode() + "");
									error.setMessage(connection.getResponseMessage());
								} else {
									publicUrl = getPublicUrl(fileName);
								}
							}
						}
						if (error == null) {
							callback.onSuccess(publicUrl);
						} else {
							callback.onFailure(error);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.d("TAG", "The exception is " + e);
					}
				}
				return null;
			}

		};
		uploadTask.execute();
	}

	public static void download(final String key,
			final AppacitiveDownloadCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		BackgroundTask<Void> downloadTask = new BackgroundTask<Void>() {

			@Override
			public Void run() {
				AppacitiveError error;
				Map<String, Object> responseMap;
				InputStream responseInputStream = null;
				if (appacitive != null && appacitive.getSessionId() != null) {
					String urlString = Constants.FILE_DOWNLOAD_URL + "/" + key;
					try {
						URL url = new URL(urlString.toString());
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.GET
										.requestMethod());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode()
									+ "");
							error.setMessage(connection.getResponseMessage());
						} else {
							InputStream is = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(is);
							BufferedReader bufferedReader = new BufferedReader(
									reader);
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
							is.close();

							if (error == null) {
								String downloadUrlString = (String) responseMap
										.get("uri");
								URL uploadUrl = new URL(downloadUrlString);
								connection = (HttpURLConnection) uploadUrl
										.openConnection();
								connection
										.setRequestMethod(AppacitiveRequestMethods.GET
												.requestMethod());

								if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
									Log.w("TAG",
											"Request failed "
													+ connection
															.getResponseMessage());
									error = new AppacitiveError();
									error.setStatusCode(connection
											.getResponseCode() + "");
									error.setMessage(connection
											.getResponseMessage());
								} else {
									responseInputStream = connection
											.getInputStream();
								}
							}
						}
						if (error == null) {
							callback.onSuccess(responseInputStream);
						} else {
							callback.onFailure(error);
						}

					} catch (Exception e) {
						e.printStackTrace();
						Log.d("TAG", "The exception is " + e);
					}
				}
				return null;
			}
		};
		downloadTask.execute();
	}

	public static void getDownloadURL(final String key,
			final AppacitiveDownloadCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		BackgroundTask<Void> downloadTask = new BackgroundTask<Void>() {

			@Override
			public Void run() {
				AppacitiveError error;
				Map<String, Object> responseMap;
				InputStream responseInputStream = null;
				if (appacitive != null && appacitive.getSessionId() != null) {
					String urlString = Constants.FILE_DOWNLOAD_URL + "/" + key;
					try {
						URL url = new URL(urlString.toString());
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection
								.setRequestMethod(AppacitiveRequestMethods.GET
										.requestMethod());
						connection.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG",
									"Request failed "
											+ connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode()
									+ "");
							error.setMessage(connection.getResponseMessage());
						} else {
							InputStream is = connection.getInputStream();
							InputStreamReader reader = new InputStreamReader(is);
							BufferedReader bufferedReader = new BufferedReader(
									reader);
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
							is.close();

							if (error == null) {
								String downloadUrlString = (String) responseMap
										.get("uri");
								URL uploadUrl = new URL(downloadUrlString);
								connection = (HttpURLConnection) uploadUrl
										.openConnection();
								connection
										.setRequestMethod(AppacitiveRequestMethods.GET
												.requestMethod());

								if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
									Log.w("TAG",
											"Request failed "
													+ connection
															.getResponseMessage());
									error = new AppacitiveError();
									error.setStatusCode(connection
											.getResponseCode() + "");
									error.setMessage(connection
											.getResponseMessage());
								} else {
									responseInputStream = connection
											.getInputStream();
								}
							}
						}
						if (error == null) {
							callback.onSuccess(responseInputStream);
						} else {
							callback.onFailure(error);
						}

					} catch (Exception e) {
						e.printStackTrace();
						Log.d("TAG", "The exception is " + e);
					}
				}
				return null;
			}
		};
		downloadTask.execute();
	}

	private static String getPublicUrl(String key) {
		AppacitiveError error;
		Appacitive appacitive = Appacitive.getInstance();
		Map<String, Object> responseMap;
		String urlString = Constants.FILE_DOWNLOAD_URL + "/" + key +"?expires=" + (525949 * 10);
		try {
			URL url = new URL(urlString.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
			connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
			connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.w("TAG","Request failed " + connection.getResponseMessage());
				error = new AppacitiveError();
				error.setStatusCode(connection.getResponseCode() + "");
				error.setMessage(connection.getResponseMessage());
			} else {
				InputStream is = connection.getInputStream();
				InputStreamReader reader = new InputStreamReader(is);
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
				is.close();

				String downloadUrlString = (String) responseMap.get("uri");
				return downloadUrlString;
			}
		} catch (Exception e) {
			Log.d("TAG", "Exception " + e);
		}
		return null;
	}
}
