package com.appacitive.android.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	
}
