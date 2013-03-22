package com.appacitive.android.model;

/**
 * Posible distance metrics.
 */
public enum AppacitiveDistanceMetrics {
	/**
	 * Kilometers metrics.
	 */
	KILOMETERS("km"),
	/**
	 * Mile metrics.
	 */
	MILES("m");
	
	private final String mDistanceMetrics;
	
	private AppacitiveDistanceMetrics(String distanceMetrics) {
		this.mDistanceMetrics = distanceMetrics;
	}

	@Override
	public String toString() {
		return mDistanceMetrics;
	}
	
}
