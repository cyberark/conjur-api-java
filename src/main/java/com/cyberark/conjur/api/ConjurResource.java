package com.cyberark.conjur.api;

import java.util.List;
import java.util.Map;

/**
 * Represents a Conjur resource returned by the
 * <a href="https://docs.cyberark.com/conjur-open-source/latest/en/content/developer/conjur_api_list_resources.htm">List Resources</a> API.
 *
 * <p>A resource has an {@code id} in the form {@code {account}:{kind}:{identifier}},
 * along with metadata such as owner, policy, creation time, permissions, and annotations.</p>
 */
public class ConjurResource {

    private String created_at;
    private String id;
    private String owner;
    private String policy;
    private List<Permission> permissions;
    private List<Annotation> annotations;
    private List<Map<String, Object>> secrets;
    private List<Map<String, Object>> policy_versions;

    /** @return the creation timestamp */
    public String getCreatedAt() { return created_at; }

    /**
     * The fully-qualified resource ID: {@code {account}:{kind}:{identifier}}
     * @return the resource ID
     */
    public String getId() { return id; }

    /** @return the owner resource ID */
    public String getOwner() { return owner; }

    /** @return the policy resource ID this resource belongs to */
    public String getPolicy() { return policy; }

    /** @return the list of permissions on this resource */
    public List<Permission> getPermissions() { return permissions; }

    /** @return the list of annotations on this resource */
    public List<Annotation> getAnnotations() { return annotations; }

    /** @return secret version info (only for variables) */
    public List<Map<String, Object>> getSecrets() { return secrets; }

    /** @return policy version info (only for policies) */
    public List<Map<String, Object>> getPolicyVersions() { return policy_versions; }

    /**
     * Extract the resource kind from the fully-qualified ID.
     * For {@code myorg:variable:db/password}, returns {@code variable}.
     *
     * @return the kind portion of the ID, or null if the ID is malformed
     */
    public String getKind() {
        if (id == null) return null;
        int first = id.indexOf(':');
        int second = id.indexOf(':', first + 1);
        if (first < 0 || second < 0) return null;
        return id.substring(first + 1, second);
    }

    /**
     * Extract the identifier (without account and kind prefix) from the fully-qualified ID.
     * For {@code myorg:variable:db/password}, returns {@code db/password}.
     *
     * @return the identifier portion of the ID, or null if the ID is malformed
     */
    public String getIdentifier() {
        if (id == null) return null;
        int first = id.indexOf(':');
        int second = id.indexOf(':', first + 1);
        if (first < 0 || second < 0) return null;
        return id.substring(second + 1);
    }

    @Override
    public String toString() {
        return "ConjurResource{id='" + id + "'}";
    }

    /**
     * Represents a permission entry on a resource.
     */
    public static class Permission {
        private String privilege;
        private String role;
        private String policy;

        public String getPrivilege() { return privilege; }
        public String getRole() { return role; }
        public String getPolicy() { return policy; }

        @Override
        public String toString() {
            return "Permission{privilege='" + privilege + "', role='" + role + "'}";
        }
    }

    /**
     * Represents an annotation (key-value metadata) on a resource.
     */
    public static class Annotation {
        private String name;
        private String value;
        private String policy;

        public String getName() { return name; }
        public String getValue() { return value; }
        public String getPolicy() { return policy; }

        @Override
        public String toString() {
            return "Annotation{name='" + name + "', value='" + value + "'}";
        }
    }
}
