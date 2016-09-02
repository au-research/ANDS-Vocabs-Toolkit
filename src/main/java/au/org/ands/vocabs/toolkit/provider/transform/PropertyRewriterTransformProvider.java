/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.PropertyConstants;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Transform provider for rewriting metadata by applying rewritings
 * as specified in the metadata rewrite configuration file.  */
public class PropertyRewriterTransformProvider extends TransformProvider {

    /** Logger for this class. */
   private final Logger logger = LoggerFactory.getLogger(
           MethodHandles.lookup().lookupClass());

   /** Access to the Toolkit properties. */
   protected static final Properties PROPS = ToolkitProperties.getProperties();

   /** A map of metadata properties to look for and rewrite. */
   private static HashMap<URI, String> metadataToLookFor =
           new HashMap<URI, String>();

   static {
       metadataToLookFor.put(DCTERMS.TITLE, "dcterms:title");
       metadataToLookFor.put(DCTERMS.DESCRIPTION, "dcterms:description");
       metadataToLookFor.put(DCTERMS.LICENSE, "dcterms:license");
       metadataToLookFor.put(DCTERMS.LANGUAGE, "dcterms:language");
       metadataToLookFor.put(DCTERMS.SUBJECT, "dcterms:subject");
       metadataToLookFor.put(DCTERMS.IDENTIFIER, "dcterms:identifier");
       metadataToLookFor.put(DCTERMS.PUBLISHER, "dcterms:publisher");
       metadataToLookFor.put(DCTERMS.CREATOR, "dcterms:creator");
       metadataToLookFor.put(DCTERMS.CONTRIBUTOR, "dcterms:contributor");
   }

   /** The path to the metadata rewrite configuration file. */
   protected static final String METADATA_REWRITE_MAP_PATH =
           PROPS.getProperty(PropertyConstants.TOOLKIT_METADATAREWRITEMAPPATH);

   /** The configuration for property rewriting. */
   private HierarchicalINIConfiguration metadataRewriteConf;

    @Override
    public final String getInfo() {
        // Not implemented.
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        // Prepare for rewriting.
        if (!loadRewriteMap()) {
            results.put(TaskStatus.ERROR,
                    "PropertyRewriter unable to load rewrite map");
            return false;
        }

        Path originalHarvestdir =
                Paths.get(ToolkitFileUtils.getTaskHarvestOutputPath(
                        taskInfo));
        // Use this transform name and the task ID to construct
        // the path names.
        String transformName = "PropertyRewriter_"
                + taskInfo.getTask().getId();
        String transformOutputDir =
                ToolkitFileUtils.getTaskTransformTemporaryOutputPath(taskInfo,
                        transformName);
        Path transformOutputDirPath =
                Paths.get(transformOutputDir);

        try {
            ToolkitFileUtils.requireEmptyDirectory(transformOutputDir);
        } catch (IOException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in PropertyRewriter while cleaning old "
                            + "transform output directory");
            logger.error("Exception in PropertyRewriter while cleaning old "
                    + "transform output directory: ",
                    ex);
            return false;
        }

        // Open the harvest directory ...
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(originalHarvestdir)) {
            // ... and iterate over every file in the harvest directory.
            for (Path entry: stream) {
                // First, parse the file into a model and do rewriting.
                Model model = new LinkedHashModel();
                RDFFormat format = Rio.getParserFormatForFileName(
                        entry.toString());
                RDFParser rdfParser = Rio.createParser(format);
                ConceptHandler conceptHandler = new ConceptHandler(
                        metadataRewriteConf, model);
                rdfParser.setRDFHandler(conceptHandler);
                FileInputStream is = new FileInputStream(entry.toString());
                logger.debug("Reading RDF:" + entry.toString());
                rdfParser.parse(is, entry.toString());
                // And now serialize the result.
                String resultFileName =
                        transformOutputDirPath.resolve(
                                entry.getFileName()).toString();
                FileOutputStream out = new FileOutputStream(resultFileName);
                // Write in the same format we read.
                Rio.write(model, out, format);
                out.close();
            }
        } catch (DirectoryIteratorException
                | IOException
                | RDFParseException
                | RDFHandlerException
                | UnsupportedRDFormatException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in PropertyRewriter while Parsing RDF");
            logger.error("Exception in PropertyRewriter while Parsing RDF:",
                    ex);
            return false;
        }

        // Done rewriting, and was successful. Replace the old
        // harvest with the transformed files.
        if (!ToolkitFileUtils.renameTransformTemporaryOutputPath(taskInfo,
                transformName)) {
            results.put(TaskStatus.ERROR,
                    "Error in PropertyRewriter when renaming output "
                    + "directory");
            logger.error("Error in PropertyRewriter when renaming output "
                    + "directory");
            return false;
        }

        return true;
    }

    /** Loads the rewrite map into metadataRewriteConf.
     * @return True if loading was successful. */
    private boolean loadRewriteMap() {
        File metadataRewriteMap = new File(METADATA_REWRITE_MAP_PATH);
        try {
            metadataRewriteConf = new HierarchicalINIConfiguration(
                    metadataRewriteMap);
            return true;
        } catch (ConfigurationException e) {
            logger.error("Toolkit.metadataRewriteMapPath is empty, or file"
                    + " can not be loaded", e);
            return false;
        }
    }

    /** RDF Handler to rewrite properties. */
    class ConceptHandler extends RDFHandlerBase {

        /** The configuration for metadata replacement. */
        private HierarchicalINIConfiguration metadataRewriteConf;

        /** The model in which to store triples after rewriting. */
        private Model model;

        /** A factory for creating RDF values. */
        private ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        /** Constructor. Initializes the metadata rewrite map.
         * @param aMetadataRewriteConf The rewriting configuration
         * @param aModel The model in which to store triples after
         * rewriting.
         */
        ConceptHandler(
                final HierarchicalINIConfiguration aMetadataRewriteConf,
                final Model aModel) {
            metadataRewriteConf = aMetadataRewriteConf;
            model = aModel;
        }

        @Override
        public void handleStatement(final Statement st) {
            for (Entry<URI, String> term : metadataToLookFor.entrySet()) {
                if (st.getPredicate().equals(term.getKey())) {
                    String key = term.getValue();
                    String value = st.getObject().stringValue();
                    if (value.isEmpty()) {
                        // Special case: don't throw away empty object
                        // literals.
                        break;
                    }
                    String valueToBeReturned = getMatchedContent(key, value);
                    if (!(valueToBeReturned.isEmpty())) {
                        model.add(st.getSubject(),
                                st.getPredicate(),
                                valueFactory.createLiteral(valueToBeReturned));
                    }
                    // Matched the predicate, so whether there was a
                    // rewriting or not, no need to go further.
                    return;
                }
            }
            // Didn't match any of our known predicates, so add the triple
            // as it is.
            model.add(st);
         }

        /** Get the replacement string for a key in a section.
         * @param section The section to look for.
         * @param key The key to be replaced.
         * @return The replacement value, or the original value
         *  if there is no match.
         */
        public String getMatchedContent(final String section,
                final String key) {
            SubnodeConfiguration sObj = metadataRewriteConf.getSection(section);
            String replacement = sObj.getString(key);
            if (replacement != null) {
                return replacement;
            }
            return key;
        }

    }

    @Override
    public final boolean untransform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        return false;
    }


}
