package com.appacitive.android.model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.appacitive.android.callbacks.AppacitiveCallback;
import com.appacitive.android.util.AppacitiveRequestMethods;
import com.appacitive.android.util.Constants;

public class AppacitiveBlob {

	public static void uploadFile(final String path, final String fileName,
			final String mimeType) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive.getSessionId() != null) {
			BackgroundTask<Void> saveTask = new BackgroundTask<Void>() {

				@Override
				public Void run()  {
					String uploadFile = path + fileName;
					String actionUrl = Constants.FILE_UPLOAD_URL;
					final String end = "\r\n";
					final String twoHyphens = "--";
					final String boundary = "*********";

					URL url;
					try {
						url = new URL(actionUrl);
						HttpURLConnection conn = (HttpURLConnection) url
								.openConnection();

						conn.setDoInput(true);
						conn.setDoOutput(true);
						conn.setUseCaches(false);
						conn.setRequestMethod("POST");
						/* setRequestProperty */
						conn.setRequestProperty("Connection", "Keep-Alive");
						conn.setRequestProperty("Charset", "UTF-8");
						conn.setRequestProperty("Content-Type",
								"multipart/form-data;boundary=" + boundary);
						conn.setRequestProperty("Appacitive-Environment",
								appacitive.getEnvironment());
						conn.setRequestProperty("Appacitive-Session",
								appacitive.getSessionId());

						DataOutputStream ds = new DataOutputStream(
								conn.getOutputStream());
						ds.writeBytes(twoHyphens + boundary + end);
						ds.writeBytes("Content-Disposition: form-data; name=\"files[]\";filename=\""
								+ uploadFile + "\"" + end);
						ds.writeBytes(end);

						FileInputStream fStream = new FileInputStream(
								uploadFile);
						int bufferSize = 1024;
						byte[] buffer = new byte[bufferSize];
						int length = -1;

						while ((length = fStream.read(buffer)) != -1) {
							ds.write(buffer, 0, length);
						}
						ds.writeBytes(end);
						ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
						fStream.close();
						ds.flush();
						ds.close();
						InputStream inputStream = conn.getInputStream();
						StringBuffer stringBuffer = new StringBuffer();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(inputStream));
						String response;
						while ((response = reader.readLine()) != null) {
							stringBuffer.append(response);
						}
						Log.d("TAG", "" + stringBuffer.toString());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			saveTask.execute();
		}
	}

	public static void downloadFile(final String fileUrl, final String toFile,
			final AppacitiveCallback callback) {
		final Appacitive appacitive = Appacitive.getInstance();
		if (appacitive.getSessionId() != null) {
			BackgroundTask<Void> saveTask = new BackgroundTask<Void>() {

				@Override
				public Void run()  {
					try {
						URL url = new URL(fileUrl);
						HttpURLConnection connection = (HttpURLConnection) url
								.openConnection();
						connection.setRequestMethod(AppacitiveRequestMethods.GET.requestMethod());
						connection.setRequestProperty("Appacitive-Environment",appacitive.getEnvironment());
						connection.setRequestProperty("Appacitive-Session",appacitive.getSessionId());
						connection.setDoOutput(true);
						InputStream inputStream = connection.getInputStream();
						FileOutputStream outputStream = new FileOutputStream("/mnt/sdcard2/" + toFile, true);

						byte[] buffer = new byte[1024];
						while (inputStream.read(buffer) != -1) {
							outputStream.write(buffer);
						}
						inputStream.close();
						outputStream.close();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			saveTask.execute();
		}
	}
}
