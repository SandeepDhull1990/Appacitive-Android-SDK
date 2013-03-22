package com.appacitive.android.model;

import java.util.ArrayList;
import java.util.Date;

/**
 * Helper class that helps in building the query.
 * @author Sandeep Dhull
 */
public class AppacitiveQuery {

	/**
	 * Helper method to generate an equal to query string.
	 * 
	 * @param propertyName
	 *            name of the property to search for
	 * @param propertyValue
	 *            the value of the property to equate to.
	 * @return Query string.
	 */
	public static String queryForEqualCondition(String propertyName,
			String propertyValue) {
		if (propertyName != null && propertyValue != null) {
			String query = String.format("*%s=='%s'", propertyName,
					propertyValue);
			return query;
		}
		return null;
	}

	/**
	 * Helper method to generate an equal query string.
	 * @param propertyName
	 *            name of the property to search for
	 * @param propertyValue
	 *            the date to equate to.
	 * @return Query string.
	 */
	public static String queryForEqualCondition(String propertyName,
			Date propertyValue) {
		if (propertyName != null && propertyValue != null) {
			String query = String.format("*%s==date('%s')", propertyName,
					propertyValue);
			return query;
		}
		return null;
	}

	/**
	 * Helper method to generate a query string for like condition.
	 * 
	 * @param propertyName
	 *            name of the property to search for
	 * @param propertyValue
	 *            the value of the property.
	 * @return Query String.
	 */
	public static String queryForLikeCondition(String propertyName,
			String propertyValue) {
		if (propertyName != null && propertyValue != null) {
			String query = String.format("*%s like '%s'", propertyName,
					propertyValue);
			return query;
		}
		return null;
	}

	/**
	 * Helper method to generate a greater than query string. 
	 * 
	 * @param propertyName
	 *            name of the property to search for
	 * @param propertyValue
	 *            value that the property should be greater than.
	 * @return Query String.
	 */
	public static String queryForGreaterThanCondition(String propertyName,
			String propertyValue) {
		if (propertyName != null && propertyValue != null) {
			String query = String.format("*%s>'%s'", propertyName,
					propertyValue);
			return query;
		}
		return null;
	}
	/**
	 * Helper method to generate a greater than equal to query string. 
	 * 
	 * @param propertyName
	 *            name of the property to search for
	 * @param propertyValue
	 *            value that the property should be greater than.
	 * @return Query String.
	 */
	public static String queryForGreaterThanEqualToCondition(String propertyName,
			String propertyValue) {
		if (propertyName != null && propertyValue != null) {
			String query = String.format("*%s>='%s'", propertyName,
					propertyValue);
			return query;
		}
		return null;
	}

	/**
	 * Helper method to generate a less than query string. 
	 * 
	 * @param propertyName
	 *            name of the property to search for.
	 * @param propertyValue
	 *            value that the property should be greater than.
	 * @return Query String.
	 */	
	public static String queryForLessThanCondition(String propertyName,
			String propertyValue) {
		if (propertyName != null && propertyValue != null) {
			String query = String.format("*%s<'%s'", propertyName,
					propertyValue);
			return query;
		}
		return null;
	}
	/**
	 * Helper method to generate a less than equal to query string. 
	 * 
	 * @param propertyName
	 *            name of the property to search for.
	 * @param propertyValue
	 *            value that the property should be greater than.
	 * @return Query String.
	 */	
	public static String queryForLessThanEqualToCondition(String propertyName,
			String propertyValue) {
		if (propertyName != null && propertyValue != null) {
			String query = String.format("*%s<='%s'", propertyName,
					propertyValue);
			return query;
		}
		return null;
	}
	
	/**
	 * Helper method to generate a query string for page size.
	 * @param psize an integer value for the page size.
	 * @return Query String.
	 */
	public static String queryStringForPageSize(int psize) {
		return String.format("psize=%d", psize);
	}

	/**
	 * Helper method to generate a query string for page num.
	 * @param pnum an integer value for the page num.
	 * @return Query String.
	 */
	public static String queryStringForPageNumber(int pnum) {
		return String.format("pnum=%d", pnum);
	}

	
	/**
	 * Helper method to generate a query string for geocode search.
	 * @param propertyName name of the geocode property of the schema to search for.
	 * @param latitude the geocode latitude to search for.
	 * @param longitude the geocode longitude to search for.
	 * @param radius the radios around the location to look for.
	 * @param metrics the distance either in km or miles.
	 * @return Query String.
	 */
	public static String queryStringForGeocodeProperty(String propertyName, double latitude, double longitude, int radius,
			AppacitiveDistanceMetrics metrics) {
		String query = "*" + propertyName + " within_circle " + latitude + "," 
			+ longitude + "," + radius + " " + metrics.toString();
		return query;
	}

	/**
	 * Helper method to generate query string for polygon search.
	 * @param propertyName name of the geocode property of the schema to search for. 
	 * @param coordinates list of coordinates to be included in the polygon search.
	 * @return
	 */
	public static String queryStringForPolygonProperty(String propertyName,ArrayList<AppacitiveGeopoint> coordinates) {
		StringBuffer query = new StringBuffer();
		query.append("*" + propertyName + " within_polygon ");
		for(int i = 0 ; i < coordinates.size() ; i++) {
			AppacitiveGeopoint point = coordinates.get(i);
			query.append(point.toString());
			if(i < coordinates.size() - 1) {
				query.append("|");
			}
		}
		return query.toString();
	}
	
	
	/**
	 * Helper method to generate a query string for search with one or more tags.
	 * @param tags an array of tags to search for.
	 * @return Query String.
	 */
	public static String queryStringForSearchWithOneOrMoreTags(ArrayList<String> tags) {
		if (tags == null) {
			return null;
		}
		StringBuffer query = new StringBuffer();
		query.append("tagged_with_one_or_more ('");
		int i = 0;
		for (i = 0; i < tags.size() - 1; i++) {
			query.append(tags.get(i) + ",");
		}
		query.append(tags.get(i) + "')");
		return query.toString();
	}

	/**
	 * Helper method to generate a query string for search with all tags.
	 * @param tags an array of tags to search for.
	 * @return Query String.
	 */	public static String queryStringForSearchWithAllTags(ArrayList<String> tags) {
		if (tags == null) {
			return null;
		}
		StringBuffer query = new StringBuffer();
		query.append("tagged_with_all ('");
		int i = 0;
		for (i = 0; i < tags.size() - 1; i++) {
			query.append(tags.get(i) + ",");
		}
		query.append(tags.get(i) + "')");
		return query.toString();
	}

	 /**
	  * Helper method to generate a AND query string.
	  * @param queries - list of queries.
	  */
	 
	 public static String generateANDQueryStringForQueries(ArrayList<String> queries) {
		 return generateQueryStringForQueries(queries, "and");
	 }

	 public static String generateORQueryStringForQueries(ArrayList<String> queries) {
		 return generateQueryStringForQueries(queries, "or");
	 }
	 
	private static String generateQueryStringForQueries(ArrayList<String> queries, String operator) {
		StringBuffer query = null;
		
		for (String q : queries) {
			if (query == null) {
				query = new StringBuffer();
				query.append(q);
			}else {
				query.append(String.format("%s %s", operator, q));
			}
		}
		return String.format("(%s)", query.toString());
	}
}
