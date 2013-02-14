package com.appacitive.android.model;

/**
 * AppacitiveGeopoint, a class to represent a geopoint.
 * @author Sandeep Dhull
 *
 */
public class AppacitiveGeopoint {
	
	/**
	 * The latitude value
	 */
	public double latitude;
	/**
	 * The longitude value
	 */
	public double longitude;
	
	@Override
	public String toString() {
		return String.format(latitude+","+longitude);
	}
}
