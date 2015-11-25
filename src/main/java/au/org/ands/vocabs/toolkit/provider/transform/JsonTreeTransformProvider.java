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

import org.apache.commons.io.FileUtils;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
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

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Transform provider for generating a tree-like representation of the
 * concepts as JSON. This assumes a vocabulary encoded using SKOS. */
public class JsonTreeTransformProvider extends TransformProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Short form of the concept type name. Used both in typesToLookFor
     * and populateTopConcepts(). */
    private static final String CONCEPT_SHORT_FORM = "Concept";

    /** A map of SKOS types to take note of. */
    private static HashMap<URI, String> typesToLookFor =
            new HashMap<URI, String>();

    static {
        typesToLookFor.put(SKOS.CONCEPT_SCHEME, "ConceptScheme");
        typesToLookFor.put(SKOS.CONCEPT, CONCEPT_SHORT_FORM);
        typesToLookFor.put(SKOS.COLLECTION, "Collection");
        typesToLookFor.put(SKOS.ORDERED_COLLECTION, "OrderedCollection");
    }

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    @Override
    public final String getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        Path dir = Paths.get(ToolkitFileUtils.getTaskHarvestOutputPath(
                taskInfo));
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
                logger.debug("Reading RDF: " + entry.toString());
            }
        } catch (DirectoryIteratorException
                | IOException
                | RDFParseException
                | RDFHandlerException
                | UnsupportedRDFormatException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in JsonTreeTransform while Parsing RDF");
            logger.error("Exception in JsonTreeTransform while Parsing RDF:",
                    ex);
            return false;
        }

        String resultFileNameTree = ToolkitFileUtils.getTaskOutputPath(taskInfo,
                "concepts_tree.json");
        try {
            File out = new File(resultFileNameTree);
            results.put("concepts_tree", resultFileNameTree);
            HashMap<String, HashMap<String, Object>> conceptTree =
                    conceptHandler.buildTree();

            FileUtils.writeStringToFile(out,
                    TasksUtils.hashMapToJSONString(conceptTree));
        } catch (IOException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in JsonTreeTransform while generating result");
            logger.error("Exception in JsonTreeTransform generating result:",
                    ex);
            return false;
        }
        return true;
    }

    /** RDF Handler to extract prefLabels, notation, definition,
     * and use broader and narrow properties to construct a tree-like
     * structure. */
    class ConceptHandler extends RDFHandlerBase {

        /** Map from concept IRI to a map that maps
         * property name to the property value(s). */
        private HashMap<String, HashMap<String, Object>> conceptMap =
                new HashMap<String, HashMap<String, Object>>();

        /** The top-most concepts of the vocabulary. This is based on
         * finding all concepts that do not have a broader concept. */
        private HashMap<String, HashMap<String, Object>> topmostConcepts =
                new HashMap<String, HashMap<String, Object>>();

        @Override
        public void handleStatement(final Statement st) {
            if (conceptMap.get(st.getSubject().stringValue()) == null) {
                conceptMap.put(st.getSubject().stringValue(),
                        new HashMap<String, Object>());
            }
            HashMap<String, Object> concept =
                    conceptMap.get(st.getSubject().stringValue());
            if (st.getPredicate().equals(RDF.TYPE)) {
                Value typeIRI = st.getObject();
                if (typesToLookFor.containsKey(typeIRI)) {
                    concept.put("type", typesToLookFor.get(typeIRI));
                }
            }
            if (st.getPredicate().equals(SKOS.PREF_LABEL)) {
                concept.put("prefLabel", st.getObject().stringValue());
            }
            // Future work: uncomment the next six lines
            // when the portal is ready to receive it.
//            if (st.getPredicate().equals(SKOS.ALT_LABEL)) {
//                concept.put("altLabel", st.getObject().stringValue());
//            }
//            if (st.getPredicate().equals(SKOS.HIDDEN_LABEL)) {
//                concept.put("hiddenLabel", st.getObject().stringValue());
//            }
            if (st.getPredicate().equals(SKOS.NOTATION)) {
                concept.put("notation", st.getObject().stringValue());
            }
            if (st.getPredicate().equals(SKOS.DEFINITION)) {
                concept.put("definition", st.getObject().stringValue());
            }
            if (st.getPredicate().equals(SKOS.BROADER)) {
                if (concept.get("broader") == null) {
                    concept.put("broader",
                            new ArrayList<String>());
                }
                @SuppressWarnings("unchecked")
                ArrayList<String> broaderList =
                        (ArrayList<String>) concept.get("broader");
                broaderList.add(st.getObject().stringValue());
            }
            if (st.getPredicate().equals(SKOS.NARROWER)) {
                if (concept.get("narrower") == null) {
                    concept.put("narrower",
                            new ArrayList<String>());
                }
                @SuppressWarnings("unchecked")
                ArrayList<String> narrowerList =
                        (ArrayList<String>) concept.get("narrower");
                narrowerList.add(st.getObject().stringValue());
            }
            // Future work: uncomment the next ten lines when work begins
            // on handling collections.
//            if (st.getPredicate().equals(SKOS.MEMBER)) {
//                if (concept.get("member") == null) {
//                    concept.put("member",
//                            new ArrayList<String>());
//                }
//                @SuppressWarnings("unchecked")
//                ArrayList<String> memberList =
//                    (ArrayList<String>) concept.get("member");
//                memberList.add(st.getObject().stringValue());
//            }
        }

        /** Build the concepts tree.
         *  @return The tree of concepts. */
        @SuppressWarnings("unchecked")
        public HashMap<String, HashMap<String, Object>> buildTree() {
            // This is a rearranged version of conceptMap, with
            // the concepts arranged in a tree structure based on
            // the broader/narrower relations.
            HashMap<String, HashMap<String, Object>> conceptTreeMap =
                    new HashMap<String, HashMap<String, Object>>();
            populateTopConcepts();
            for (Entry<String, HashMap<String, Object>> topmostConcept
                    : topmostConcepts.entrySet()) {
                if (topmostConcept.getValue().containsKey("narrower")) {
                    ArrayList<String> narrowerList =
                            (ArrayList<String>)
                            topmostConcept.getValue().get("narrower");
                    // Remove the narrower map; it will be replaced
                    // by the tree structure of narrower concepts.
                    topmostConcept.getValue().remove("narrower");
                    for (String narrowerKey : narrowerList) {
                        HashMap<String, Object> narrower =
                                buildNarrower(narrowerKey);
                        if (narrower != null) {
                            topmostConcept.getValue().put(narrowerKey,
                                    narrower);
                        }
                    }

                }
                conceptTreeMap.put(topmostConcept.getKey(),
                        (HashMap<String, Object>)
                        (topmostConcept.getValue().clone()));
            }
            return conceptTreeMap;
        }

        /** Resolve all narrower concepts of a concept.
         * NB: This method is recursive.
         * @param key The key of the narrower concept.
         * @return The tree of concept text */
        private HashMap<String, Object> buildNarrower(final String key) {
            if (conceptMap.get(key) == null) {
                logger.error("buildNarrower Orphan key: " + key);
                return null;
            }
            HashMap<String, Object> narrowerConcepts = conceptMap.get(key);
            if (narrowerConcepts.containsKey("broader")) {
                // Remove all broader maps; they don't get returned in
                // the resulting tree structure. The tree structure
                // is based on narrower relations.
                narrowerConcepts.remove("broader");
            }
            if (narrowerConcepts.containsKey("narrower")) {
                @SuppressWarnings("unchecked")
                ArrayList<String> narrowerList =
                        (ArrayList<String>)
                        narrowerConcepts.get("narrower");
                narrowerConcepts.remove("narrower");
                for (String narrowerKey : narrowerList) {
                    HashMap<String, Object> narrowerChildAndDescendantsMap =
                            buildNarrower(narrowerKey);
                    if (narrowerChildAndDescendantsMap != null) {
                        narrowerConcepts.put(narrowerKey,
                                narrowerChildAndDescendantsMap);
                    }
                }
            }
            // For now, remove all occurrences of the type.
            // Future work: support collections.
            narrowerConcepts.remove("type");
            return narrowerConcepts;
        }

        /** Populate the top-most concepts.
         * A concept is considered to be "top-most" if it is a SKOS Concept
         * and it
         * does not specify any broader concepts.
         * This (probably) catches both concepts explicitly
         * labelled as top concepts, and also any "dangling"
         * concepts.
         * FIXME: Cope with a vocabulary with a hierarchy specified only using
         * the narrower property. */
        private void populateTopConcepts() {
            for (Entry<String, HashMap<String, Object>>
            concept : conceptMap.entrySet()) {
                HashMap<String, Object> propertyMap = concept.getValue();
                if (!propertyMap.isEmpty()
                        && CONCEPT_SHORT_FORM.equals(propertyMap.get("type"))
                        && propertyMap.get("broader") == null) {
                    topmostConcepts.put(concept.getKey(), propertyMap);
                    // For now, remove all occurrences of the type.
                    // Future work: support collections.
                    concept.getValue().remove("type");
                }
            }
        }
    }

    @Override
    public final boolean untransform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        return false;
    }

}
