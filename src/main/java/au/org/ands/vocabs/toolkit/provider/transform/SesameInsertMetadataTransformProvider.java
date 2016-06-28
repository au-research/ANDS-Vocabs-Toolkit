/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;

import java.util.HashMap;
import java.util.Properties;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.db.model.Version;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/**
 * Transform provider for inserting (version) metadata into a Sesame repository.
 * In case we need to do a transform provider that operates on "raw" RDF files,
 * see
 * https://groups.google.com/d/msg/sesame-users/fJctKX_vNEs/a1gm7rqD3L0J for how
 * to do it.
 */
public class SesameInsertMetadataTransformProvider extends TransformProvider {

    // Not needed yet.
//    /** Logger for this class. */
//    private final Logger logger = LoggerFactory.getLogger(
//            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    @Override
    public final String getInfo() {
        // Not implemented.
        return null;
    }

    /** Update to insert dcterms:issued metadata. Removes any existing
     * triples of this format. */
    private static final String INSERT_DCTERMS_ISSUED_METADATA_UPDATE =
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX dcterms: <http://purl.org/dc/terms/>\n"
            + "DELETE {\n"
            + "  ?scheme dcterms:issued ?oldIssuedDate\n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme .\n"
            + "  ?scheme dcterms:issued ?oldIssuedDate\n"
            + "} ;\n"
            + "INSERT {\n"
            + "  ?scheme dcterms:issued ?issuedDate\n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme\n"
            + "}";

    /** Update to insert owl:versionInfo metadata. Removes any existing
     * triples of this format. */
    private static final String INSERT_OWL_VERSIONINFO_METADATA_UPDATE =
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
            + "DELETE {\n"
            + "  ?scheme owl:versionInfo ?oldVersionTitle\n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme .\n"
            + "  ?scheme owl:versionInfo ?oldVersionTitle\n"
            + "} ;\n"
            + "INSERT {\n"
            + "  ?scheme owl:versionInfo ?versionTitle .\n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme\n"
            + "}";

    /* * Update to insert adms:status metadata. Removes any existing
     * triples of this format. */
    /* Uncomment when needed.
    private static final String INSERT_ADMS_STATUS_METADATA_UPDATE =
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX adms: <http://www.w3.org/ns/adms#>\n"
            + "DELETE {\n"
            + "  ?scheme adms:status ?oldVersionStatus \n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme .\n"
            + "  ?scheme adms:status ?oldVersionStatus \n"
            + "} ;\n"
            + "INSERT {\n"
            + "  ?scheme adms:status ?versionStatus \n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme\n"
            + "}";
            */

    /** Update to remove (version) metadata. */
    private static final String DELETE_METADATA_UPDATE =
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
            + "PREFIX dcterms: <http://purl.org/dc/terms/>\n"
            + "PREFIX adms: <http://www.w3.org/ns/adms#>\n"
            + "DELETE {\n"
            + "  ?scheme dcterms:issued ?oldIssuedDate\n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme .\n"
            + "  ?scheme dcterms:issued ?oldIssuedDate\n"
            + "} ;\n"
            + "DELETE {\n"
            + "  ?scheme owl:versionInfo ?oldVersionTitle\n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme .\n"
            + "  ?scheme owl:versionInfo ?oldVersionTitle\n"
            + "} ;\n"
            + "DELETE {\n"
            + "  ?scheme adms:status ?oldVersionStatus \n"
            + "} WHERE {\n"
            + "  ?scheme a skos:ConceptScheme .\n"
            + "  ?scheme adms:status ?oldVersionStatus \n"
            + "}";

    /** Map of our own version status indicators to the PURLs used
     * by ADMS 1.0. */
    private static HashMap<String, String> admsStatusMap =
            new HashMap<String, String>();

    static {
        admsStatusMap.put("current",
                "http://purl.org/adms/status/Completed");
        admsStatusMap.put("superseded",
                "http://purl.org/adms/status/Withdrawn");
        admsStatusMap.put("deprecated",
                "http://purl.org/adms/status/Deprecated");
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        boolean result = true;
        // Get the metadata values to be inserted.
        Version version = taskInfo.getVersion();

        // Use the release date as it is. As it may be
        // YYYY, YYYY-MM, or YYYY-MM-DD, can't use a date
        // formatter.
        String issuedDate = version.getReleaseDate();

        String versionTitle = version.getTitle();

        // Construct bindings for SPARQL Update.
        ValueFactory factory = ValueFactoryImpl.getInstance();
        HashMap<String, Value> bindings = new HashMap<String, Value>();

        if (issuedDate != null) {
            bindings.put("issuedDate", factory.createLiteral(issuedDate));
            result = SesameTransformUtils.runUpdate(taskInfo, subtask, results,
                    INSERT_DCTERMS_ISSUED_METADATA_UPDATE, bindings);
            if (!result) {
                // Failure applying the Update. Stop here.
                return false;
            }
        }

        if (versionTitle != null) {
            // Reset bindings and apply the version title Update.
            bindings.clear();
            bindings.put("versionTitle", factory.createLiteral(versionTitle));
            result = SesameTransformUtils.runUpdate(taskInfo, subtask, results,
                    INSERT_OWL_VERSIONINFO_METADATA_UPDATE, bindings);
            if (!result) {
                // Failure applying the Update. Stop here.
                return false;
            }
        }

        /* Future work: Add ADMS status. The problem is, that the
         * publication workflow is not so great, so metadata injection
         * doesn't happen when the status changes. So once set, always
         * set with the same value. If/when publication workflow is
         * improved, uncomment this.
         */
        /*
        String versionStatus = admsStatusMap.get(version.getStatus());
        if (versionStatus != null) {
            // Reset bindings and apply the ADMS status Update.
            bindings.clear();
            bindings.put("versionStatus", factory.createURI(versionStatus));
            return SesameTransformUtils.runUpdate(taskInfo, subtask, results,
                    INSERT_ADMS_STATUS_METADATA_UPDATE, bindings);
        }
        */
        return result;
    }

    @Override
    public final boolean untransform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        return SesameTransformUtils.runUpdate(taskInfo, subtask, results,
                DELETE_METADATA_UPDATE, new HashMap<String, Value>());
    }

}
