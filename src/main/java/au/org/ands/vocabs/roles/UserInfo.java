/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.roles;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import au.org.ands.vocabs.roles.db.entity.AuthenticationServiceId;

/** Representation of the currently-logged in user, with their
 * roles.
 */
@XmlRootElement
public class UserInfo {

    /** id. */
    private String id;
    /** authenticationServiceId. */
    private AuthenticationServiceId authenticationServiceId;
    /** name. */
    private String fullName;
    /** parentRoles. */
    private Set<Role> parentRoles;
    /** isRegistrySuperUser. */
    private Boolean isRegistrySuperUser;

    /** Get the value of id.
     * @return The value of id.
     */
    public String getId() {
        return this.id;
    }

    /** Set the value of id.
     * @param anId The value of id
     *      to set.
     */
    public void setId(
            final String anId) {
        id = anId;
    }

    /** Get the value of authenticationServiceId.
     * @return The value of authenticationServiceId.
     */
    public AuthenticationServiceId getAuthenticationServiceId() {
        return this.authenticationServiceId;
    }

    /** Set the value of authenticationServiceId.
     * @param aAuthenticationServiceId The value of authenticationServiceId
     *      to set.
     */
    public void setAuthenticationServiceId(
            final AuthenticationServiceId aAuthenticationServiceId) {
        authenticationServiceId = aAuthenticationServiceId;
    }

    /** Get the value of fullName.
     * @return The value of fullName.
     */
    public String getFullName() {
        return this.fullName;
    }

    /** Set the value of fullName.
     * @param aFullName The value of fullName to set.
     */
    public void setFullName(final String aFullName) {
        fullName = aFullName;
    }

    /** Get the value of parentRoles.
     * @return The value of parentRoles.
     */
    @XmlElementWrapper(name = "parentRoles")
    @XmlElement(name = "role")
    public Set<Role> getParentRoles() {
        return this.parentRoles;
    }

    /** Set the value of parentRoles.
     * @param aParentRoles The value of roles to set.
     */
    public void setParentRoles(final Set<Role> aParentRoles) {
        parentRoles = aParentRoles;
    }

    /** Get the value of isRegistrySuperUser.
     * @return The value of isRegistrySuperUser.
     */
    public Boolean getIsSuperUser() {
        return this.isRegistrySuperUser;
    }

    /** Set the value of isRegistrySuperUser.
     * @param anIsRegistrySuperUser The value of isRegistrySuperUser to set.
     */
    public void setIsSuperUser(final Boolean anIsRegistrySuperUser) {
        isRegistrySuperUser = anIsRegistrySuperUser;
    }


}
