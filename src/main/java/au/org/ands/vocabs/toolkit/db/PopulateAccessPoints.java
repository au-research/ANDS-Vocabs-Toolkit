/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.util.List;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import au.org.ands.vocabs.toolkit.db.model.AccessPoints;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Populate the access_points table based on the content of the versions
 * table. This works on the "original" version of the versions table,
 * in which the "data" attribute contains the access points directly. */
public final class PopulateAccessPoints {

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL that is a prefix to all our SPARQL endpoints. */
    private static String sparqlPrefixProperty =
            PROPS.getProperty("SesameImporter.sparqlPrefix") + "/";

    /** URL that is a prefix to Sesame endpoints. */
    private static String sesamePrefixProperty =
            PROPS.getProperty("SesameImporter.serverUrl");

    /** Private constructor for a utility class. */
    private PopulateAccessPoints() {
    }

    /** Get vocabulary by vocabulary id.
     * @param id vocabulary id
     * @return the task
     */
    public static Vocabularies getVocabularyById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Vocabularies v = em.find(Vocabularies.class, id);
        em.close();
        return v;
    }

    /** Get vocabulary by vocabulary id.
     * @return an array of all versions
     */
    public static List<Versions> getAllVersions() {
        EntityManager em = DBContext.getEntityManager();
        Query q = em.createQuery("select v from Versions v");
        @SuppressWarnings("unchecked")
        List<Versions> v = q.getResultList();
        em.close();
        return v;
    }

    /** Save a new access point to the database.
     * @param ap The access point to be saved.
     */
    public static void saveAccessPoint(final AccessPoints ap) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.persist(ap);
        em.getTransaction().commit();
        em.close();
    }

    /** Update an existing access point in the database.
     * @param ap The access point to be update.
     */
    public static void updateAccessPoint(final AccessPoints ap) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.merge(ap);
        em.getTransaction().commit();
        em.close();
    }
    /**
     * Main program.
     * @param args Command-line arguments
     */
    public static void main(final String[] args) {
        // Create prefixes that both end with a slash, so that
        // they can be substituted for each other.
        String sparqlPrefix = sparqlPrefixProperty;
        if (!sparqlPrefix.endsWith("/")) {
            sparqlPrefix += "/";
        }
        String sesamePrefix = sesamePrefixProperty;
        if (!sesamePrefix.endsWith("/")) {
            sesamePrefix += "/";
        }
        sesamePrefix += "repositories/";
        System.out.println("sparqlPrefix: " + sparqlPrefix);
        System.out.println("sesamePrefix: " + sesamePrefix);
        List<Versions> versions = getAllVersions();
        for (Versions version: versions) {
            System.out.println(version.getId());
            System.out.println(version.getTitle());
            String data = version.getData();
            System.out.println(data);
            JsonNode dataJson = TasksUtils.jsonStringToTree(data);
            JsonNode accessPoints = dataJson.get("access_points");
            if (accessPoints != null) {
                System.out.println(accessPoints);
                System.out.println(accessPoints.size());
                for (JsonNode accessPoint: accessPoints) {
                    System.out.println(accessPoint);
                    AccessPoints ap = new AccessPoints();
                    ap.setVersionId(version.getId());
                    String type = accessPoint.get("type").asText();
                    JsonObjectBuilder jobPortal = Json.createObjectBuilder();
                    JsonObjectBuilder jobToolkit = Json.createObjectBuilder();
                    String uri;
                    switch (type) {
                    case "file":
                       ap.setType(type);
                       jobToolkit.add("path", accessPoint.get("uri").asText());
                       ap.setPortalData("");
                       ap.setToolkitData(jobToolkit.build().toString());
                       // Persist what we have ...
                       saveAccessPoint(ap);
                       // ... so that now we can get access to the
                       // ID of the persisted object with ap2.getId().
                       jobPortal.add("uri", "FIXME "
                               + accessPoint.get("uri").asText());
                       jobPortal.add("format",
                               accessPoint.get("format").asText());
                       jobPortal.add("uri",
                               sparqlPrefix.replaceFirst("api/sparql.*",
                                       "api/download/" + ap.getId()));
                       ap.setPortalData(jobPortal.build().toString());
                       updateAccessPoint(ap);
                       break;
                    case "apiSparql":
                        ap.setType(type);
                        uri = accessPoint.get("uri").asText();
                        jobPortal.add("uri", uri);
                        if (uri.startsWith(sparqlPrefix)) {
                            // One of ours, so also add a sesameDownload
                            // endpoint.
                            AccessPoints ap2 = new AccessPoints();
                            ap2.setVersionId(version.getId());
                            ap2.setType("sesameDownload");
                            ap2.setPortalData("");
                            // Persist what we have ...
                            saveAccessPoint(ap2);
                            // ... so that now we can get access to the
                            // ID of the persisted object with ap2.getId().
                            JsonObjectBuilder job2Portal =
                                    Json.createObjectBuilder();
                            JsonObjectBuilder job2Toolkit =
                                    Json.createObjectBuilder();
                            job2Portal.add("uri",
                                    uri.replaceFirst("api/sparql.*",
                                            "api/download/" + ap2.getId()));
                            job2Toolkit.add("uri",
                                    uri.replaceFirst(sparqlPrefix,
                                            sesamePrefix).
                                        replaceFirst("api/sparql",
                                            "api/download"));
                            ap2.setPortalData(job2Portal.build().toString());
                            ap2.setToolkitData(job2Toolkit.build().toString());
                            updateAccessPoint(ap2);
                            jobToolkit.add("source", "local");
                        } else {
                            jobToolkit.add("source", "remote");
                        }
                        ap.setPortalData(jobPortal.build().toString());
                        ap.setToolkitData(jobToolkit.build().toString());
                        saveAccessPoint(ap);
                        break;
                    case "webPage":
                        uri = accessPoint.get("uri").asText();
                        if (uri.endsWith("concept/topConcepts")) {
                            ap.setType("sissvoc");
                            jobToolkit.add("source", "local");
                            jobPortal.add("uri", uri.
                                    replaceFirst("/concept/topConcepts$", ""));
                        } else {
                            ap.setType(type);
                            jobPortal.add("uri", uri);
                        }
                        ap.setPortalData(jobPortal.build().toString());
                        ap.setToolkitData(jobToolkit.build().toString());
                        saveAccessPoint(ap);
                        break;
                    default:
                    }
                    System.out.println("type is: " + ap.getType());
                    System.out.println("portal_data: " + ap.getPortalData());
                    System.out.println("toolkit_data: " + ap.getToolkitData());
                }
            }

        }
    }


}
