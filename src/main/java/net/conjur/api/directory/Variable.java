package net.conjur.api.directory;

import org.apache.http.util.Args;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Variable {
	private String id;
	private String kind;
	private String mimeType;
	private int versionCount;
	

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
