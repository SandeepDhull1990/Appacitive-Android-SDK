package com.appacitive.android.model;

import java.util.ArrayList;
import java.util.Date;

public class AppacitiveQuery {
	
	public static String queryForEqualCondition (String propertyName, String propertyValue) {
		if(propertyName != null && propertyValue != null) {
			String query = String.format("*%s=='%s'", propertyName, propertyValue);
			return query;
		}
		return null;
	}
	
	public static String queryForEqualCondition (String propertyName, Date propertyValue) {
		if(propertyName != null && propertyValue != null) {
			String query = String.format("*%s==date('%s')", propertyName, propertyValue);
			return query;
		}
		return null;
	}
	
	public static String queryForLikeCondition (String propertyName, String propertyValue) {
		if(propertyName != null && propertyValue != null) {
			String query = String.format("*%s like '%s'", propertyName, propertyValue);
			return query;
		}
		return null;
	}
	
	public static String queryForGreaterThanCondition (String propertyName, String propertyValue) {
		if(propertyName != null && propertyValue != null) {
			String query = String.format("*%s>'%s'", propertyName, propertyValue);
			return query;
		}
		return null;
	}
	
	public static String queryForLessThanCondition (String propertyName, String propertyValue) {
		if(propertyName != null && propertyValue != null) {
			String query = String.format("*%s<'%s'", propertyName, propertyValue);
			return query;
		}
		return null;
	}
	
	public static String queryStringForPageSize (int psize) {
		return String.format("psize=%d", psize);
	}
	
	public static String queryStringForPageNumber (int pnum) {
		return String.format("pnum=%d", pnum);
	}
	
//	TODO : Add query for geocode property 
//	TODO : Add query for Polygon search
	
	public static String queryStringForSearchWithOneOrMoreTags (ArrayList<String> tags) {
		if(tags == null) {
			return null;
		}
		StringBuffer query = new StringBuffer();
		query.append("tagged_with_one_or_more ('");
		int i = 0;
		for(i = 0 ; i < tags.size() - 1; i ++) {
			query.append(tags.get(i)  + ",");
		}
		query.append(tags.get(i) + "')");
		return query.toString();
	}
	
	public static String queryStringForSearchWithAllTags (ArrayList<String> tags) {
		if(tags == null) {
			return null;
		}
		StringBuffer query = new StringBuffer();
		query.append("tagged_with_all ('");
		int i = 0;
		for(i = 0 ; i < tags.size() - 1; i ++) {
			query.append(tags.get(i)  + ",");
		}
		query.append(tags.get(i) + "')");
		return query.toString();
	}
	
}
