package com.appacitive.android.model;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveDownloadCallback;
import com.appacitive.android.callbacks.AppacitiveFetchUrlCallback;
import com.appacitive.android.callbacks.AppacitiveUploadCallback;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

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

	public static void uploadData(final String fileName, final String contentType, final long validity, 
			final byte[] data, final AppacitiveUploadCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();

		AppacitiveInternalCallback<AppacitiveFileJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveFileJsonModel>() {

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}

			@Override
			public void done(AppacitiveFileJsonModel result) {

				if (result.mStatus.getStatusCode().equals("200")) {
					callback.onSuccess(result.getPublicUrl());
				} else {
					callback.onFailure(result.mStatus);
				}
			}
		};
		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveFileJsonModel> uploadTask = new BackgroundTask<AppacitiveFileJsonModel>(internalCallback) {

				@Override
				public AppacitiveFileJsonModel run() {
					AppacitiveError error;
					AppacitiveFileJsonModel response = null;
					String publicUrl = null;

					StringBuffer urlString = new StringBuffer(Constants.FILE_UPLOAD_URL);
					urlString = urlString.append("?filename=" + fileName + "&expires=" + validity);
					if (contentType != null) {
						urlString.append("&contenttype=" + contentType);
					} else {
						urlString.append("&contenttype=application/octet-stream");
					}
					try {
						URL url = new URL(urlString.toString());
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Error : " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
							this.setNetworkError(error);
						} else {
							InputStream inputStream = connection.getInputStream();
							Gson gson = new Gson();
							Reader reader = new InputStreamReader(inputStream);
							response = gson.fromJson(reader,AppacitiveFileJsonModel.class);
							error = response.mStatus;
							inputStream.close();

							if (error.mStatusCode.equals("200")) {
								String uplaodUrlString = response.mUrl;
								URL uploadUrl = new URL(uplaodUrlString);
								connection = (HttpURLConnection) uploadUrl.openConnection();
								if (contentType == null) {
									connection.setRequestProperty("Content-Type","application/octet-stream");
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
									Log.w("TAG", "Error : " + connection.getResponseMessage());
									error = new AppacitiveError();
									error.setStatusCode(connection.getResponseCode());
									error.setMessage(connection.getResponseMessage());
								} else {
									publicUrl = getPublicUrl(fileName);
									response.setPublicUrl(publicUrl);
								}
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return response;
				}
			};
			uploadTask.execute();
		} else {
			Log.w("Appacitive", "Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	public static void download(final String fileName, final AppacitiveDownloadCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();
		AppacitiveInternalCallback<AppacitiveFileJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveFileJsonModel>() {

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}

			@Override
			public void done(AppacitiveFileJsonModel result) {
				if (result.mStatus.getStatusCode().equals("200")) {
					callback.onSuccess(result.getResult());
				} else {
					callback.onFailure(result.mStatus);
				}
			}
		};

		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveFileJsonModel> downloadTask = new BackgroundTask<AppacitiveFileJsonModel>(internalCallback) {
				
				@Override
				public AppacitiveFileJsonModel run() {
					AppacitiveError error;
					AppacitiveFileJsonModel response = null;

					String urlString = Constants.FILE_DOWNLOAD_URL + "/" + fileName;
					try {
						URL url = new URL(urlString.toString());
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG","Error : " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
						} else {
							InputStream inputStream = connection.getInputStream();
							Gson gson = new Gson();
							Reader reader = new InputStreamReader(inputStream);
							response = gson.fromJson(reader, AppacitiveFileJsonModel.class);
							error = response.mStatus;
							inputStream.close();

							if (error.mStatusCode.equals("200")) {
								String downloadUrlString = (String) response.mUri;
								URL uploadUrl = new URL(downloadUrlString);
								connection = (HttpURLConnection) uploadUrl.openConnection();
								connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
								if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
									Log.w("TAG", "Error : " + connection.getResponseMessage());
									error = new AppacitiveError();
									error.setStatusCode(connection.getResponseCode());
									error.setMessage(connection.getResponseMessage());
								} else {
									InputStream stream = connection.getInputStream();
									ByteArrayOutputStream bos = new ByteArrayOutputStream();
									int next = stream.read();
									while (next > -1) {
										bos.write(next);
										next = stream.read();
									}
									byte[] result = bos.toByteArray();
									response.setByteArray(result);
									bos.flush();
									bos.close();
								}
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return response;
				}
			};
			downloadTask.execute();
		} else {
			Log.w("Appacitive", "Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	public static void getDownloadURL(final String fileName, final long validity, final AppacitiveFetchUrlCallback callback) {

		final Appacitive appacitive = Appacitive.getInstance();
		AppacitiveInternalCallback<AppacitiveFileJsonModel> internalCallback = new AppacitiveInternalCallback<AppacitiveFile.AppacitiveFileJsonModel>() {

			@Override
			public void onFailed(AppacitiveError error) {
				if (callback != null) {
					callback.onFailure(error);
				}
			}

			@Override
			public void done(AppacitiveFileJsonModel result) {
				if (result.mStatus.getStatusCode().equals("200")) {
					callback.onSuccess(result.mUri);
				} else {
					callback.onFailure(result.mStatus);
				}
			}
		};
		if (appacitive != null && appacitive.getSessionId() != null) {
			BackgroundTask<AppacitiveFileJsonModel> downloadTask = new BackgroundTask<AppacitiveFileJsonModel>(internalCallback) {

				@Override
				public AppacitiveFileJsonModel run() {
					AppacitiveFileJsonModel response = null;
					AppacitiveError error;

					String urlString = Constants.FILE_DOWNLOAD_URL + "/" + fileName + "?expires=" + validity;
					try {
						URL url = new URL(urlString.toString());
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						AppacitiveHelper.addHeaders(connection);

						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							Log.w("TAG", "Error : " + connection.getResponseMessage());
							error = new AppacitiveError();
							error.setStatusCode(connection.getResponseCode());
							error.setMessage(connection.getResponseMessage());
						} else {
							InputStream is = connection.getInputStream();
							Gson gson = new Gson();
							Reader reader = new InputStreamReader(is);
							response = gson.fromJson(reader, AppacitiveFileJsonModel.class);
							error = response.mStatus;
							is.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return response;
				}
			};
			downloadTask.execute();
		} else {
			Log.w("Appacitive", "Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			AppacitiveError error = new AppacitiveError();
			error.setMessage("Appacitive Object is uninitialized. Initilaze the appacitive object first with proper api key");
			error.setStatusCode(8002);
			if (callback != null) {
				callback.onFailure(error);
			}
		}
	}

	private static String getPublicUrl(String key) {
		AppacitiveError error;
		String urlString = Constants.FILE_DOWNLOAD_URL + "/" + key + "?expires=" + Long.MAX_VALUE;
		try {
			URL url = new URL(urlString.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
			AppacitiveHelper.addHeaders(connection);

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.w("TAG", "Error " + connection.getResponseMessage());
				error = new AppacitiveError();
				error.setStatusCode(connection.getResponseCode());
				error.setMessage(connection.getResponseMessage());
			} else {
				InputStream is = connection.getInputStream();
				Gson gson = new Gson();
				Reader reader = new InputStreamReader(is);
				AppacitiveFileJsonModel responseModel = gson.fromJson(reader,AppacitiveFileJsonModel.class);
				error = responseModel.mStatus;
				is.close();
				String downloadUrlString = responseModel.mUri;
				return downloadUrlString;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static class AppacitiveFileJsonModel {
		@SerializedName("url")
		public String mUrl;

		@SerializedName("uri")
		public String mUri;

		@SerializedName("status")
		public AppacitiveError mStatus;

		public String mPublicUrl;
		public byte[] mResult;

		public void setPublicUrl(String publicUrl) {
			mPublicUrl = publicUrl;
		}

		public String getPublicUrl() {
			return mPublicUrl;
		}

		public void setByteArray(byte[] result) {
			mResult = result;
		}

		public byte[] getResult() {
			return mResult;
		}
	}
}
