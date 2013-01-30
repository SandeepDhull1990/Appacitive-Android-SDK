package com.appacitive.android.model;

import java.util.HashMap;

import com.google.gson.Gson;

/**
 * The appacitive user detail. This class contains the user information
 * necessary to create new user.
 * 
 * @author Sandeep Dhull
 * 
 */
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

	String createRequestParams() {
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

	public void setUserName(String userName) {
		this.mUserName = userName;
	}

	public void setPassword(String password) {
		this.mPassword = password;
	}

	public void setBirthDate(String birthDate) {
		this.mBirthDate = birthDate;
	}

	public void setFirstName(String firstName) {
		this.mFirstName = firstName;
	}

	public void setLastName(String lastName) {
		this.mLastName = lastName;
	}

	public void setEmail(String email) {
		this.mEmail = email;
	}

	public void setSecretQuestion(String secretQuestion) {
		this.mSecretQuestion = secretQuestion;
	}

	public void setSecretAnswer(String secretAnswer) {
		this.mSecretAnswer = secretAnswer;
	}

	public void setPhone(String phone) {
		this.mPhone = phone;
	}

}
