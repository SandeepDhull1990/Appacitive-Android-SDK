package com.appacitive.android.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.JsonReader;
import android.util.JsonToken;

public class AppacitiveHelperMethods {

	public static AppacitiveError checkForErrorInStatus(JsonReader reader)
			throws IOException {
		AppacitiveError appacitiveError = new AppacitiveError();
		reader.beginObject();
		String name;
		while (reader.hasNext()) {
			name = reader.nextName();
			if (name.equals("code") && reader.peek() != JsonToken.NULL) {
				String code = reader.nextString();
				appacitiveError.setStatusCode(code);
			} else if (name.equals("message") && reader.peek() != JsonToken.NULL) {
				String message = reader.nextString();
				appacitiveError.setMessage(message);
			} else if (name.equals("referenceid")
					&& reader.peek() != JsonToken.NULL) {
				String referenceId = reader.nextString();
				appacitiveError.setReferenceId(referenceId);
			} else if (name.equals("additionalmessages")
					&& reader.peek() != JsonToken.NULL) {
				// The contents are array of string.
				reader.skipValue();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		if (!appacitiveError.getStatusCode().equals("200")) {
			return appacitiveError;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static AppacitiveError checkForErrorInStatus(Map<String,Object> response) {
		AppacitiveError error = new AppacitiveError();
		Map<String,Object> statusMap ;
		if(response.containsKey("status")) {
			statusMap = (Map<String,Object>)response.get("status");
		} else {
			statusMap = response;
		}
		String code = (String)statusMap.get("code");
		if(code.equals("200")) {
			return null;
		}
		error.setStatusCode(code);
		error.setMessage((String)statusMap.get("message"));
		error.setReferenceId((String)statusMap.get("referenceid"));
		error.setVersion((String)statusMap.get("version"));
		error.setAdditionalMessages((ArrayList<String>)statusMap.get("additionalmessages"));
		return error;
	}

	public static AppacitiveError checkForErrorInStatus(com.google.gson.stream.JsonReader reader) throws IOException {
		AppacitiveError appacitiveError = new AppacitiveError();
		reader.beginObject();
		String name;
		while (reader.hasNext()) {
			name = reader.nextName();
			if (name.equals("code") && reader.peek() != com.google.gson.stream.JsonToken.NULL) {
				String code = reader.nextString();
				appacitiveError.setStatusCode(code);
			} else if (name.equals("message") && reader.peek() != com.google.gson.stream.JsonToken.NULL) {
				String message = reader.nextString();
				appacitiveError.setMessage(message);
			} else if (name.equals("referenceid") && reader.peek() != com.google.gson.stream.JsonToken.NULL) {
				String referenceId = reader.nextString();
				appacitiveError.setReferenceId(referenceId);
			} else if (name.equals("additionalmessages") && reader.peek() != com.google.gson.stream.JsonToken.NULL) {
				// Read the additional messages array -> currently skipping the
				// value as don't know what is content of that array
				reader.skipValue();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		if (!appacitiveError.getStatusCode().equals("200")) {
			return appacitiveError;
		}
		return null;
	}

	// TODO : Handle the additional messages in the response status
	public static AppacitiveError checkForErrorInStatus(JSONObject statusObject)
			throws JSONException {
		AppacitiveError error = new AppacitiveError();
		error.setStatusCode(statusObject.getString("code"));
		error.setMessage(statusObject.getString("message"));
		error.setReferenceId(statusObject.getString("referenceid"));
		error.setVersion(statusObject.getString("version"));
		// error.setAdditionalMessages("");
		if (!error.getStatusCode().equals("200")) {
			return error;
		}
		return null;
	}

	public static AppacitivePagingInfo readPagingInfo(JsonReader reader)
			throws IOException {
		AppacitivePagingInfo pagingInfo = new AppacitivePagingInfo();
		reader.beginObject();
		String name;

		while (reader.hasNext()) {
			name = reader.nextName();
			if (name.equals("pagenumber") && reader.peek() != JsonToken.NULL) {
				pagingInfo.setPageNumber(reader.nextLong());
			} else if (name.equals("pagesize")
					&& reader.peek() != JsonToken.NULL) {
				pagingInfo.setPageSize(reader.nextLong());
			} else if (name.equals("totalrecords")
					&& reader.peek() != JsonToken.NULL) {
				pagingInfo.setTotalRecords(reader.nextLong());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return pagingInfo;

	}

	public static AppacitivePagingInfo readPagingInfo(JSONObject object)
			throws JSONException {
		AppacitivePagingInfo pagingInfo = new AppacitivePagingInfo();
		pagingInfo.setPageNumber(object.getLong("pagenumber"));
		pagingInfo.setPageSize(object.getLong("pagesize"));
		pagingInfo.setTotalRecords(object.getLong("totalrecords"));
		return pagingInfo;

	}

}
