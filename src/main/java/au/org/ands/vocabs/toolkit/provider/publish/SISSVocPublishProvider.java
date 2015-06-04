package au.org.ands.vocabs.toolkit.provider.publish;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** SISSVoc publish provider. */
public class SISSVocPublishProvider extends PublishProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Key of the subtask object that contains additional
     * replacements to use in the spec file template.
     */
    private static final String SPEC_SETTINGS_KEY = "spec_settings";

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

//    /** URL to access the Sesame server. */
//    private String sissvocPath =
//            PROPS.getProperty("SISSVocPublish.spec_path");

    /** The location of the spec file template. */
    private String sissvocSpecTemplatePath =
            PROPS.getProperty("SISSVoc.specTemplate");

    /** The directory in which to write generated spec files. */
    private String sissvocSpecOutputPath =
            PROPS.getProperty("SISSVoc.specsPath");


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
        writeSpecFile(taskInfo, subtask, results);

        // results.put("sissvoc_path",
        //   TasksUtils.getTaskRepositoryId(taskInfo));
        return true;
    }

    @Override
    public final boolean unpublish(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        removeSpecFile(taskInfo, subtask, results);

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
                PROPS.getProperty("SISSVoc.variable.DEPLOYPATH",
                        "/repository/api/lda"));
        // The name of the ANDS Vocabulary service
        specProperties.put("SERVICE_TITLE",
                PROPS.getProperty("SISSVoc.variable.SERVICE_TITLE",
                        "ANDS Vocabularies LDA service"));
        // The name of the ANDS Vocabulary service owner
        specProperties.put("SERVICE_AUTHOR",
                PROPS.getProperty("SISSVoc.variable.SERVICE_AUTHOR",
                        "ANDS Services"));
        // Contact email address for the ANDS Vocabulary service owner
        specProperties.put("SERVICE_AUTHOR_EMAIL",
                PROPS.getProperty("SISSVoc.variable.SERVICE_AUTHOR_EMAIL",
                        "services@ands.org.au"));
        // Homepage of the ANDS Vocabulary service
        // ANDS home page for now; in future, could be
        // vocabs.ands.org.au itself.
        specProperties.put("SERVICE_HOMEPAGE",
                PROPS.getProperty("SISSVoc.variable.SERVICE_HOMEPAGE",
                        "http://www.ands.org.au/"));
        // Vocabulary title
        specProperties.put("SERVICE_LABEL",
                StringEscapeUtils.escapeJava(
                        taskInfo.getVocabulary().getTitle()));
        String repositoryId = TasksUtils.getTaskRepositoryId(taskInfo);
        // SPARQL endpoint to use for doing queries
        specProperties.put("SPARQL_ENDPOINT",
                PROPS.getProperty("SISSVoc.variable.SPARQL_ENDPOINT_PREFIX",
                        "http://localhost:8080/repository/"
                                + "openrdf-sesame/repositories/")
                                + repositoryId);
        specProperties.put("SVC_ID", repositoryId);
        // Additional path to all the endpoints for this repository.
        // The template assumes the variable begins with a slash.
        specProperties.put("SVC_PREFIX",
                "/" + TasksUtils.getSISSVocRepositoryPath(taskInfo));
        // Path to the XSL stylesheet that generates the HTML pages.
        // Path is relative to the SISSVoc webapp.
        specProperties.put("HTML_STYLESHEET",
                PROPS.getProperty("SISSVoc.variable.HTML_STYLESHEET",
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
     */
    private void writeSpecFile(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        File templateFile = new File(sissvocSpecTemplatePath);
        String specTemplate;
        try {
            specTemplate = FileUtils.readFileToString(templateFile);
        } catch (IOException e) {
            logger.error("SISSVoc writeSpecFile: can't open template file.",
                    e);
            return;
        }
        StrSubstitutor sub = new StrSubstitutor(specProperties);
        String customSpec = sub.replace(specTemplate);
        ToolkitFileUtils.requireDirectory(sissvocSpecOutputPath);
        File specFile = new File(
                Paths.get(sissvocSpecOutputPath).
                resolve(TasksUtils.getTaskRepositoryId(taskInfo)
                        + ".ttl").toString());
        try {
            FileUtils.writeStringToFile(specFile, customSpec);
        } catch (IOException e) {
            logger.error("SISSVoc writeSpecFile: can't write spec file.",
                    e);
            return;
         }

    }

    /** Remove any existing spec file for SISSVoc.
     * @param taskInfo The TaskInfo object for this task.
     * @param subtask The specification of this publish subtask
     * @param results HashMap representing the result of the publish.
     */
    private void removeSpecFile(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        try {
            Files.deleteIfExists(Paths.get(sissvocSpecOutputPath).
                    resolve(TasksUtils.getTaskRepositoryId(taskInfo)
                            + ".ttl"));
        } catch (IOException e) {
            // This may mean a file permissions problem, so do log it.
            logger.error("removeSpecFile failed", e);
        }
    }

}
