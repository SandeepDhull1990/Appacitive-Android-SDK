package com.appacitive.android.model;

import com.google.gson.annotations.SerializedName;

public class AppacitivePagingInfo {
	@SerializedName("pagenumber")
	public int mPageNumber;
	@SerializedName("pagesize")
	public int mPageSize;
	@SerializedName("totalrecords")
	public int mTotalRecords;

	@Override
	public String toString() {
		return "AppacitivePagingInfo : mPageNumber = " + mPageNumber
				+ ", mPageSize = " + mPageSize + ", mTotalRecords = "
				+ mTotalRecords;
	}
}
