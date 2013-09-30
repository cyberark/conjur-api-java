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
	
	private VariableDelagateMethods delegate;

    public Variable withDelegate(VariableDelagateMethods delegate){
        this.delegate = Args.notNull(delegate, "delegate");
        return this;
    }

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

	public VariableDelagateMethods getDelegate() {
		return delegate;
	}

	public String getValue(int version){
		return delegate.getVariableValue(getId(),version);
	}
	
	public String getValue(){
		return delegate.getVariableValue(getId());
	}
	
	private String getValue(Integer version){
		return version != null ? 
				delegate.getVariableValue(id, version)
				: delegate.getVariableValue(id);
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
		delegate.addVariableValue(getId(), value);
		return refresh();
	}
	
	public Variable refresh(){
		return delegate.getVariable(getId());
	}
	
	public static Variable fromJson(JsonElement json, VariableDelagateMethods client){
		return new Gson().fromJson(Args.notNull(json, "Json"), Variable.class).withDelegate(client);
	}
	
	public static Variable fromJson(String jsonString, DirectoryClient client){
		return fromJson(new JsonParser().parse(Args.notBlank(jsonString, "JsonString")), client);
	}
}
