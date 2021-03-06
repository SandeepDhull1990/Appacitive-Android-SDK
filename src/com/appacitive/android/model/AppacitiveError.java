package com.appacitive.android.model;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * AppacitiveError, describes the error occured during appacitive operation.
 * @author Sandeep Dhull
 *
 */
public class AppacitiveError {
	@SerializedName("code")
	public String mStatusCode;
	@SerializedName("message")
	private String mMessage;
	@SerializedName("referenceid")
	private String mReferenceId;
	@SerializedName("version")
	private String mVersion;
	@SerializedName("additionalmessages")
	private ArrayList<String> mAdditionalMessages;

	/**
	 * Returns the status code of the error
	 * @return errorCode
	 */
	public String getStatusCode() {
		return mStatusCode;
	}

	/**
	 * Sets the status code of the error
	 * @param statusCode Status code
	 */
	public void setStatusCode(int statusCode) {
		this.mStatusCode = String.format("%d", statusCode);
	}

	/**
	 * Returns the message
	 * @return Error message
	 */
	public String getMessage() {
		return mMessage;
	}

	/**
	 * Sets the error message.
	 * @param message Error message to set.
	 */
	public void setMessage(String message) {
		this.mMessage = message;
	}
	public String getReferenceId() {
		return mReferenceId;
	}

	/**
	 * Returns the reference ID.
	 * @param referenceId The reference ID of the error
	 */
	public void setReferenceId(String referenceId) {
		this.mReferenceId = referenceId;
	}

	/**
	 * Returns the version number of the error.
	 * @return The version number.
	 */
	public String getVersion() {
		return mVersion;
	}

	/**
	 * Sets the version number.
	 * @param version version number.
	 */
	public void setVersion(String version) {
		this.mVersion = version;
	}

	/**
	 * Returns the array containing the additional message.
	 * @return
	 */
	public ArrayList<String> getAdditionalMessages() {
		return mAdditionalMessages;
	}

	/**
	 * Sets the additional message.
	 * @param additionalMessages
	 */
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
