package net.conjur.api.directory;

import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.Token;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class User {
	private String login;
	private String password;
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

	public String getPassword(){
		return password;
	}
	
	private User() {}

	public static User fromJson(String jsonString){
		return new Gson().fromJson(jsonString, User.class);
	}
	
	public static User fromJson(JsonElement json){
		return new Gson().fromJson(json, User.class);
	}

	public Token authenticate(AuthnClient authn){
		return authn.authenticate(this);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("User [login=").append(login).append(", password=")
				.append(password).append(", userId=").append(userId)
				.append(", ownerId=").append(ownerId).append(", uid=")
				.append(uid).append(", roleId=").append(roleId)
				.append(", resourceIdentifier=").append(resourceIdentifier)
				.append(", apiKey=").append(apiKey).append("]");
		return builder.toString();
	}

	
}
