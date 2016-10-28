/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.publish;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.db.AccessPointUtils;
import au.org.ands.vocabs.toolkit.db.model.AccessPoint;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.PropertyConstants;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitNetUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** SISSVoc publish provider. */
public class SISSVocPublishProvider extends PublishProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Key of the subtask object that contains additional
     * replacements to use in the spec file template.
     */
    private static final String SPEC_SETTINGS_KEY = "spec_settings";

    /** The location of the spec file template. */
    private String sissvocSpecTemplatePath = ToolkitProperties.getProperty(
            PropertyConstants.SISSVOC_SPECTEMPLATE);

    /** The directory in which to write generated spec files. */
    private String sissvocSpecOutputPath = ToolkitProperties.getProperty(
            PropertyConstants.SISSVOC_SPECSPATH);

    /** URL that is a prefix to all SISSVoc endpoints. */
    private String sissvocEndpointsPrefix = ToolkitProperties.getProperty(
            PropertyConstants.SISSVOC_ENDPOINTSPREFIX);

    /** Values to be substituted in the spec file template. */
    private final HashMap<String, String> specProperties =
            new HashMap<String, String>();

    @Override
    public final String getInfo() {
        return "";
    }

    @Override
    public final boolean publish(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        addBasicSpecProperties(taskInfo);
        addAdditionalSpecProperties(subtask);
        if (!writeSpecFile(taskInfo, subtask, results)) {
            return false;
        }

        // Use the nice JAX-RS libraries to construct the path to
        // the SPARQL endpoint.
        Client client = ToolkitNetUtils.getClient();
        WebTarget target = client.target(sissvocEndpointsPrefix);
        WebTarget sparqlTarget = target
                .path(ToolkitFileUtils.getSISSVocRepositoryPath(taskInfo));
        results.put("sissvoc_endpoints",
                sparqlTarget.getUri().toString());
        // Add sissvoc endpoint.
        AccessPointUtils.createSissvocAccessPoint(taskInfo.getVersion(),
                sparqlTarget.getUri().toString(), AccessPoint.SYSTEM_SOURCE);
        return true;
    }

    @Override
    public final boolean unpublish(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        // Remove the sissvoc access point.
        AccessPointUtils.deleteAccessPointsForVersionAndType(
                taskInfo.getVersion(), AccessPoint.SISSVOC_TYPE);
        // Use the following version when the elda library
        // supports it.
        //        removeSpecFile(taskInfo, subtask, results);
        // For now, use the truncation method.
        if (!truncateSpecFileIfExists(taskInfo, subtask, results)) {
            return false;
        }

        // results.put("sissvoc_path",
        //   TasksUtils.getTaskRepositoryId(taskInfo));
        return true;
    }


    /** Add the essential properties required by the spec file template.
     * @param taskInfo The TaskInfo object for this task.
     */
    private void addBasicSpecProperties(final TaskInfo taskInfo) {
        // Top-level of deployment path
        specProperties.put("DEPLOYPATH",
                ToolkitProperties.getProperty(
                        PropertyConstants.SISSVOC_VARIABLE_DEPLOYPATH,
                        "/repository/api/lda"));
        // The name of the ANDS Vocabulary service
        specProperties.put("SERVICE_TITLE",
                ToolkitProperties.getProperty(
                        PropertyConstants.SISSVOC_VARIABLE_SERVICE_TITLE,
                        "ANDS Vocabularies LDA service"));
        // The name of the ANDS Vocabulary service owner
        specProperties.put("SERVICE_AUTHOR",
                ToolkitProperties.getProperty(
                        PropertyConstants.SISSVOC_VARIABLE_SERVICE_AUTHOR,
                        "ANDS Services"));
        // Contact email address for the ANDS Vocabulary service owner
        specProperties.put("SERVICE_AUTHOR_EMAIL",
                ToolkitProperties.getProperty(
                        PropertyConstants.SISSVOC_VARIABLE_SERVICE_AUTHOR_EMAIL,
                        "services@ands.org.au"));
        // Homepage of the ANDS Vocabulary service
        // ANDS home page for now; in future, could be
        // vocabs.ands.org.au itself.
        specProperties.put("SERVICE_HOMEPAGE",
                ToolkitProperties.getProperty(
                        PropertyConstants.SISSVOC_VARIABLE_SERVICE_HOMEPAGE,
                        "http://www.ands.org.au/"));
        // Vocabulary title
        specProperties.put("SERVICE_LABEL",
                StringEscapeUtils.escapeJava(
                        taskInfo.getVocabulary().getTitle()));
        String repositoryId = ToolkitFileUtils.getSesameRepositoryId(taskInfo);
        // SPARQL endpoint to use for doing queries
        specProperties.put("SPARQL_ENDPOINT",
                ToolkitProperties.getProperty(
                    PropertyConstants.SISSVOC_VARIABLE_SPARQL_ENDPOINT_PREFIX,
                        "http://localhost:8080/repository/"
                                + "openrdf-sesame/repositories/")
                                + repositoryId);
        specProperties.put("SVC_ID", repositoryId);
        // Additional path to all the endpoints for this repository.
        // The template assumes the variable begins with a slash.
        specProperties.put("SVC_PREFIX",
                "/" + ToolkitFileUtils.getSISSVocRepositoryPath(taskInfo));
        // Path to the XSL stylesheet that generates the HTML pages.
        // Path is relative to the SISSVoc webapp.
        specProperties.put("HTML_STYLESHEET",
                ToolkitProperties.getProperty(
                        PropertyConstants.SISSVOC_VARIABLE_HTML_STYLESHEET,
                        "resources/default/transform/ands-ashtml-sissvoc.xsl"));
        // Empty string for now
        specProperties.put("NAMESPACES", "");
        // Title of the vocab displayed at the top of HTML pages
        specProperties.put("ANDS_VOCABNAME",
                StringEscapeUtils.escapeJava(
                        taskInfo.getVocabulary().getTitle()));
        // Add more properties here, if/when needed.
//        specProperties.put("", "");
        // The above properties are all more-or-less required.
        // Below are properties that are optional, and
        // may be overridden by the subtask settings.
        specProperties.put("ANDS_VOCABMORE", "");
        specProperties.put("ANDS_VOCABAPIDOCO", "");
    }

    /** Add the additional properties as provided in the subtask
     * specification. Values are escaped using StringEscapeUtils.escapeJava()
     * to prevent nasty injection.
     * @param subtask The specification of this publish subtask
     */
    private void addAdditionalSpecProperties(final JsonNode subtask) {
        if (subtask.get(SPEC_SETTINGS_KEY) == null) {
            // No additional properties specified.
            return;
        }
        for (Iterator<Entry<String, JsonNode>> nodeIterator =
                subtask.get(SPEC_SETTINGS_KEY).fields();
                nodeIterator.hasNext();) {
            Entry<String, JsonNode> specProperty = nodeIterator.next();
            logger.debug("addAdditionalSpecProperties replacing with"
                    + " value: " + specProperty.getValue().textValue());
            specProperties.put(
                    specProperty.getKey(),
                    StringEscapeUtils.escapeJava(
                            specProperty.getValue().textValue()));
        }
    }

    /** Write out the spec file for SISSVoc.
     * @param taskInfo The TaskInfo object for this task.
     * @param subtask The specification of this publish subtask
     * @param results HashMap representing the result of the publish.
     * @return True iff success.
     */
    private boolean writeSpecFile(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        File templateFile = new File(sissvocSpecTemplatePath);
        String specTemplate;
        try {
            specTemplate = FileUtils.readFileToString(templateFile,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            results.put(TaskStatus.EXCEPTION,
                    "SISSVoc writeSpecFile: can't open template file");
            logger.error("SISSVoc writeSpecFile: can't open template file",
                    e);
            return false;
        }
        StrSubstitutor sub = new StrSubstitutor(specProperties);
        String customSpec = sub.replace(specTemplate);
        ToolkitFileUtils.requireDirectory(sissvocSpecOutputPath);
        File specFile = new File(
                Paths.get(sissvocSpecOutputPath).
                resolve(ToolkitFileUtils.getSesameRepositoryId(taskInfo)
                        + ".ttl").toString());
        try {
            FileUtils.writeStringToFile(specFile, customSpec,
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            results.put(TaskStatus.EXCEPTION,
                    "SISSVoc writeSpecFile: can't write spec file");
            logger.error("SISSVoc writeSpecFile: can't write spec file.",
                    e);
            return false;
        }
        return true;
    }

    /** If there is an existing spec file for SISSVoc, overwrite
     * it and truncate it to zero size. This is the workaround
     * for unpublication until the elda library supports detection
     * of deleted files.
     * @param taskInfo The TaskInfo object for this task.
     * @param subtask The specification of this publish subtask
     * @param results HashMap representing the result of the unpublish.
     * @return True iff success.
     */
    private boolean truncateSpecFileIfExists(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        try {
            Path specFilePath = Paths.get(sissvocSpecOutputPath).
                    resolve(ToolkitFileUtils.getSesameRepositoryId(taskInfo)
                            + ".ttl");
            if (Files.exists(specFilePath)) {
                Files.write(specFilePath, new byte[0]);
            }

        } catch (IOException e) {
            // This may mean a file permissions problem, so do log it.
            results.put(TaskStatus.EXCEPTION,
                    "SISSVoc truncateSpecFileIfExists: failed");
            logger.error("truncateSpecFileIfExists failed", e);
            return false;
        }
        return true;
    }

    /** Remove any existing spec file for SISSVoc.
     * @param taskInfo The TaskInfo object for this task.
     * @param subtask The specification of this publish subtask
     * @param results HashMap representing the result of the unpublish.
     * @return True iff success.
     */
    @SuppressWarnings("unused")
    private boolean removeSpecFile(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        try {
            Files.deleteIfExists(Paths.get(sissvocSpecOutputPath).
                    resolve(ToolkitFileUtils.getSesameRepositoryId(taskInfo)
                            + ".ttl"));
        } catch (IOException e) {
            // This may mean a file permissions problem, so do log it.
            results.put(TaskStatus.EXCEPTION,
                    "SISSVoc removeSpecFile: failed");
            logger.error("removeSpecFile failed", e);
            return false;
        }
        return true;
    }

}
