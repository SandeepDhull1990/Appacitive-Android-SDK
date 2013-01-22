package com.appacitive.android.model;

public enum AppacitiveDistanceMetrics {
	KILOMETERS("km"),
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
