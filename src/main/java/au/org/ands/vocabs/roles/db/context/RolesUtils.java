/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.roles.db.context;

import javax.persistence.EntityManager;

import au.org.ands.vocabs.roles.db.entity.Roles;

/** Work with database roles. */
public final class RolesUtils {

    /** Private constructor for a utility class. */
    private RolesUtils() {
    }

    /** Get role by id.
     * @param id The id.
     * @return The role.
     */
    public static Roles getRolesById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Roles v = em.find(Roles.class, id);
        em.close();
        return v;
    }

    /** Get role by role id.
     * @param roleId role id
     * @return the role
     */
    public static Roles getRolesByRoleId(final String roleId) {
        EntityManager em = DBContext.getEntityManager();
        Roles v = em.find(Roles.class, roleId);
        em.close();
        return v;
    }


}
