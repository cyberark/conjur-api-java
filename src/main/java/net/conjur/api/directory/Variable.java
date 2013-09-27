package net.conjur.api.directory;

import org.apache.http.util.Args;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

public class Variable {
	private String id;
	private String kind;
	@SerializedName("mime_type") private String mimeType;
	@SerializedName("version_count") private int versionCount;
	@SerializedName("userid") private String userId;
	@SerializedName("resource_identifier") private String resourceIdentifier;
	@SerializedName("ownerid") private String ownerId;
	
	private DirectoryClient client;
	
	public String getId(){
		return id;
	}
	
	public int getVersionCount(){
		return versionCount;
	}
	
	public String getKind() {
		return kind;
	}

	public String getMimeType() {
		return mimeType;
	}
	
	public String getUserId() {
		return userId;
	}

	public String getResourceIdentifier() {
		return resourceIdentifier;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public DirectoryClient getClient() {
		return client;
	}

	public String getValue(int version){
		return getValue(version);
	}
	
	public String getValue(){
		return getValue(null);
	}
	
	private String getValue(Integer version){
		return version != null ? 
				client.getVariableValue(id, version)
				: client.getVariableValue(id);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Variable [id=").append(id).append(", kind=")
				.append(kind).append(", mimeType=").append(mimeType)
				.append(", versionCount=").append(versionCount)
				.append(", userId=").append(userId)
				.append(", resourceIdentifier=").append(resourceIdentifier)
				.append(", ownerId=").append(ownerId).append("]");
		return builder.toString();
	}

	public Variable addValue(String value) {
		client.addVariableValue(getId(), value);
		return refresh();
	}
	
	public Variable refresh(){
		return client.getVariable(getId());
	}
	
	public static Variable fromJson(JsonElement json, DirectoryClient client){
		Variable var = new Gson().fromJson(Args.notNull(json, "Json"), Variable.class);
		var.client = Args.notNull(client, "Client");
		return var;
	}
	
	public static Variable fromJson(String jsonString, DirectoryClient client){
		return fromJson(new JsonParser().parse(Args.notBlank(jsonString, "JsonString")), client);
	}
}
