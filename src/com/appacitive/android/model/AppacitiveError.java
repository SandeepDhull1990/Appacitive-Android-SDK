package com.appacitive.android.model;

import java.util.ArrayList;

public class AppacitiveError {

	private String mStatusCode;
	private String mMessage;
	private String mReferenceId;
	private String mVersion;
	private ArrayList<String> mAdditionalMessages;
	
	public String getStatusCode() {
		return mStatusCode;
	}
	public void setStatusCode(String statusCode) {
		this.mStatusCode = statusCode;
	}
	public String getMessage() {
		return mMessage;
	}
	public void setMessage(String message) {
		this.mMessage = message;
	}
	public String getReferenceId() {
		return mReferenceId;
	}
	public void setReferenceId(String referenceId) {
		this.mReferenceId = referenceId;
	}
	public String getVersion() {
		return mVersion;
	}
	public void setVersion(String version) {
		this.mVersion = version;
	}
	public ArrayList<String> getAdditionalMessages() {
		return mAdditionalMessages;
	}
	public void setAdditionalMessages(ArrayList<String> additionalMessages) {
		this.mAdditionalMessages = additionalMessages;
	}
	
	@Override
	public String toString() {
		return "AppacitiveError :--> mStatusCode=" + mStatusCode + ", mMessage="
				+ mMessage + ", mReferenceId=" + mReferenceId + ", mVersion="
				+ mVersion;
	}
	
}
