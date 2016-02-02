package net.conjur.api;

import net.conjur.util.Args;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;

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

    public void create(HasRole owner){
        // TODO
        throw new NotImplementedException();
    }

    public boolean exists(){
        try{
            resolveRoleTarget().request().get();
        }catch(NotFoundException e){
            return false;
        }catch(ForbiddenException e){
            // role exists, but we can't see its details.
            return true;
        }
        return true; // everything's cool
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
