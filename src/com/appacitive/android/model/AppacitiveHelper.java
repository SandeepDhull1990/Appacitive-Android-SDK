package com.appacitive.android.model;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

class AppacitiveHelper {

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
		if(Integer.parseInt(code) == 200) {
			return null;
		}
		error.setStatusCode(Integer.parseInt(code));
		error.setMessage((String)statusMap.get("message"));
		error.setReferenceId((String)statusMap.get("referenceid"));
		error.setVersion((String)statusMap.get("version"));
		error.setAdditionalMessages((ArrayList<String>)statusMap.get("additionalmessages"));
		return error;
	}
	
	public static Map<String,Object> getProperties(Map<String,Object> map) {
		Map<String,Object> propertiesMap = new HashMap<String, Object>();
		for(String key : map.keySet()) {
			if(!key.startsWith("__")) {
				propertiesMap.put(key, map.get(key));
			}
		}
		return propertiesMap;
	}
	
	public static AppacitiveObject readArticleFromStream (JsonReader reader) throws IOException {
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

	private static void getTags(JsonReader jsonReader,
			AppacitiveObject appacitiveObject) throws IOException {
		jsonReader.beginArray();
		while (jsonReader.hasNext()) {
			appacitiveObject.addTag(jsonReader.nextString());
		}
		jsonReader.endArray();
	}

	private static void getAttributes(JsonReader reader, AppacitiveObject object) throws IOException {
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

	@SuppressLint("SimpleDateFormat")
	private static Date fromJsonResponse(String dateString) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
		Date date = null;
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static void addHeaders(HttpURLConnection connection) {
		Appacitive appacitive = Appacitive.getInstance();
		connection.setRequestProperty("Appacitive-Session", appacitive.getSessionId());
		connection.setRequestProperty("Appacitive-Environment", appacitive.getEnvironment());
		if(AppacitiveUser.currentUser != null) {
			connection.setRequestProperty("Appacitive-User-Auth", AppacitiveUser.currentUser.mUserToken);
		}
	}
	
}
