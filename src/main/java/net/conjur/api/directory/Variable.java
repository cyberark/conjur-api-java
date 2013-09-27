package net.conjur.api.directory;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Variable {
	private final Directory client;
	
	private final String id;
	private final int versionCount;
	
	
	private Variable(Directory client, String id, int versionCount){
		this.client = client;
		this.id = id;
		this.versionCount = versionCount;
	}
	
	public String getId(){
		return id;
	}
	
	public int getVersionCount(){
		return versionCount;
	}
	
	public String getValue(int version){
		return getValue(version);
	}
	
	public String getValue() throws IOException{
		return getValue(null);
	}
	
	private String getValue(Integer version) throws IOException{
		return version != null ? 
				client.getVariableValue(id, version)
				: client.getVariableValue(id);
	}
	
	public void addValue(String value) throws IOException{
		client.addVariableValue(getId(), value);
	}
	
	public static Variable fromJson(Directory client, String jsonString){
		JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
		String id = json.get("id").getAsString();
		int versionCount = json.get("version_count").getAsInt();
		return new Variable(client, id, versionCount);
	}
}
