package net.conjur.api.directory;

/**
 *
 */
public interface VariableDelagateMethods {
    void addVariableValue(String variableId, String value);

    String getVariableValue(String variableId);

    String getVariableValue(String variableId, int version);

    Variable getVariable(String id);

}
