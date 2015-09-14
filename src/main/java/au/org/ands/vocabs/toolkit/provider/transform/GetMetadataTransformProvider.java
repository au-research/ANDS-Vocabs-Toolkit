/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
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

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Extract metadata from a PoolParty project, and apply rewritings
 * as specified in the metadata rewrite configuration file. */
public class GetMetadataTransformProvider extends TransformProvider {

    /** Logger for this class. */
   private final Logger logger = LoggerFactory.getLogger(
           MethodHandles.lookup().lookupClass());

   /** Access to the Toolkit properties. */
   protected static final Properties PROPS = ToolkitProperties.getProperties();

   /** The path to the metadata rewrite configuration file. */
   protected static final String METADATA_REWRITE_MAP_PATH =
           PROPS.getProperty("Toolkit.metadataRewriteMapPath");

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
   }

    @Override
    public final String getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        return false;
    }

    @Override
    public final boolean untransform(final TaskInfo taskInfo,
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
    public final HashMap<String, Object> extractMetadata(
            final String pPprojectId) {
        Path dir = Paths.get(ToolkitFileUtils.getMetadataOutputPath(
                pPprojectId));
        HashMap<String, Object> results = new HashMap<String, Object>();
        ConceptHandler conceptHandler = new ConceptHandler();
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(dir)) {
            for (Path entry: stream) {
                conceptHandler.setSource(entry.getFileName().toString());
                RDFFormat format = Rio.getParserFormatForFileName(
                        entry.toString());
                RDFParser rdfParser = Rio.createParser(format);
                rdfParser.setRDFHandler(conceptHandler);
                FileInputStream is = new FileInputStream(entry.toString());
                logger.debug("Reading RDF:" + entry.toString());
                rdfParser.parse(is, entry.toString());
            }
        } catch (DirectoryIteratorException
                | IOException
                | RDFParseException
                | RDFHandlerException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in extractMetadata while Parsing RDF");
            logger.error("Exception in extractMetadata while Parsing RDF:",
                    ex);
            return results;
        }
        results.putAll(conceptHandler.getMetadata());
        results.put("concept_count", Integer.toString(
                conceptHandler.getCountedConcepts()));
        return results;
    }
    /** RDF Handler to extract metadata and construct maps. */
    class ConceptHandler extends RDFHandlerBase {

        /** The configuration for metadata replacement. */
        private HierarchicalINIConfiguration metadataRewriteConf;

        /** Number of concept resources. */
        private int countedConcepts = 0;

        /** Map for metadata contained in the graph.
         * Keys are property strings ("dcterms:title", etc., as
         * per the values of the metadataToLookFor map.
         * Values are maps, with: keys: source file name,
         * value: maps. These maps have: keys: "value" (+ optional
         * "_" + language tag), value: an ArrayList
         * of Strings of corresponding values. */
        private HashMap<String, HashMap<String, HashMap<String,
            ArrayList<String>>>> metadataMap =
                new HashMap<String, HashMap<String, HashMap<String,
                ArrayList<String>>>>();

        /** Source filename of the metadata. */
        private String source = "";

        /** Constructor. Initializes the metadata rewrite map. */
        public ConceptHandler() {
            loadRewriteMap();
        }

        @Override
        public void handleStatement(final Statement st) {
            for (Entry<URI, String> term : metadataToLookFor.entrySet()) {
                if (st.getPredicate().equals(term.getKey())) {
                   addToMap(term.getValue(), st);
                }
            }
            if (st.getPredicate().equals(RDF.TYPE)
                    && (st.getObject().equals(SKOS.CONCEPT))) {
                countedConcepts++;
            }
        }

        /** Getter for concept count.
         * @return The number of concept resources. */
        public int getCountedConcepts() {
            return countedConcepts;
        }


        /** Add a statement to the metadata Map.
         * See comment for metatdataMap for the structure.
         * @param key Metadata key ("dcterms:title", etc.).
         * @param st The statement to be added.
         */
        private void addToMap(final String key, final Statement st) {

            String value = st.getObject().stringValue();
            String valueToBeReturned = getMatchedContent(key, value);
            if (!(valueToBeReturned.isEmpty())) {
                String lang = "";
                // mMap: keys: source file name, values: maps, with
                // keys: "value" (+ optional "_" + language tag),
                // value: an ArrayList of Strings of corresponding values.
                // (The values are the same type as aMap.)
                HashMap<String, HashMap<String, ArrayList<String>>> mMap;
                // aMap: keys: "value" (+ optional "_" + language tag),
                // value: an ArrayList of Strings of corresponding values.
                HashMap<String, ArrayList<String>> aMap;
                // If st's object is a literal and has a language tag,
                // set lang to be "_" plus the tag.
                if (st.getObject().getClass().equals(LiteralImpl.class)) {
                    if (((Literal) st.getObject()).getLanguage() != null) {
                        lang = "_" + ((Literal)
                                st.getObject()).getLanguage().toString();
                    }
                }

                if (metadataMap.containsKey(key)) {
                    mMap = metadataMap.get(key);
                } else {
                    mMap = new HashMap<String, HashMap<String,
                            ArrayList<String>>>();
                    metadataMap.put(key, mMap);
                }

                if (mMap.containsKey(source)) {
                    aMap =  mMap.get(source);
                } else {
                    aMap = new HashMap<String, ArrayList<String>>();
                    mMap.put(source, aMap);
                }

                ArrayList<String> aList;
                if (!aMap.containsKey("value" + lang)) {
                    // Not already there, so create a new ArrayList
                    // and insert it into aMap.
                    aList = new ArrayList<String>();
                    aMap.put("value" + lang, aList);
                } else {
                    aList = aMap.get("value" + lang);
                }
                // Either way, add the value to the ArrayList iff
                // it is not already there.
                if (!aList.contains(valueToBeReturned)) {
                    aList.add(valueToBeReturned);
                }
            }
        }

        /** Getter for concepts list. */
        /** @return The completed concept map. */
        public HashMap<String, HashMap<String, HashMap<String,
            ArrayList<String>>>> getMetadata() {
            return metadataMap;
        }

        /** Setter for source. */
        /** @param aSource Source filename. */
        public void setSource(final String aSource) {
            source = aSource;
        }

        /** Get the replacement string for a key in a section. */
        /** @param section The section to look for.
        /** @param key The key to be replaced
        /** @return The replacement value, or the original value
         *  if there is no match. */
        public String getMatchedContent(final String section, final String key)
        {
            SubnodeConfiguration sObj = metadataRewriteConf.getSection(section);
            String replacement = sObj.getString(key);
            if (replacement != null) {
                return replacement;
            }
            return key;
        }

        /** Loads the rewrite map into metadataRewriteConf. */
        private void loadRewriteMap() {
            File metadataRewriteMap = new File(METADATA_REWRITE_MAP_PATH);
            try {
                metadataRewriteConf = new HierarchicalINIConfiguration(
                        metadataRewriteMap);
            } catch (ConfigurationException e) {
                logger.error("Toolkit.metadataRewriteMapPath is empty, or file"
                        + " can not be loaded", e);
            }
        }

    }

}
