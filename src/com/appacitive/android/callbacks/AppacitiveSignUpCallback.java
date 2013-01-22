package com.appacitive.android.callbacks;

import com.appacitive.android.model.AppacitiveError;
import com.appacitive.android.model.AppacitiveUser;

public interface AppacitiveSignUpCallback {
	public void onSuccess(AppacitiveUser user);
	public void onFailure(AppacitiveError error);
}
