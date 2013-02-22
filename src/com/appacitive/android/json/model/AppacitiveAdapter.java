package com.appacitive.android.json.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.util.Log;
import com.appacitive.android.model.AppacitiveObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class AppacitiveAdapter extends TypeAdapter<AppacitiveObject> {
	
	@Override
	public AppacitiveObject read(JsonReader jsonReader) throws IOException {
		String createdBy;
		String schemaType;
		String lastModifiedBy;
		long objectId;
		long revision = -999999;
		long schemaId;
		Date UTCDateCreated;
		Date UTCLastUpdatedDate;
		
		AppacitiveObject appacitiveObject = new AppacitiveObject()	;
		jsonReader.beginObject();
		while (jsonReader.hasNext()) {
			if (jsonReader.peek() == JsonToken.NULL) {
				jsonReader.skipValue();
			}
			String name = jsonReader.nextName();
			if (name.equals("__id") && jsonReader.peek() != JsonToken.NULL) {
				objectId = jsonReader.nextLong();
				appacitiveObject.setObjectId(objectId);
			} else if (name.equals("__schematype") && jsonReader.peek() != JsonToken.NULL) {
				schemaType = jsonReader.nextString();
				appacitiveObject.setSchemaType(schemaType);
			} else if (name.equals("__schemaid")
					&& jsonReader.peek() != JsonToken.NULL) {
				schemaId = jsonReader.nextLong();
				appacitiveObject.setSchemaId(schemaId);
			} else if (name.equals("__createdby")
					&& jsonReader.peek() != JsonToken.NULL) {
				createdBy = jsonReader.nextString();
				appacitiveObject.setCreatedBy(createdBy);
			} else if (name.equals("__lastmodifiedby")
					&& jsonReader.peek() != JsonToken.NULL) {
				lastModifiedBy = jsonReader.nextString();
				appacitiveObject.setLastModifiedBy(lastModifiedBy);
			} else if (name.equals("__revision")
					&& jsonReader.peek() != JsonToken.NULL) {
				revision = jsonReader.nextLong();
				appacitiveObject.setRevision(revision);
			} else if (name.equals("__utcdatecreated")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					UTCDateCreated = fromJsonResponse(jsonReader
							.nextString());
					appacitiveObject.setUTCDateCreated(UTCDateCreated);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__utclastupdateddate")
					&& jsonReader.peek() != JsonToken.NULL) {
				try {
					UTCLastUpdatedDate = fromJsonResponse(jsonReader
							.nextString());
					appacitiveObject.setUTCLastUpdatedDate(UTCLastUpdatedDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (name.equals("__tags")) {
				fetchTags(jsonReader, appacitiveObject);
			} else if (name.equals("__attributes")) {
				fetchAttributes(jsonReader, appacitiveObject);
			} else if(!name.equals("")){
				appacitiveObject.addProperty(name, jsonReader.nextString());
			}
		}
		jsonReader.endObject();
		return appacitiveObject;
	}

	private void fetchTags(JsonReader jsonReader, AppacitiveObject appacitiveObject) throws IOException{
		jsonReader.beginArray();
	     while (jsonReader.hasNext()) {
	       appacitiveObject.addTag(jsonReader.nextString());
	     }
		jsonReader.endArray();
	}

	private void fetchAttributes(JsonReader reader, AppacitiveObject object) throws IOException {
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
	
	@Override
	public void write(JsonWriter arg0, AppacitiveObject arg1) throws IOException {
		
	}
	
	private Date fromJsonResponse(String dateString) throws ParseException {
//		Log.d("TAG", "json-  " + dateString + "   and formatted is - ");
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		Date date = formatter.parse(dateString);
		return date;
	}
}