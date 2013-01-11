package com.appacitive.android.model;

public class AppacitivePagingInfo {

	private long mPageNumber;
	private long mPageSize;
	private long mTotalRecords;
	
	public long getPageNumber() {
		return mPageNumber;
	}
	public void setPageNumber(long pageNumber) {
		this.mPageNumber = pageNumber;
	}
	public long getPageSize() {
		return mPageSize;
	}
	public void setPageSize(long pageSize) {
		this.mPageSize = pageSize;
	}
	public long getTotalRecords() {
		return mTotalRecords;
	}
	public void setTotalRecords(long totalRecords) {
		this.mTotalRecords = totalRecords;
	}
	@Override
	public String toString() {
		return "AppacitivePagingInfo [mPageNumber=" + mPageNumber
				+ ", mPageSize=" + mPageSize + ", mTotalRecords="
				+ mTotalRecords + "]";
	}
	
	
}
