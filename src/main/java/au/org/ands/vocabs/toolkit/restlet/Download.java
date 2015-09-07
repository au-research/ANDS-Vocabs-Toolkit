/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.restlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.Hashtable;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.AccessPointsUtils;
import au.org.ands.vocabs.toolkit.db.VersionsUtils;
import au.org.ands.vocabs.toolkit.db.VocabulariesUtils;
import au.org.ands.vocabs.toolkit.db.model.AccessPoints;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

/** Restlet for downloading a vocabulary. */
@Path("download")
public class Download {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());


    /** Mapping of Sesame Download formats to MIME types. */
    public static final Hashtable<String, String>
    SESAME_FORMAT_TO_MIMETYPE_MAP =
    new Hashtable<String, String>();

    // List taken from:
    // http://rdf4j.org/sesame/2.8/docs/system.docbook?view#content-types
    static {
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("rdf", "application/rdf+xml");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("nt", "text/plain");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("ttl", "text/turtle");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("n3", "text/rdf+n3");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("nq", "text/x-nquads");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("json", "application/rdf+json");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("trix", "application/trix");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("trig", "application/x-trig");
        SESAME_FORMAT_TO_MIMETYPE_MAP.put("bin", "application/x-binary-rdf");
    }

    /** Mapping of file formats to MIME types. */
    public static final Hashtable<String, String>
    FILE_FORMAT_TO_MIMETYPE_MAP =
    new Hashtable<String, String>();

    // The keys should match those in:
    // ANDS-Registry-Core/applications/portal/vocabs/assets/js/versionCtrl.js
    static {
        FILE_FORMAT_TO_MIMETYPE_MAP.put("RDF/XML", "application/rdf+xml");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("TTL", "text/turtle");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("N-Triples", "text/plain");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("JSON", "application/json");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("TriG", "application/x-trig");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("TriX", "application/trix");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("N3", "text/rdf+n3");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("CSV", "text/csv");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("TSV", "text/csv");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("XLS", "application/vnd.ms-excel");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("XLSX",
                "application/vnd.openxmlformats-officedocument."
                + "spreadsheetml.sheet");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("BinaryRDF",
                "application/x-binary-rdf");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("ODS",
                "application/vnd.oasis.opendocument.spreadsheet");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("ZIP", "application/zip");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("XML", "application/xml");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("TXT", "text/plain");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("ODT",
                "application/vnd.oasis.opendocument.text");
        FILE_FORMAT_TO_MIMETYPE_MAP.put("TEXT", "text/plain");
//        FILE_FORMAT_TO_MIMETYPE_MAP.put("", "");

//        FILE_FORMAT_TO_MIMETYPE_MAP.put("nq", "text/x-nquads");
//        FILE_FORMAT_TO_MIMETYPE_MAP.put("json", "application/rdf+json");
    }



    /** Get the download for an access point, as RDF/JSON.
     * @param response asynchronous response for this request
     * @param accessPointId access point project id
     * @param downloadFormat the download format
     * xreturn The download for this access point,
     * as returned by Sesame. */
    @Path("{access_point_id}")
    @GET
    public final void download(
            @Suspended final AsyncResponse response,
            @PathParam("access_point_id")
            final int accessPointId,
            @DefaultValue("rdf")
            @QueryParam("format")
            final String downloadFormat) {
        logger.info("Called download: " + accessPointId
                + ", download format: " + downloadFormat);
        // Have a look at the downloadFormat, irrespective of
        // whether it is supported -- which is currently only
        // is for sesameDownload. In future, we _may_ support
        // transforms for file downloads.
        final String mimeType =
                SESAME_FORMAT_TO_MIMETYPE_MAP.get(downloadFormat);
        if (mimeType == null) {
            response.resume(Response.status(Status.NOT_FOUND).
                    entity("Not found: no such format").build());
            return;
        }
        AccessPoints ap = AccessPointsUtils.getAccessPointById(accessPointId);
        if (ap == null) {
            response.resume(Response.status(Status.NOT_FOUND).
                    entity("Not found: no such access point").build());
            return;
        }

        switch (ap.getType()) {
        case "file":
            fileDownload(response, accessPointId, ap);
            break;
        case "sesameDownload":
            sesameDownload(response, accessPointId, ap,
                    downloadFormat, mimeType);
            return;
        default:
            logger.error("download: invalid type for access point: "
                    + ap.getType());
            response.resume(Response.status(Status.NOT_FOUND).
                    entity("Invalid access point type").build());
            return;
        }

    }

    /** Return a file download.
     * @param response The response back to the browser.
     * @param accessPointId The access point id.
     * @param ap The access point.
     */
    private void fileDownload(final AsyncResponse response,
            final int accessPointId, final AccessPoints ap) {
        String format = AccessPointsUtils.getFormat(ap);
        if (format == null) {
            response.resume(Response.status(Status.NOT_FOUND).
                    entity("Not found: no format specified "
                            + "for access point").build());
            return;
        }
        String responseMimeType =
                FILE_FORMAT_TO_MIMETYPE_MAP.get(
                        format);
        if (responseMimeType == null) {
            response.resume(Response.status(Status.NOT_FOUND).
                    entity("Not found: no such format").build());
            return;
        }

        String localPath = AccessPointsUtils.getToolkitPath(ap);
        logger.debug("Getting download from file" + localPath
                + ", MIME type = " + responseMimeType);
        String downloadFilename = Paths.get(localPath).getFileName().toString();

        InputStream fileStream;
        try {
            fileStream = new FileInputStream(
                    new File(localPath));
        } catch (FileNotFoundException e) {
            logger.error("download: file not found: "
                    + localPath, e);
            response.resume(Response.status(Status.NOT_FOUND).
                    entity("File not found").build());
            return;
        }

        response.resume(Response.ok(fileStream).
                        header("Content-Disposition",
                                "attachment; filename="
                        + downloadFilename).
                        header("Content-Type",
                                responseMimeType + ";charset=UTF-8").
                        build());
    }

    /** Return a download from Sesame.
     * @param response The response back to the browser.
     * @param accessPointId The access point id.
     * @param ap The access point.
     * @param downloadFormat The download format.
     * @param mimeType The MIME type of the download.
     */
    private void sesameDownload(final AsyncResponse response,
            final int accessPointId, final AccessPoints ap,
            final String downloadFormat, final String mimeType) {
        String sesameUri = AccessPointsUtils.getToolkitUri(ap);
        logger.debug("Getting download from " + sesameUri
                + ", downloadFormat = " + downloadFormat);

        final String downloadFilename = downloadFilename(ap, downloadFormat);

        // Prepare the connection to Sesame.
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(sesameUri + "/statements");

        final Invocation.Builder invocationBuilder =
                target.request(mimeType);

        // Now go into a separate thread to manage the tunneling.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response sesameResponse = invocationBuilder.get();
                if (sesameResponse.getStatus()
                        >= Response.Status.BAD_REQUEST.getStatusCode()) {
                    logger.error("download from Sesame got an error "
                            + "from Sesame; "
                            + "accessPointId: " + accessPointId);
                    response.resume(Response.status(Status.NOT_FOUND).
                            entity("Not found: no such access point").build());
                    return;
                }

                InputStream sesameResponseStream =
                        sesameResponse.readEntity(InputStream.class);
                response.resume(Response.ok(sesameResponseStream).
                        header("Content-Disposition",
                                "attachment; filename="
                        + downloadFilename).
                        header("Content-Type",
                                mimeType + ";charset=UTF-8").
                        build());
            } }).start();
    }

    /** Generate the filename to use for the download.
     * @param ap The access point
     * @param downloadFormat The download format
     * @return The generated filename.
     */
    private String downloadFilename(final AccessPoints ap,
            final String downloadFormat) {
        // Work out the filename that the download should have.
        Versions version = VersionsUtils.getVersionById(ap.getVersionId());
        Vocabularies vocabulary =
                VocabulariesUtils.getVocabularyById(version.getVocabId());
        final String downloadFilename =
                ToolkitFileUtils.makeSlug(vocabulary.getOwner())
                + "_"
                + vocabulary.getSlug()
                + "_"
                + ToolkitFileUtils.makeSlug(version.getTitle())
                + "." + downloadFormat;
        return downloadFilename;
    }

}
