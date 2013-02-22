package com.appacitive.android.json.model;

import java.util.ArrayList;

import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitiveObject;
import com.google.gson.annotations.SerializedName;

public class AppacitiveObjectModel {
	@SerializedName("articles")
	public ArrayList<AppacitiveObject> mAppacitiveObjects;
	@SerializedName("paginginfo")
	public AppacitivePagingInfoModel mPagingInfo;
	@SerializedName("status")
	public AppacitiveError mStatus;
	@SerializedName("article")
	public AppacitiveObject mAppacitiveObject;
}
