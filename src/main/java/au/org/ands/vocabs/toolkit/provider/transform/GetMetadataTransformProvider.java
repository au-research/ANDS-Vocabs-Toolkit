package au.org.ands.vocabs.toolkit.provider.transform;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

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
                rdfParser.parse(is, entry.toString());
                logger.debug("Reading RDF:" + entry.toString());

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

        /** Number of concept resources. */
        private int countedConcepts = 0;

        /** Map for metadata contained in the graph.
         * Keys are property strings ("dcterms:title", etc., as
         * per the values of the metadataToLookFor map.
         * Values are maps, with: keys: source file name,
         * value: maps. These maps have: keys: "value" (+ optional
         * "_" + language tag), value: either a String, or an ArrayList
         * of Strings of corresponding values. */
        private HashMap<String, Object> metadataMap =
                new HashMap<String, Object>();

        /** Source filename of the metadata. */
        private String source = "";

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
        @SuppressWarnings("unchecked")
        private void addToMap(final String key, final Statement st) {

            String value = st.getObject().stringValue();
            String lang = "";
            // mMap: keys: source file name, values: maps, with
            // keys: "value" (+ optional "_" + language tag),
            // value: either a String, or an ArrayList
            // of Strings of corresponding values. (The values are
            // the same type as aMap.)
            HashMap<String, Object> mMap;
            // aMap: keys: "value" (+ optional "_" + language tag),
            // value: either a String, or an ArrayList
            // of Strings of corresponding values. (The values are
            // the same type as aMap.)
            HashMap<String, Object> aMap;
            // If st's object is a literal and has a language tag,
            // set lang to be "_" plus the tag.
            if (st.getObject().getClass().equals(LiteralImpl.class)) {
                if (((Literal) st.getObject()).getLanguage() != null) {
                    lang = "_" + ((Literal)
                            st.getObject()).getLanguage().toString();
                }
            }

            if (metadataMap.containsKey(key)) {
                mMap = (HashMap<String, Object>) metadataMap.get(key);
            } else {
                mMap = new HashMap<String, Object>();
                metadataMap.put(key, mMap);
            }

            if (mMap.containsKey(source)) {
                aMap =  (HashMap<String, Object>) mMap.get(source);
            } else {
                aMap = new HashMap<String, Object>();
                mMap.put(source, aMap);
            }

            if (aMap.containsKey("value" + lang)) {
                // Already in the map
                if (aMap.get("value" + lang) instanceof String) {
                    // Create a list and add both the previous and the
                    // new one to it.
                    if (!aMap.get("value" + lang).equals(value)) {
                    ArrayList<String> aList = new ArrayList<String>();
                    aList.add((String) aMap.get("value" + lang));
                    aList.add(value);
                    aMap.put("value" + lang, aList);
                    }
                } else {
                    // Already a list
                    ArrayList<String> aList =
                    (ArrayList<String>) aMap.get("value" + lang);
                    if (!aList.contains(value)) {
                        aList.add(value);
                    }
                }
            } else {
                aMap.put("value" + lang, value);
            }
        }

        /** Getter for concepts list. */
        /** @return The completed concept map. */
        public HashMap<String, Object> getMetadata() {
            return metadataMap;
        }

        /** Setter for source. */
        /** @param aSource Source filename. */
        public void setSource(final String aSource) {
            source = aSource;
        }
    }

}
