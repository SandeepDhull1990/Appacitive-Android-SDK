package com.appacitive.android.model;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.JsonReader;
import android.util.JsonToken;

public class AppacitiveHelperMethods {

	public void checkForErrorStatus() {
	}
	
	public static AppacitiveError checkForErrorInStatus(JsonReader reader) throws IOException{
		AppacitiveError appacitiveError = new AppacitiveError();
		reader.beginObject();
		String name;
		while (reader.hasNext()) {
			name = reader.nextName();
			if (name.equals("code") && reader.peek() != JsonToken.NULL) {
				String code = reader.nextString();
				appacitiveError.setStatusCode(code);
			} else if(name.equals("message") && reader.peek() != JsonToken.NULL) {
				String message = reader.nextString();
				appacitiveError.setMessage(message);
			} else if(name.equals("referenceid") && reader.peek() != JsonToken.NULL) {
				String referenceId= reader.nextString();
				appacitiveError.setReferenceId(referenceId);
			} else if(name.equals("additionalmessages") && reader.peek() != JsonToken.NULL) {
//				Read the additional messages array - > currently skipping the value as don't know what is content of that array
				reader.skipValue();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		if(!appacitiveError.getStatusCode().equals("200")) {
			return appacitiveError;
		}
		return null;
	}
	
//		TODO : Handle the additional messages in the response status
	public static AppacitiveError checkForErrorInStatus(JSONObject statusObject) throws JSONException {
		AppacitiveError error = new AppacitiveError();
		error.setStatusCode(statusObject.getString("code"));
		error.setMessage(statusObject.getString("message"));
		error.setReferenceId(statusObject.getString("referenceid"));
		error.setVersion(statusObject.getString("version"));
//		error.setAdditionalMessages("");
		if(!error.getStatusCode().equals("200")) {
			return error;
		}
		return null;
	}
}
