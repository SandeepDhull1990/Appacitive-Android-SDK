package com.appacitive.android.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.callbacks.AppacitiveDownloadCallback;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * AppacitiveFile, provides the helper method to upload and download files from appacitive.
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
			final AppacitiveCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		BackgroundTask<Void> uploadTask = new BackgroundTask<Void>() {

			@Override
			public Void run() {
				AppacitiveError error;
				Map<String, Object> responseMap;
				if (appacitive != null && appacitive.getSessionId() != null) {
					StringBuffer urlString = new StringBuffer(
							Constants.FILE_UPLOAD_URL);
					urlString = urlString.append("?filename=" + fileName
							+ "&expires=" + validity);
					if (contentType != null) {
						urlString.append("&contenttype=" + contentType);
					}
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
								String uplaodUrlString = (String) responseMap
										.get("url");
								URL uploadUrl = new URL(uplaodUrlString);
								connection = (HttpURLConnection) uploadUrl
										.openConnection();
								if (contentType == null) {
									connection.setRequestProperty(
											"Content-Type",
											"application/octet-stream");
								} else {
									connection.setRequestProperty(
											"Content-Type", contentType);
								}
								connection.setRequestProperty("Content-Length",
										data.length + "");
								connection
										.setRequestMethod(AppacitiveRequestMethods.PUT
												.requestMethod());
								connection.setDoOutput(true);

								OutputStream os = connection.getOutputStream();
								os.write(data);
								os.close();

								if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
									Log.w("TAG",
											"Request failed " + connection.getResponseMessage());
									error = new AppacitiveError();
									error.setStatusCode(connection
											.getResponseCode() + "");
									error.setMessage(connection
											.getResponseMessage());
								}
							}
						}
						if (error == null) {
							callback.onSuccess();
						} else {
							callback.onFailure(error);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.d("TAG",
								"The exception is " + e.getLocalizedMessage());
					}
				}
				return null;
			}

		};
		uploadTask.execute();
	}

	public static void uploadFile(final String fileName,
			final String contentType, final int validity,
			final InputStream inputStream, final AppacitiveCallback callback) {
		byte[] data;
		try {
			data = new byte[inputStream.available()];
			inputStream.read(data);
			uploadData(fileName, contentType, validity, data, callback);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());

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
							responseMap = gson.fromJson(buffer.toString(),typeOfClass);
							error = AppacitiveHelper.checkForErrorInStatus(responseMap);
							is.close();

							if (error == null) {
								String downloadUrlString = (String) responseMap.get("uri");
								URL uploadUrl = new URL(downloadUrlString);
								connection = (HttpURLConnection) uploadUrl.openConnection();
								connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());

								if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
									Log.w("TAG","Request failed " + connection.getResponseMessage());
									error = new AppacitiveError();
									error.setStatusCode(connection.getResponseCode() + "");
									error.setMessage(connection.getResponseMessage());
								} else {
									responseInputStream = connection.getInputStream();
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
						Log.d("TAG",
								"The exception is " + e.getLocalizedMessage());
					}
				}
				return null;
			}
		};
		downloadTask.execute();
	}

}
