/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.db.model.AccessPoint;
import au.org.ands.vocabs.toolkit.db.model.Version;
import au.org.ands.vocabs.toolkit.restlet.Download;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Work with database access points. */
public final class AccessPointUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL that is a prefix to download endpoints. */
    private static String downloadPrefixProperty =
            PROPS.getProperty("Toolkit.downloadPrefix");

    /** Mapping of file extensions to file formats. */
    public static final Hashtable<String, String>
    EXTENSION_TO_FILE_FORMAT_MAP =
    new Hashtable<String, String>();

    /** Mapping of MIME types to file formats. */
    public static final Hashtable<String, String>
    MIMETYPE_TO_FILE_FORMAT_MAP =
    new Hashtable<String, String>();

    // The values should match those in:
    // ANDS-Registry-Core/applications/portal/vocabs/assets/js/versionCtrl.js
    static {
        EXTENSION_TO_FILE_FORMAT_MAP.put("rdf", "RDF/XML");
        EXTENSION_TO_FILE_FORMAT_MAP.put("ttl", "TTL");
        EXTENSION_TO_FILE_FORMAT_MAP.put("nt", "N-Triples");
        EXTENSION_TO_FILE_FORMAT_MAP.put("json", "JSON");
        EXTENSION_TO_FILE_FORMAT_MAP.put("trig", "TriG");
        EXTENSION_TO_FILE_FORMAT_MAP.put("trix", "TriX");
        EXTENSION_TO_FILE_FORMAT_MAP.put("n3", "N3");
        EXTENSION_TO_FILE_FORMAT_MAP.put("csv", "CSV");
        EXTENSION_TO_FILE_FORMAT_MAP.put("tsv", "TSV");
        EXTENSION_TO_FILE_FORMAT_MAP.put("xls", "XLS");
        EXTENSION_TO_FILE_FORMAT_MAP.put("xlsx", "XLSX");
        EXTENSION_TO_FILE_FORMAT_MAP.put("ods", "ODS");
        EXTENSION_TO_FILE_FORMAT_MAP.put("zip", "ZIP");
        EXTENSION_TO_FILE_FORMAT_MAP.put("xml", "XML");
        EXTENSION_TO_FILE_FORMAT_MAP.put("txt", "TXT");
        EXTENSION_TO_FILE_FORMAT_MAP.put("odt", "ODT");
//        EXTENSION_TO_FILE_FORMAT_MAP.put("", "");

//        EXTENSION_TO_FILE_FORMAT_MAP.put("nq", "NQ");
    }

    // The values should match those in:
    // ANDS-Registry-Core/applications/portal/vocabs/assets/js/versionCtrl.js
    static {
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/rdf+xml", "RDF/XML");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/turtle", "TTL");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/plain", "N-Triples");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/json", "JSON");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/x-trig", "TriG");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/trix", "TriX");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/rdf+n3", "N3");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/csv", "CSV");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/csv", "TSV");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/vnd.ms-excel", "XLS");
        MIMETYPE_TO_FILE_FORMAT_MAP.put(
                "application/vnd.openxmlformats-officedocument."
                + "spreadsheetml.sheet", "XLSX");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/x-binary-rdf",
                "BinaryRDF");
        MIMETYPE_TO_FILE_FORMAT_MAP.put(
                "application/vnd.oasis.opendocument.spreadsheet", "ODS");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/zip", "ZIP");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/xml", "XML");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/plain", "TXT");
        MIMETYPE_TO_FILE_FORMAT_MAP.put(
                "application/vnd.oasis.opendocument.text", "ODT");
        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/plain", "TEXT");
//        MIMETYPE_TO_FILE_FORMAT_MAP.put("", "");

//        MIMETYPE_TO_FILE_FORMAT_MAP.put("text/x-nquads", "nq");
//        MIMETYPE_TO_FILE_FORMAT_MAP.put("application/rdf+json", "json");
    }


    /** Private constructor for a utility class. */
    private AccessPointUtils() {
    }

    /** Get all access points.
     * @return A list of AccessPoints
     */
    public static List<AccessPoint> getAllAccessPoints() {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery<AccessPoint> query =
                em.createNamedQuery(AccessPoint.GET_ALL_ACCESSPOINTS,
                        AccessPoint.class);
        List<AccessPoint> aps = query.getResultList();
        em.close();
        return aps;
    }

    /** Get access point by access point id.
     * @param id access point id
     * @return the access point
     */
    public static AccessPoint getAccessPointById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        AccessPoint ap = em.find(AccessPoint.class, id);
        em.close();
        return ap;
    }

    /** Get all access points for a version.
     * @param version The version.
     * @return The list of access points for this version.
     */
    public static List<AccessPoint> getAccessPointsForVersion(
            final Version version) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery<AccessPoint> q = em.createNamedQuery(
                AccessPoint.GET_ACCESSPOINTS_FOR_VERSION,
                AccessPoint.class).
                setParameter(
                        AccessPoint.GET_ACCESSPOINTS_FOR_VERSION_VERSIONID,
                        version.getId());
        List<AccessPoint> aps = q.getResultList();
        em.close();
        return aps;
    }

    /** Get all access points of a certain type for a version.
     * @param version The version.
     * @param type The type of access point to look for.
     * @return The list of access points for this version.
     */
    public static List<AccessPoint> getAccessPointsForVersionAndType(
            final Version version, final String type) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery<AccessPoint> q = em.createNamedQuery(
                AccessPoint.GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE,
                AccessPoint.class).
                setParameter(AccessPoint.
                            GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_VERSIONID,
                            version.getId()).
                setParameter(AccessPoint.
                            GET_ACCESSPOINTS_FOR_VERSION_AND_TYPE_TYPE,
                            type);
        List<AccessPoint> aps = q.getResultList();
        em.close();
        return aps;
    }

    /** Delete all access points of a certain type for a version.
     * @param version The version.
     * @param type The type of access point to look for.
     */
    public static void deleteAccessPointsForVersionAndType(
            final Version version, final String type) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        Query q = em.createNamedQuery(
                AccessPoint.DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE).
                setParameter(AccessPoint.
                        DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_VERSIONID,
                        version.getId()).
                setParameter(AccessPoint.
                        DELETE_ACCESSPOINTS_FOR_VERSION_AND_TYPE_TYPE, type);
        q.executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

    /** Get the portal's format setting for a file access point.
     * @param ap the access point
     * @return the access point's format setting, if it has one,
     * or null otherwise.
     */
    public static String getFormat(final AccessPoint ap) {
        if (!"file".equals(ap.getType())) {
            // Not the right type.
            return null;
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(ap.getPortalData());
        JsonNode format = dataJson.get("format");
        if (format == null) {
            return null;
        }
        return format.asText();
    }

    /** Update the portal's format setting for a file access point.
     * @param ap the access point
     * @param newFormat the access point's new format setting,
     * or null otherwise.
     */
    public static void updateFormat(final AccessPoint ap,
            final String newFormat) {
        if (!"file".equals(ap.getType())) {
            // Not the right type.
            return;
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(ap.getPortalData());
        JsonObjectBuilder jobPortal = Json.createObjectBuilder();
        Iterator<Entry<String, JsonNode>> dataJsonIterator =
                dataJson.fields();
        while (dataJsonIterator.hasNext()) {
            Entry<String, JsonNode> entry = dataJsonIterator.next();
            jobPortal.add(entry.getKey(), entry.getValue().asText());
        }
        jobPortal.add("format", newFormat);
        ap.setPortalData(jobPortal.build().toString());
        updateAccessPoint(ap);
    }

    /** Get the Toolkit's path setting for a file access point.
     * @param ap the access point
     * @return the access point's file setting, if it has one,
     * or null otherwise.
     */
    public static String getToolkitPath(final AccessPoint ap) {
        if (!"file".equals(ap.getType())) {
            // Not the right type.
            return null;
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(ap.getToolkitData());
        JsonNode path = dataJson.get("path");
        if (path == null) {
            return null;
        }
        return path.asText();
    }

    /** Get the portal's uri setting for an apiSparql or sissvoc access point.
     * @param ap the access point
     * @return the access point's portal uri setting, if it has one,
     * or null otherwise.
     */
    public static String getPortalUri(final AccessPoint ap) {
        if (!(AccessPoint.API_SPARQL_TYPE.equals(ap.getType())
                || (AccessPoint.SISSVOC_TYPE.equals(ap.getType())))) {
            // Not the right type.
            return null;
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(ap.getPortalData());
        JsonNode uri = dataJson.get("uri");
        if (uri == null) {
            return null;
        }
        return uri.asText();
    }

    /** Get the Toolkit's uri setting for a sesameDownload access point.
     * @param ap the access point
     * @return the access point's Toolkit uri setting, if it has one,
     * or null otherwise.
     */
    public static String getToolkitUri(final AccessPoint ap) {
        if (!AccessPoint.SESAME_DOWNLOAD_TYPE.equals(ap.getType())) {
            // Not the right type.
            return null;
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(ap.getToolkitData());
        JsonNode uri = dataJson.get("uri");
        if (uri == null) {
            return null;
        }
        return uri.asText();
    }

    /** Save a new access point to the database.
     * @param ap The access point to be saved.
     */
    public static void saveAccessPoint(final AccessPoint ap) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.persist(ap);
        em.getTransaction().commit();
        em.close();
    }

    /** Update an existing access point in the database.
     * @param ap The access point to be update.
     */
    public static void updateAccessPoint(final AccessPoint ap) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.merge(ap);
        em.getTransaction().commit();
        em.close();
    }

    /** Create an access point for a version, for a file. Don't duplicate it,
     * if it already exists.
     * @param version The version for which the access point is to be created.
     * @param format The format of the access point. If null, attempt
     * to deduce a format from the filename.
     * @param targetPath The path to the existing file.
     */
    public static void createFileAccessPoint(final Version version,
            final String format,
            final Path targetPath) {
        String targetPathString;
        try {
            targetPathString = targetPath.toRealPath().toString();
        } catch (IOException e) {
            LOGGER.error("createFileAccessPoint failed calling "
                    + "toRealPath() on file: " + targetPath.toString(), e);
            // Try toAbsolutePath() instead.
            targetPathString = targetPath.toAbsolutePath().toString();
        }
        List<AccessPoint> aps = getAccessPointsForVersionAndType(
                version, AccessPoint.FILE_TYPE);
        for (AccessPoint ap : aps) {
            if (targetPathString.equals(getToolkitPath(ap))) {
                // Already exists. Check the format.
                if (format != null && !format.equals(getFormat(ap))) {
                    // Format changed.
                    updateFormat(ap, format);
                }
                return;
            }
        }
        // No existing access point for this file, so create a new one.
        AccessPoint ap = new AccessPoint();
        ap.setVersionId(version.getId());
        ap.setType(AccessPoint.FILE_TYPE);
        JsonObjectBuilder jobPortal = Json.createObjectBuilder();
        JsonObjectBuilder jobToolkit = Json.createObjectBuilder();
        jobToolkit.add("path", targetPathString);
        // toolkitData is now done.
        ap.setToolkitData(jobToolkit.build().toString());
        ap.setPortalData("");
        // Persist what we have ...
        AccessPointUtils.saveAccessPoint(ap);
        // ... so that now we can get access to the
        // ID of the persisted object with ap.getId().
        String baseFilename = targetPath.getFileName().toString();
        jobPortal.add("uri", downloadPrefixProperty + ap.getId()
                + "/" + baseFilename);
        // Now work on the format. The following is messy. It's really very
        // much for the best if the portal provides the format.
        String deducedFormat;
        if (format == null) {
            // The format was not provided to us, so try to deduce it.
            // First, try the extension.
            String extension = FilenameUtils.getExtension(baseFilename);
            deducedFormat = EXTENSION_TO_FILE_FORMAT_MAP.get(extension);
            if (deducedFormat == null) {
                // No luck with the extension, so try probing.
                try {
                    String mimeType = Files.probeContentType(targetPath);
                    if (mimeType == null) {
                        // Give up.
                        deducedFormat = "Unknown";
                    } else {
                        deducedFormat = MIMETYPE_TO_FILE_FORMAT_MAP
                                .get(mimeType);
                        if (deducedFormat == null) {
                            // Give up.
                            deducedFormat = "Unknown";
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("createFileAccessPoint failed to get "
                            + "MIME type of file: " + targetPathString, e);
                    // Give up.
                    deducedFormat = "Unknown";
                }
            }
        } else {
            // The format was provided to us, so use that. Much easier.
            deducedFormat = format;
        }
        jobPortal.add("format", deducedFormat);
        // portalData is now complete.
        ap.setPortalData(jobPortal.build().toString());
        AccessPointUtils.updateAccessPoint(ap);
    }

    /** Create an access point for a version, for a SPARQL endpoint.
     * Don't duplicate it, if it already exists.
     * @param version The version for which the access point is to be created.
     * @param portalUri The URI to put into the portalData.
     * @param source The source of the endpoint, either SYSTEM_SOURCE
     * or USER_SOURCE.
     */
    public static void createApiSparqlAccessPoint(final Version version,
            final String portalUri,
            final String source) {
        List<AccessPoint> aps = getAccessPointsForVersionAndType(
                version, AccessPoint.API_SPARQL_TYPE);
        for (AccessPoint ap : aps) {
            if (portalUri.equals(getPortalUri(ap))) {
                // Already exists. Don't bother checking the source.
                return;
            }
        }
        // No existing access point for this file, so create a new one.
        AccessPoint ap = new AccessPoint();
        ap.setVersionId(version.getId());
        ap.setType(AccessPoint.API_SPARQL_TYPE);
        JsonObjectBuilder jobPortal = Json.createObjectBuilder();
        JsonObjectBuilder jobToolkit = Json.createObjectBuilder();
        jobPortal.add("uri", portalUri);
        jobPortal.add("source", source);
        ap.setPortalData(jobPortal.build().toString());
        ap.setToolkitData(jobToolkit.build().toString());
        AccessPointUtils.saveAccessPoint(ap);
    }

    /** Create an access point for a version, for a Sesame download.
     * Don't duplicate it, if it already exists.
     * @param version The version for which the access point is to be created.
     * @param toolkitUri The URI to put into the toolkitData.
     */
    public static void createSesameDownloadAccessPoint(
            final Version version,
            final String toolkitUri) {
        List<AccessPoint> aps = getAccessPointsForVersionAndType(
                version, AccessPoint.SESAME_DOWNLOAD_TYPE);
        for (AccessPoint ap : aps) {
            if (toolkitUri.equals(getToolkitUri(ap))) {
                // Already exists.
                return;
            }
        }
        // No existing access point for this file, so create a new one.
        AccessPoint ap = new AccessPoint();
        ap.setVersionId(version.getId());
        ap.setType(AccessPoint.SESAME_DOWNLOAD_TYPE);
        ap.setPortalData("");
        JsonObjectBuilder jobToolkit = Json.createObjectBuilder();
        jobToolkit.add("uri", toolkitUri);
        ap.setToolkitData(jobToolkit.build().toString());
        // Persist what we have ...
        AccessPointUtils.saveAccessPoint(ap);
        // ... so that now we can get access to the
        // ID of the persisted object with ap.getId().
        JsonObjectBuilder jobPortal = Json.createObjectBuilder();
        jobPortal.add("uri",
                downloadPrefixProperty + ap.getId()
                + "/"
                + Download.downloadFilename(ap, ""));
        ap.setPortalData(jobPortal.build().toString());
        AccessPointUtils.updateAccessPoint(ap);
    }

    /** Create a sissvoc access point for a version.
     * Don't duplicate it, if it already exists.
     * @param version The version for which the access point is to be created.
     * @param portalUri The URI to put into the portalData.
     * @param source The source of the endpoint, either SYSTEM_SOURCE
     * or USER_SOURCE.
     */
    public static void createSissvocAccessPoint(final Version version,
            final String portalUri,
            final String source) {
        List<AccessPoint> aps = getAccessPointsForVersionAndType(
                version, AccessPoint.SISSVOC_TYPE);
        for (AccessPoint ap : aps) {
            if (portalUri.equals(getPortalUri(ap))) {
                // Already exists. Don't bother checking the source.
                return;
            }
        }
        // No existing access point for this file, so create a new one.
        AccessPoint ap = new AccessPoint();
        ap.setVersionId(version.getId());
        ap.setType(AccessPoint.SISSVOC_TYPE);
        JsonObjectBuilder jobPortal = Json.createObjectBuilder();
        JsonObjectBuilder jobToolkit = Json.createObjectBuilder();
        jobPortal.add("uri", portalUri);
        jobPortal.add("source", source);
        ap.setPortalData(jobPortal.build().toString());
        ap.setToolkitData(jobToolkit.build().toString());
        AccessPointUtils.saveAccessPoint(ap);
    }

}
