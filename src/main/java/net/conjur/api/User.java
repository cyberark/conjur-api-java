package net.conjur.api;

import net.conjur.api.authn.Authn;
import net.conjur.api.authn.Token;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class User {
	/*
	  {
  "login": "fobarar",
  "userid": "rr3n00-alice",
  "ownerid": "sandbox:user:fobarar",
  "uidnumber": 1167,
  "roleid": "sandbox:user:fobarar",
  "resource_identifier": "sandbox:user:fobarar",
  "api_key": "3fh7awt3jkph3v1513c953d87wqa3g3jyqc3t61wy41bdnst92xvvvg7"
}

	 */
	
	private String login;
	@SerializedName("userid") private String userId;
	@SerializedName("ownerid") private String ownerId;
	@SerializedName("uidnumber") private int uid;
	@SerializedName("roleid") private String roleId;
	@SerializedName("resource_identifier") private String resourceIdentifier;
	@SerializedName("api_key") private String apiKey;
	
	public String getLogin() {
		return login;
	}

	public String getUserId() {
		return userId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public int getUid() {
		return uid;
	}

	public String getRoleId() {
		return roleId;
	}

	public String getResourceIdentifier() {
		return resourceIdentifier;
	}

	public String getApiKey() {
		return apiKey;
	}

	private User() {}

	public static User fromJson(JsonElement json){
		return new Gson().fromJson(json, User.class);
	}
	
	public String toJson(){
		return new Gson().toJson(this);
	}

	public Token authenticate(Authn authn) throws ConjurApiException{
		return authn.authenticate(getLogin(), getApiKey());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [");
		if (getLogin() != null)
			builder.append("getLogin()=").append(getLogin()).append(", ");
		if (getUserId() != null)
			builder.append("getUserId()=").append(getUserId()).append(", ");
		if (getOwnerId() != null)
			builder.append("getOwnerId()=").append(getOwnerId()).append(", ");
		builder.append("getUid()=").append(getUid()).append(", ");
		if (getRoleId() != null)
			builder.append("getRoleId()=").append(getRoleId()).append(", ");
		if (getResourceIdentifier() != null)
			builder.append("getResourceIdentifier()=")
					.append(getResourceIdentifier()).append(", ");
		if (getApiKey() != null)
			builder.append("getApiKey()=").append(getApiKey());
		builder.append("]");
		return builder.toString();
	}
}
