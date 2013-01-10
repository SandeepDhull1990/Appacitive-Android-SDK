package com.appacitive.android.util;

import java.util.Collection;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class AppacitiveUtility {
	
	public void toJSON(Map<?, ?> map, JSONStringer stringer)
			throws JSONException {
		stringer.object();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			stringer.key(String.valueOf(entry.getKey()));
			toJSONValue(entry.getValue(), stringer);
		}
		stringer.endObject();
	}

	public void toJSONValue(Object value, JSONStringer stringer)
			throws JSONException {
		if (value == null) {
			stringer.value(null);
		} else if (value instanceof Collection) {
			toJSON((Collection<?>) value, stringer);
		} else if (value instanceof Map) {
			toJSON((Map<?, ?>) value, stringer);
		} else if (value.getClass().isArray()) {
			if (value.getClass().getComponentType().isPrimitive()) {
				stringer.array();
				if (value instanceof byte[]) {
					for (byte b : (byte[]) value) {
						stringer.value(b);
					}
				} else if (value instanceof short[]) {
					for (short s : (short[]) value) {
						stringer.value(s);
					}
				} else if (value instanceof int[]) {
					for (int i : (int[]) value) {
						stringer.value(i);
					}
				} else if (value instanceof float[]) {
					for (float f : (float[]) value) {
						stringer.value(f);
					}
				} else if (value instanceof double[]) {
					for (double d : (double[]) value) {
						stringer.value(d);
					}
				} else if (value instanceof char[]) {
					for (char c : (char[]) value) {
						stringer.value(c);
					}
				} else if (value instanceof boolean[]) {
					for (boolean b : (boolean[]) value) {
						stringer.value(b);
					}
				}
				stringer.endArray();
			} else {
				toJSON((Object[]) value, stringer);
			}
		} else {
			stringer.value(value);
		}
	}

	public void toJSON(Object[] array, JSONStringer stringer)
			throws JSONException {
		stringer.array();
		for (Object value : array) {
			toJSONValue(value, stringer);
		}
		stringer.endArray();
	}

	public void toJSON(Collection<?> collection, JSONStringer stringer)
			throws JSONException {
		stringer.array();
		for (Object value : collection) {
			toJSONValue(value, stringer);
		}
		stringer.endArray();
	}
	public static JSONObject toJSON(Map<?,?> map) throws JSONException {
		AppacitiveUtility utility = new AppacitiveUtility();
		JSONStringer stringer = new JSONStringer();
		stringer.object();
		stringer.key("obj");
		utility.toJSON(map, stringer);
		stringer.endObject();
		JSONObject object = new JSONObject(stringer.toString());
		JSONObject responseObject = object.getJSONObject("obj");
		return responseObject;
	}
	
}
