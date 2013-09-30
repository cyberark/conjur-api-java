package net.conjur.api.integration;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 *
 */
public class BaseSteps {
    Scenario currentScenario;
    String namespace;
    Throwable failure;

    public static boolean debugOutput = true;

    public void debug(String msg, Object...args){
        if(debugOutput){
            msg = String.format(msg, args);
            msg = String.format("[DEBUG] %s", msg);
            info(msg);
        }
    }

    public String namespaced(String value){
        return value == null ? value :
                value.startsWith(namespace + ":") ? value :
                        namespace + ":" + value;
    }
    public void before(Scenario scenario){
        currentScenario = scenario;
        namespace = World.getAdminClient().createUniqueId();
    }

    public void after(){
        currentScenario = null;
    }

    public void info(String msg, Object...args){
        msg = String.format(msg, args);
        if(currentScenario == null){
            System.err.format("[No Scenario] %s", msg);
        }else{
            currentScenario.write(msg);
        }
    }
}
