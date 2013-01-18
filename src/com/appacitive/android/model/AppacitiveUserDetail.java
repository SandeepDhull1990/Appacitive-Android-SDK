package com.appacitive.android.model;

import java.util.HashMap;

import com.google.gson.Gson;

public class AppacitiveUserDetail {

	private String mUserName;
	private String mPassword;
	private String mBirthDate;
	private String mFirstName;
	private String mLastName;
	private String mEmail;
	private String mSecretQuestion;
	private String mSecretAnswer;
	private String mPhone;

	public String createRequestParams() {
		HashMap<String, String> userDetails = new HashMap<String, String>();
		userDetails.put("__schematype", "user");
		if (mUserName != null) {
			userDetails.put("username", this.mUserName);
		}
		if (mPassword != null) {
			userDetails.put("password", this.mPassword);
		}
		if (mFirstName != null) {
			userDetails.put("firstname", this.mFirstName);
		}
		if (mEmail != null) {
			userDetails.put("email", this.mEmail);
		}
		if (mLastName != null) {
			userDetails.put("lastname", this.mLastName);
		}
		if (mBirthDate != null) {
			userDetails.put("birthdate", this.mBirthDate);
		}
		if (mSecretQuestion != null) {
			userDetails.put("secretquestion", this.mSecretQuestion);
		}
		if (mSecretAnswer != null) {
			userDetails.put("secretanswer", this.mSecretAnswer);
		}
		if (mPhone != null) {
			userDetails.put("phone", this.mPhone);
		}
		Gson gson = new Gson();
		String jsonObject = gson.toJson(userDetails);
		return jsonObject;
	}

	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String userName) {
		this.mUserName = userName;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public String getBirthDate() {
		return mBirthDate;
	}

	public void setBirthDate(String birthDate) {
		this.mBirthDate = birthDate;
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setFirstName(String firstName) {
		this.mFirstName = firstName;
	}

	public String getLastName() {
		return mLastName;
	}

	public void setLastName(String lastName) {
		this.mLastName = lastName;
	}

	public String getEmail() {
		return mEmail;
	}

	public void setEmail(String email) {
		this.mEmail = email;
	}

	public String getSecretQuestion() {
		return mSecretQuestion;
	}

	public void setSecretQuestion(String secretQuestion) {
		this.mSecretQuestion = secretQuestion;
	}

	public String getSecretAnswer() {
		return mSecretAnswer;
	}

	public void setSecretAnswer(String secretAnswer) {
		this.mSecretAnswer = secretAnswer;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String phone) {
		this.mPhone = phone;
	}

}
