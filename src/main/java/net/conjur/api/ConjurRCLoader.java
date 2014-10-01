package net.conjur.api;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;



/**
 * Loads a Configuration instance from several .conjurrc files.
 *
 * By default, a system wide configuration file is read from /etc/conjur.conf,
 * followed by a user defined configuration file which is  read from either $CONJURRC
 * or $HOME/.conjurrc (although the later is deprecated), and a .conjurrc file from
 * the current working directory.
 */
public class ConjurRCLoader {
    private boolean useSystem = true;
    private boolean useEnvironment = true;
    private boolean useLocal = true;
    private final List<String> additionalFiles = new ArrayList<String>();

    public ConjurRCLoader(){}

    public Configuration load(){
        Configuration config = new Configuration();
        for(String path : getConfigFiles()){
            config.merge(loadFile(path));
        }
        return config;
    }

    private Map<String, Object> loadFile(String path){
        try {
            YamlReader reader = new YamlReader(new FileReader(path));
            Object contents = reader.read();

            if(contents instanceof Map){
                return normalizeKeys((Map) contents);
            }
            return Collections.emptyMap();
        }catch(IOException e){
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> normalizeKeys(Map<String, Object> map){
        final Map<String, Object> result =  new HashMap<String, Object>(map.size());
        for(Map.Entry<String, Object> e : map.entrySet()){
            result.put(normalizeKey(e.getKey()), e.getValue());
        }
        return result;
    }

    private String normalizeKey(String key){
        // convert snake_case to camelCase
        String[] words= key.split("_");
        StringBuilder sb = new StringBuilder(words[0]);
        for(int i=1; i < words.length; i++){
            sb.append(Character.toUpperCase(words[i].charAt(0)))
                    .append(words[i].substring(1));
        }
        return sb.toString();
    }

    private String[] getConfigFiles(){
        final List<String> files = new ArrayList<String>();

        if(getUseSystem()) files.add(getSystemConfigFile());
        if(getUseEnvironment()) files.add(getUserConfigFile());
        if(getUseLocal()) files.add(getLocalConfigFile());
        files.addAll(additionalFiles);


        files.removeAll(Collections.singleton(null));
        return files.toArray(new String[files.size()]);
    }


    public boolean getUseSystem() {
        return useSystem;
    }

    public ConjurRCLoader setUseSystem(boolean useSystem) {
        this.useSystem = useSystem;
        return this;
    }

    public boolean getUseEnvironment() {
        return useEnvironment;
    }

    public ConjurRCLoader setUseEnvironment(boolean useEnvironment) {
        this.useEnvironment = useEnvironment;
        return this;
    }

    public boolean getUseLocal() {
        return useLocal;
    }

    public ConjurRCLoader setUseLocal(boolean useLocal) {
        this.useLocal = useLocal;
        return this;
    }

    public List<String> getAdditionalFiles() {
        return additionalFiles;
    }

    public ConjurRCLoader addFiles(Collection<String> files){
        additionalFiles.addAll(files);
        return this;
    }

    private String getSystemConfigFile(){
        return findFile("/etc/conjur.conf");
    }

    private String getUserConfigFile(){
        String conjurrc = System.getenv("CONJURRC");
        if(conjurrc == null){
            System.err.println("No CONJURRC environment variable found, using $HOME/.conjurrc instead (this behavior is deprecated)");
            String home = System.getenv("HOME");
            if(home != null){
                conjurrc = new File(new File(home), ".conjurrc").getAbsolutePath();
            }else{
                return null;
            }
        }
        return findFile(conjurrc);
    }

    private String getLocalConfigFile(){
        // This is not the preferred way to get the cwd in Java 7 (that would be by using the NIO Paths class),
        // but I think we still support Java 6, so we'll do it the old way.
        final String cwd = System.getProperty("user.dir");
        if(cwd != null){
            return findFile(new File(cwd, ".conjurrc").getAbsolutePath());
        }
        return null;
    }

    private String findFile(String path){
        File file = new File(path);
        if(!file.exists() || !file.isFile() || !file.canRead()){
            return null;
        }
        return file.getAbsolutePath();
    }
}
