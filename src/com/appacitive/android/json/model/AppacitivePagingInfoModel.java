package com.appacitive.android.json.model;

import com.google.gson.annotations.SerializedName;

public class AppacitivePagingInfoModel {
@SerializedName("pagenumber")
public int mPageNumber;
@SerializedName("pagesize")
public int mPageSize;
@SerializedName("totalrecords")
public int mTotalRecords;

}
