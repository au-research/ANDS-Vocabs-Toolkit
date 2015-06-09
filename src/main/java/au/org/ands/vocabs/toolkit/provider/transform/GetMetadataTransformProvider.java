package au.org.ands.vocabs.toolkit.provider.transform;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Transform provider for generating a list-like representation of the
 * concepts as JSON. This assumes a vocabulary encoded using SKOS. */
public class GetMetadataTransformProvider extends TransformProvider {

    /** Logger for this class. */
   private final Logger logger = LoggerFactory.getLogger(
           MethodHandles.lookup().lookupClass());

   /** Access to the Toolkit properties. */
   protected static final Properties PROPS = ToolkitProperties.getProperties();

   /** A map of metadata properties to look for and extract. */
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
//       metadataToLookFor.put(DCTERMS., "dcterms:");
//       metadataToLookFor.put(DCTERMS., "dcterms:");
//       metadataToLookFor.put(DCTERMS., "dcterms:");

   }

    @Override
    public final Collection<?> getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        return false;
    }

    /**
     * Parse the files harvested from PoolParty and extract the
     * metadata.
     * @param pPprojectId The PoolParty project id.
     * @return The results of the metadata extraction.
     */
    public final HashMap<String, String> extractMetadata(
            final String pPprojectId) {
        Path dir = Paths.get(TasksUtils.getMetadataOutputPath(pPprojectId));
        HashMap<String, String> results = new HashMap<String, String>();
        ConceptHandler conceptHandler = new ConceptHandler();
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(dir)) {
            for (Path entry: stream) {
                RDFFormat format = Rio.getParserFormatForFileName(
                        entry.toString());
                RDFParser rdfParser = Rio.createParser(format);
                rdfParser.setRDFHandler(conceptHandler);
                FileInputStream is = new FileInputStream(entry.toString());
                rdfParser.parse(is, entry.toString());
                logger.debug("Reading RDF:" + entry.toString());

            }
        } catch (DirectoryIteratorException
                | IOException
                | RDFParseException
                | RDFHandlerException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in JsonListTransform while Parsing RDF");
            logger.error("Exception in JsonListTransform while Parsing RDF:",
                    ex);
            return results;
        }
        results.putAll(conceptHandler.getMetadata());
        results.put("concept_count", Integer.toString(
                conceptHandler.getCountedConcepts()));
        return results;
    }
    /** RDF Handler to extract prefLabels, notation, and use broader
     * and narrow properties to construct a list-like structure. */
    class ConceptHandler extends RDFHandlerBase {

        /** Number of concept resources. */
        private int countedConcepts = 0;

        /** Map for metadata contained in the graph
         * property name to the property value(s). */
        private HashMap<String, String> metadataMap =
                new HashMap<String, String>();


        @Override
        public void handleStatement(final Statement st) {
            for (Entry<URI, String> term : metadataToLookFor.entrySet()) {
                if (st.getPredicate().equals(term.getKey())) {
                    metadataMap.put(term.getValue(),
                            st.getObject().stringValue());
                }
            }
            if (st.getPredicate().equals(RDF.TYPE)
                    && (st.getObject().equals(SKOS.CONCEPT))) {
                countedConcepts++;
            }
        }

        /** Getter for concept count. */
        /** @return The number of concept resources. */
        public int getCountedConcepts() {
            return countedConcepts;
        }


        /** Getter for concepts list. */
        /** @return The completed concept map. */
        public HashMap<String, String> getMetadata() {
            return metadataMap;
        }
    }

}
