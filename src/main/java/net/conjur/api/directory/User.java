package net.conjur.api.directory;

import org.apache.http.util.Args;

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
	
	/**
	 * @return The login identifying this user.
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @return The login of the user who created this user.
	 * TODO Is this only present after creation?
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @return The full asset id for the role that owns this user.  Typically
	 * this is the user itself.
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * @return A numeric unique id for this user.  You won't normally use this.
	 */
	public int getUid() {
		return uid;
	}

	/**
	 * @return The full role id for this user. You won't normally use this.
	 */
	public String getRoleId() {
		return roleId;
	}

	/**
	 * @return The resource/asset identifier for this user. You won't normally use this.
	 */
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}

	/**
	 * @return The user's api key.  Only included when the user is returned from 
	 * 	{@link DirectoryClient#createUser(String, String)}
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @return The user's password.  Only included when  the user is returned after creation
	 * and a password was given when creating the user.
	 */
	public String getPassword(){
		return password;
	}
	
	private User() {}

	/**
	 * Parse a jsonString and return a User object representing it.
	 * @param jsonString Must be non-blank.
	 * @return
	 */
	public static User fromJson(String jsonString){
		return new Gson().fromJson(Args.notBlank(jsonString, "JsonString"), User.class);
	}
	
	/**
	 * Initialize a user from a JsonElement.
	 * @param json Must be non-null and a JsonObject
	 * @return
	 */
	public static User fromJson(JsonElement json){
		Args.notNull(json, "Json");
		if(!(json.isJsonObject())){
			throw new IllegalArgumentException("Json must be a JsonObject (was a " + json.getClass().getName() + ")");
		}
		return new Gson().fromJson(json, User.class);
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
