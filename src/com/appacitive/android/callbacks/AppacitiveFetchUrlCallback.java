package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;

public interface AppacitiveFetchUrlCallback {

	void onSuccess(String downloadUrlString);

	void onFailure(AppacitiveError error);

}
