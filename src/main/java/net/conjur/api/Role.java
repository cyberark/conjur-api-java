package net.conjur.api;

import net.conjur.util.Args;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import static net.conjur.util.EncodeUriComponent.encodeUriComponent;

/**
 * This class represents a Conjur role.  A {@code Role} instance can be obtained from an id through the {@code getRole}
 * method of the {@code Authorization} service.
 *
 * When initialized, a role may or may not exist.  This can be checked with the {@code exists()} method.
 *
 * If the role does not exist, it can be created by  calling the {@code create(String)} methods.
 *
 * You can call the createIfNotFound(HasRole) to create a role.
 *
 *
 */
public class Role extends RestResource implements HasRole {

    private final ConjurIdentifier roleId;
    private WebTarget roleTarget;

    Role(RestResource relative, ConjurIdentifier roleId){
        super(relative);
        this.roleId = Args.notNull(roleId);

        initializeTargets();
    }

    public ConjurIdentifier getRoleId(){
        return roleId;
    }

    public Role getRole(){
        return this;
    }

    public boolean isPermitted(HasResource resource, String privilege){
        return isPermitted(resource.getResource().getResourceId(), privilege);
    }

    public boolean isPermitted(String resourceId, String privilege){
        return isPermitted(ConjurIdentifier.parse(resourceId, getAccount()),
                privilege);
    }

    public boolean isPermitted(ConjurIdentifier resourceId, String privilege){
        try{
            resolveRoleTarget()
                    .queryParam("check", "true")
                    .queryParam("resource_id", resourceId.toString())
                    .queryParam("privilege", privilege)
                    .request()
                    .get(String.class);
        }catch (ForbiddenException e){
            return false;
        }catch(NotFoundException e){
            return false;
        }

        return true;
    }

    public void createIfNotExists(HasRole actingAs){
        if(!exists()){
            create(actingAs);
        }
    }

    public void createIfNotExists(){
        createIfNotExists(null);
    }

    public void create(){
        create(null);
    }

    public void create(HasRole actingAs){


        WebTarget target = resolveRoleTarget();

        if(actingAs != null){
            target = target.queryParam("acting_as", encodeUriComponent(actingAs.getRole().getRoleId().toString()));
        }

        target.request().put(Entity.entity("","text/plain"), String.class);
    }

    public boolean exists(){
        return checkExists(resolveRoleTarget());
    }

    public void grantTo(HasRole otherRole){
        resolveMembersTarget(otherRole, null)
                .request()
                .put(Entity.entity("", "text/plain"), String.class);
    }

    public void grantTo(HasRole otherRole, boolean adminOption){
        resolveMembersTarget(otherRole, adminOption)
                .request()
                .put(Entity.entity("", "text/plain"), String.class);
    }

    public void revokeFrom(HasRole otherRole, boolean adminOption){
        resolveMembersTarget(otherRole, adminOption).request().delete(String.class);
    }

    public void revokeFrom(HasRole otherRole){
        resolveMembersTarget(otherRole, null).request().delete(String.class);
    }

    /**
     * Retrieve all roles of which this role is a member.  The list includes this role for convenience.
     *
     * If a role A is a member of role B, role A can do everything that role B can do.
     * @return The roles of which this role is a member.
     */
    public Collection<Role> getMemberships(){
        final WebTarget target = resolveRoleTarget().queryParam("all", "true");
        final String[] roleIds = target.request(MediaType.APPLICATION_JSON_TYPE).get(String[].class);
        final Collection<Role> roles = new ArrayList<Role>(roleIds.length);
        for(String roleId : roleIds){
            roles.add(new Role(this, ConjurIdentifier.parse(roleId, getAccount())));
        }
        return roles;
    }

    private WebTarget resolveRoleTarget(){
        return roleTarget.resolveTemplate("account", getAccount());
    }

    // gives us the target for revoking and granting
    private WebTarget resolveMembersTarget(HasRole member, Boolean adminOption){
        String memberId = member.getRole().getRoleId().toString();
        WebTarget result = resolveRoleTarget()
                .queryParam("members","true")
                .queryParam("member", encodeUriComponent(memberId));
        if(adminOption != null) {
            return result.queryParam("admin_option", adminOption ? "true" : "false");
        }else{
            return result;
        }
    }

    private void initializeTargets(){
        roleTarget = target(getEndpoints().getAuthzUri())
                .path("{account}") // don't call getAccount from ctor to avoid insane shit
                .path("roles")
                .path(getRoleId().getKind())
                .path(getRoleId().getId());
    }

    @Override
    public String toString() {
        return "[Role " + getRoleId().toString() + "]";
    }

    @Override
    public int hashCode() {
        return getRoleId().hashCode() * 31; // garbage, but w.e
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof HasRole)
            return getRoleId().equals(((HasRole)obj).getRole().getRoleId());
        return false;
    }
}
