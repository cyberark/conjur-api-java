package net.conjur.api;

import net.conjur.util.Args;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses and represents a fully qualified Conjur identifier.
 */
public class ConjurIdentifier {
    private final String account;
    private final String kind;
    private final String id;

    private String stringValue;

    public ConjurIdentifier(String account, String kind, String id){
        this.account = Args.notNull(account, "account");
        this.kind = Args.notNull(kind, "kind");
        this.id = Args.notNull(id, "id");
    }

    public String getAccount(){
        return account;
    }

    public String getKind(){
        return kind;
    }

    public String getId(){
        return id;
    }

    public static ConjurIdentifier parse(String identifier, String account){
        Args.notNull(identifier, "identifier");
        Args.notNull(account, "account");

        String[] tokens = identifier.split(":", 3);
        if(tokens.length < 2){
            throw new IllegalArgumentException("Expected at least 2 tokens in " +
                identifier);
        }

        if(tokens.length == 2){
            return new ConjurIdentifier(account, tokens[0], tokens[1]);
        }else{
            return new ConjurIdentifier(tokens[0], tokens[1], tokens[2]);
        }

    }

    @Override
    public String toString(){
        if(stringValue == null){
            stringValue =  account + ":" + kind + ":" + id;
        }
        return stringValue;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ConjurIdentifier)){
            return false;
        }

        return obj.toString().equals(toString());
    }
}
