/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.roles;

import au.org.ands.vocabs.roles.db.entity.RoleTypeId;

/** Representation of one role. */
public class Role {

    /** id. */
    private String id;
    /** typeId. */
    private RoleTypeId typeId;
    /** fullName. */
    private String fullName;

    /** Constructor that takes all properties.
     * @param anId The value of id.
     * @param aTypeId The value of typeId.
     * @param aFullName The value of fullName.
     */
    public Role(final String anId, final RoleTypeId aTypeId,
            final String aFullName) {
        id = anId;
        typeId = aTypeId;
        fullName = aFullName;
    }

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

    /** Get the value of typeId.
     * @return The value of typeId.
     */
    public RoleTypeId getTypeId() {
        return this.typeId;
    }

    /** Set the value of typeId.
     * @param aTypeId The value of typeId
     *      to set.
     */
    public void setTypeId(
            final RoleTypeId aTypeId) {
        typeId = aTypeId;
    }

    /** Get the value of fullName.
     * @return The value of fullName.
     */
    public String getFullName() {
        return this.fullName;
    }

    /** Set the value of fullName.
     * @param aFullName The value of fullName
     *      to set.
     */
    public void setFullName(
            final String aFullName) {
        fullName = aFullName;
    }

}
