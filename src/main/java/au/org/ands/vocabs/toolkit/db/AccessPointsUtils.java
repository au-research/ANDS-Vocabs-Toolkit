/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import javax.persistence.EntityManager;

import au.org.ands.vocabs.toolkit.db.model.AccessPoints;

import com.fasterxml.jackson.databind.JsonNode;

/** Work with database access points. */
public final class AccessPointsUtils {

    /** Private constructor for a utility class. */
    private AccessPointsUtils() {
    }

    /** Get access point by access point id.
     * @param id access point id
     * @return the access point
     */
    public static AccessPoints getAccessPointById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        AccessPoints ap = em.find(AccessPoints.class, id);
        em.close();
        return ap;
    }

    /** Get the portal's format setting for a file access point.
     * @param ap the access point
     * @return the access point's format setting, if it has one,
     * or null otherwise.
     */
    public static String getFormat(final AccessPoints ap) {
        if (!"file".equals(ap.getType())) {
            // Not the right type.
            return null;
        }
        JsonNode dataJson = TasksUtils.jsonStringToTree(ap.getPortalData());
        JsonNode format = dataJson.get("format");
        if (format == null) {
            return null;
        }
        return format.asText();
    }

    /** Get the Toolkit's path setting for a file access point.
     * @param ap the access point
     * @return the access point's file setting, if it has one,
     * or null otherwise.
     */
    public static String getToolkitPath(final AccessPoints ap) {
        if (!"file".equals(ap.getType())) {
            // Not the right type.
            return null;
        }
        JsonNode dataJson = TasksUtils.jsonStringToTree(ap.getToolkitData());
        JsonNode path = dataJson.get("path");
        if (path == null) {
            return null;
        }
        return path.asText();
    }

    /** Get the Toolkit's uri setting for a sesameDownload access point.
     * @param ap the access point
     * @return the access point's Toolkit uri setting, if it has one,
     * or null otherwise.
     */
    public static String getToolkitUri(final AccessPoints ap) {
        if (!"sesameDownload".equals(ap.getType())) {
            // Not the right type.
            return null;
        }
        JsonNode dataJson = TasksUtils.jsonStringToTree(ap.getToolkitData());
        JsonNode uri = dataJson.get("uri");
        if (uri == null) {
            return null;
        }
        return uri.asText();
    }

}
