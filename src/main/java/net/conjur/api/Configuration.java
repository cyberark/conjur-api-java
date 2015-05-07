package net.conjur.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Conjur configuration, typically loaded from one or more .conjurrc files.
 * @deprecated this is unused and will be replaced in future versions of the api.
 */
public class Configuration {

    private final Map<String, String> map = new HashMap<String, String>();

    /**
     * @deprecated
     * @return
     */
    public static Configuration getDefaultConfiguration(){
       return new Configuration();
    }

    public Configuration(){}

    public Configuration(Map<String, String> values){
        merge(values);
    }

    public String getApplianceUrl(){
        return get("applianceUrl");
    }

    public void setApplianceUrl(String applianceUrl){
        set("applianceUrl", applianceUrl);
    }
    public String getCertPath(){
        return get("certPath");
    }

    public void setCertPath(String certPath){
        set("certPath", certPath);
    }
    public String getAccount(){
        return get("account");
    }

    public void setAccount(String account){
        set("account", account);
    }
    public String getStack(){
        return get("stack");
    }

    public void setStack(String stack){
        set("stack", stack);
    }


    public String get(String key){
        return map.get(key);
    }

    public void set(String key, String value){
        map.put(key, value);
    }

    public void clear(){
        map.clear();
    }

    public Map<String, String> toMap(){
        return Collections.unmodifiableMap(map);
    }


    public void merge(Map<String, ?> values){
        for(Map.Entry<String, ?> e : values.entrySet()){
            if(e.getValue() instanceof String){
                set(e.getKey(), (String) e.getValue());
            }
        }
    }

    public void merge(Configuration source){
        merge(source.toMap());
    }

    public Configuration merged(Configuration source){
        Configuration copy = copy();
        copy.merge(source);
        return copy;
    }

    public Configuration copy(){
        return new Configuration(toMap());
    }
}
