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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

/** Transform provider for generating a tree-like representation of the
 * concepts as JSON. This assumes a vocabulary encoded using SKOS.
 * The resulting output is sorted at each level by prefLabel,
 * case-insensitively.
 * Concepts without prefLabels are gathered at the end, sorted
 * by IRI.
 * The structure of the generated JSON is:
 * <pre>
 * [
 *   {
 *     "iri": "http://uri1",
 *     "prefLabel": "Label 1",
 *     "definition": "Definition 1",
 *     "notation": "1"
 *     "narrower": [
 *       {
 *         "iri": "http://uri1/narrower1",
 *         "prefLabel": "Label 1.1",
 *         "definition": "Definition 1.1",
 *         "notation": "1.1",
 *         "narrower": [
 *           {
 *             "iri": "http://uri1/narrower1/narrower1",
 *             "prefLabel": "Label 1.1.1",
 *             "definition": "Definition 1.1.1",
 *             "notation": "1.1.1"
 *           }
 *         ]
 *       }
 *     ]
 *   },
 *   {
 *     "iri": "http://uri2",
 *     "prefLabel: "Label 2",
 *     "definition": "Definition 2"
 *   },
 *   {
 *     "iri": "http://noPrefLabel1",
 *     "definition": "Concepts without preflabels go at the end ..."
 *   },
 *   {
 *     "iri": "http://noPrefLabel2",
 *     "definition": "... sorted by IRI"
 *   }
 * ]
 * </pre>
 * See ANDS-Registry-Core's
 * {@code applications/portal/vocabs/models/_vocabulary.php}, method
 * {@code buildTree()}, for the consumer of the generated data.
 * Changes made to the structure made here need to be reflected there.
 *
 * The input vocabulary can have its hierarchy specified using either
 * skos:narrower or skos:broader; missing properties are inferred.
 * The presence of a cycle in the broader/narrower
 * relationships is detected, and this will result in an error being
 * returned, rather than causing a stack overflow.
 */
public class JsonTreeTransformProvider extends TransformProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Short form of the concept type name. Used both in
     * {@link #typesToLookFor} and
     * {@link ConceptHandler#populateTopConcepts()}. */
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

    @Override
    public final String getInfo() {
        // Not implemented.
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        Path dir = Paths.get(ToolkitFileUtils.getTaskHarvestOutputPath(
                taskInfo));
        ConceptHandler conceptHandler = new ConceptHandler();
        // Parse all input files in the harvest directory, loading
        // the content into conceptHandler.
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

        // Extract the result, save in results Set and store in the
        // file system.
        String resultFileNameTree = ToolkitFileUtils.getTaskOutputPath(taskInfo,
                "concepts_tree.json");
        try {
            File out = new File(resultFileNameTree);
            results.put("concepts_tree", resultFileNameTree);
            Set<Concept> conceptTree = conceptHandler.buildTree();

            // Serialize the tree and write to the file system.
            // Jackson will serialize TreeSets in sorted order of values
            // (i.e., the Concept objects' prefLabels).
            FileUtils.writeStringToFile(out,
                    TaskUtils.collectionToJSONString(conceptTree));
        } catch (IOException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in JsonTreeTransform while generating result");
            logger.error("Exception in JsonTreeTransform generating result:",
                    ex);
            return false;
        }
        return true;
    }

    /** Inner class for representing concepts, to be used as
     * values of Sets that store concepts. An instance
     * stores an IRI, its narrower Concepts, and (optional) metadata
     * of the concept: a prefLabel, definition, and notation.
     * The natural order of instances
     * ({@link #compareTo(JsonTreeTransformProvider.Concept)})
     * is based on a case-insensitive comparison of the prefLabels,
     * but equality, and the value of {@link #toString()}, is based
     * on the IRI.
     * The purpose of this class is to facilitate sorting of
     * the result of this transform based on prefLabels.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Concept implements Comparable<Concept> {

        /** The IRI of the concept. */
        private String iri;

        /** The prefLabel of the concept. */
        private String prefLabel;

        /** The definition of the concept. */
        private String definition;

        /** The notation of the concept. */
        private String notation;

        /** The narrower concepts of the concept. */
        private TreeSet<Concept> narrower;

        /** Constructor with an IRI specified.
         * @param anIRI The IRI of the concept. Must be non-null.
         * @throws IllegalArgumentException Thrown if {@code anIRI == null}.
         */
        Concept(final String anIRI) {
            if (anIRI == null) {
                throw new IllegalArgumentException("Won't make a Concept "
                        + "with a null IRI");
            }
            iri = anIRI;
        }

        /** Get the IRI.
         * @return The value of the IRI.
         */
        public String getIri() {
            return iri;
        }

        /** Set the prefLabel.
         * @param aPrefLabel The value of the prefLabel.
         */
        public void setPrefLabel(final String aPrefLabel) {
            prefLabel = aPrefLabel;
        }

        /** Get the prefLabel.
         * @return The value of the prefLabel.
         */
        public String getPrefLabel() {
            return prefLabel;
        }

        /** Set the definition.
         * @param aDefinition The value of the definition.
         */
        public void setDefinition(final String aDefinition) {
            definition = aDefinition;
        }

        /** Get the definition.
         * @return The value of the definition.
         */
        public String getDefinition() {
            return definition;
        }

        /** Set the notation.
         * @param aNotation The value of the notation.
         */
        public void setNotation(final String aNotation) {
            notation = aNotation;
        }

        /** Get the notation.
         * @return The value of the notation.
         */
        public String getNotation() {
            return notation;
        }

        /** Set the Set of narrower concepts.
         * @param aNarrower The value of the Set of narrower concepts.
         */
        public void setNarrower(final TreeSet<Concept> aNarrower) {
            if (narrower != null) {
                // We've already done this. This means there is
                // a cycle in the narrower relationships!
                throw new IllegalArgumentException(
                        "Cycle in narrower terms: IRI = " + iri);
            }
            narrower = aNarrower;
        }

        /** Get the Set of narrower concepts.
         * @return The Set of narrower concepts.
         */
        public TreeSet<Concept> getNarrower() {
            return narrower;
        }

        /** {@inheritDoc}
         * Comparison based first (case-insensitively) on prefLabels,
         * then on IRIs.
         * All Concepts with null prefLabels are sorted at the end
         * (by their IRIs).
         */
        @Override
        public int compareTo(final Concept otherConcept) {
            if (otherConcept == null) {
                // NPE required by the contract specified in
                // the Javadocs of Comparable<T>.
                throw new NullPointerException();
            }
            if (prefLabel == null) {
                // This concept has no prefLabel. It will be sorted
                // after all concepts that _do_ have prefLabels.
                if (otherConcept.prefLabel == null) {
                    // Both concepts have null prefLabels, so
                    // sort by their IRIs.
                    return iri.compareTo(otherConcept.iri);
                }
                // The other concept has a prefLabel. This concept
                // is sorted after it.
                return 1;
            }
            // This concept has a prefLabel.
            if (otherConcept.prefLabel == null) {
                // The other concept doesn't have a prefLabel. It is
                // sorted after this concept.
                return -1;
            }
            // Both this and otherConcept have prefLabels.
            // Use String case-insensitive comparison on them.
            int prefLabelComparison =
                    prefLabel.compareToIgnoreCase(otherConcept.prefLabel);
            if (prefLabelComparison != 0) {
                return prefLabelComparison;
            }
            // Identical prefLabels. Fall back to comparing their IRIs.
            return iri.compareTo(otherConcept.iri);
        }

        /** {@inheritDoc}
         * Equality test based on IRI. But there should be only one
         * instance of an IRI in a Set or Map of Concepts.
         */
        @Override
        public boolean equals(final Object other) {
            if (other == null || !(other instanceof Concept)) {
                return false;
            }
            Concept otherConcept = (Concept) other;
            return iri.equals(otherConcept.iri);
        }

        /** {@inheritDoc}
         * The hash code returned is that of the IRI.
         */
        @Override
        public int hashCode() {
            return iri.hashCode();
        }

        /** {@inheritDoc}
         * Generate String representation based on the IRI.
         */
        @Override
        public String toString() {
            return iri;
        }

    }

    /** RDF Handler to extract prefLabels, notation, definition,
     * and use broader and narrow properties to construct a tree-like
     * structure.
     * Note the use of both HashMaps and TreeSets.
     * TreeSets are used when the sorting of keys is required
     * during serialization;
     * in this case, the values are Concepts.
     * HashMaps are used when the sorting of keys is not required;
     * in this case, the keys are Strings ("prefLabel", "notation",
     * etc.)
     * Note well that all TreeSets are constructed only <i>after</i>
     * all the RDF data has been parsed, so that all {@link Concept}
     * instances have their prefLabel data set, so that insertion
     * of the subsequently generated {@link Concept} instances
     * into a TreeSet (which is based on the
     * {@link Concept#compareTo(JsonTreeTransformProvider.Concept)}
     * method) will work correctly. */
    class ConceptHandler extends RDFHandlerBase {

        /** Map from concept IRI to Concept object,
         * used as a cache of all Concept objects. This Map
         * is maintained by {@link #getConcept(String)}, whose
         * body contains the only invocation of the constructor of the
         * Concept class.
         */
        private Map<String, Concept> iriConceptMap =
                new HashMap<String, Concept>();

        /** Map from concept IRI to a map that maps
         * property name to the property value(s).
         * Used during parsing to collect all of the concepts.
         * This can be a HashMap (e.g., rather than a TreeMap),
         * because we are not sorting at this
         * stage. Sorting happens during {@link #buildTree()}.
         * The values of the map are themselves maps.
         * Those maps have keys which are
         * Strings: either "type", "broader", or "narrower".
         * (The keys "prefLabel", "notation", "definition" are
         * not used for now; they may come back, if it is desired
         * to represent values for resources other than SKOS Concepts.)
         * The values depend on what the keys are
         * (hence, the formal type is Object). For key "type"
         * (and "prefLabel", "notation", and "definition", if supported),
         * the actual type will be String;
         * for keys "broader", "narrower", the actual type will be
         * {@code Set<Concept>}.
         * */
        private Map<Concept, HashMap<String, Object>> conceptMap =
                new HashMap<Concept, HashMap<String, Object>>();

        /** The top-most concepts of the vocabulary. This is based on
         * finding all concepts that do not have a broader concept.
         * This is used in the first stage of {@link #buildTree()}
         * to collect the top-most concepts. It can be a HashMap
         * (e.g., rather than a TreeMap), because its contents are iterated
         * over to produce the Set that is actually returned by
         * {@link #buildTree()}. */
        private Map<Concept, HashMap<String, Object>> topmostConcepts =
                new HashMap<Concept, HashMap<String, Object>>();

        /** Get the Concept object for an IRI from the iriConceptMap
         * cache. Create such an object and add it to the cache,
         * if it is not already there.
         * @param iri The IRI to look up.
         * @return The Concept for this IRI.
         */
        private Concept getConcept(final String iri) {
            Concept concept = iriConceptMap.get(iri);
            if (concept == null) {
                concept = new Concept(iri);
                iriConceptMap.put(iri, concept);
            }
            return concept;
        }

        /** When either a broader or narrower triple is encountered,
         * keep track of that relationship and infer its inverse.
         * @param parent The parent Concept.
         * @param child The child Concept.
         */
        private void addBroaderNarrower(final Concept parent,
                final Concept child) {
            HashMap<String, Object> parentConcept;
            HashMap<String, Object> childConcept;

            if (conceptMap.get(parent) == null) {
                parentConcept = conceptMap.put(parent,
                        new HashMap<String, Object>());
            }
            parentConcept = conceptMap.get(parent);

            if (conceptMap.get(child) == null) {
                childConcept = conceptMap.put(child,
                        new HashMap<String, Object>());
            }
            childConcept = conceptMap.get(child);

            if (parentConcept.get("narrower") == null) {
                parentConcept.put("narrower",
                        new HashSet<Concept>());
            }
            @SuppressWarnings("unchecked")
            HashSet<Concept> narrowerSet =
                    (HashSet<Concept>) parentConcept.get("narrower");
            narrowerSet.add(child);

            if (childConcept.get("broader") == null) {
                childConcept.put("broader",
                        new HashSet<Concept>());
            }
            @SuppressWarnings("unchecked")
            HashSet<Concept> broaderSet =
                    (HashSet<Concept>) childConcept.get("broader");
            broaderSet.add(parent);
        }

        @Override
        public void handleStatement(final Statement st) {
            Concept subjectConcept = getConcept(st.getSubject().stringValue());
            if (conceptMap.get(subjectConcept) == null) {
                conceptMap.put(subjectConcept,
                        new HashMap<String, Object>());
            }
            HashMap<String, Object> concept =
                    conceptMap.get(subjectConcept);
            if (st.getPredicate().equals(RDF.TYPE)) {
                Value typeIRI = st.getObject();
                if (typesToLookFor.containsKey(typeIRI)) {
                    concept.put("type", typesToLookFor.get(typeIRI));
                }
            }
            if (st.getPredicate().equals(SKOS.PREF_LABEL)) {
                // Don't need need to do this, since for now
                // we are only processing SKOS Concepts. If we later
                // somehow wish to support prefLabels on things
                // other than Concepts, uncomment/modify as needed.
                // concept.put("prefLabel", st.getObject().stringValue());
                subjectConcept.setPrefLabel(st.getObject().stringValue());
            }
            // Future work: uncomment/modify the next six lines
            // when the portal is ready to receive it.
//            if (st.getPredicate().equals(SKOS.ALT_LABEL)) {
//                concept.put("altLabel", st.getObject().stringValue());
//            }
//            if (st.getPredicate().equals(SKOS.HIDDEN_LABEL)) {
//                concept.put("hiddenLabel", st.getObject().stringValue());
//            }
            if (st.getPredicate().equals(SKOS.NOTATION)) {
                // Don't need need to do this, since for now
                // we are only processing SKOS Concepts. If we later
                // somehow wish to support notations on things
                // other than Concepts, uncomment/modify as needed.
                // concept.put("notation", st.getObject().stringValue());
                subjectConcept.setNotation(st.getObject().stringValue());
            }
            if (st.getPredicate().equals(SKOS.DEFINITION)) {
                // Don't need need to do this, since for now
                // we are only processing SKOS Concepts. If we later
                // somehow wish to support definitions on things
                // other than Concepts, uncomment/modify as needed.
                // concept.put("definition", st.getObject().stringValue());
                subjectConcept.setDefinition(st.getObject().stringValue());
            }
            // The (absence of a) broader relationship is used to identify
            // top concepts.
            if (st.getPredicate().equals(SKOS.BROADER)) {
                addBroaderNarrower(getConcept(st.getObject().stringValue()),
                        subjectConcept);
            }
            if (st.getPredicate().equals(SKOS.NARROWER)) {
                addBroaderNarrower(subjectConcept,
                        getConcept(st.getObject().stringValue()));
            }
            // Future work: uncomment the next ten lines when work begins
            // on handling collections. NB: this code doesn't reflect
            // the "new" data structures; it will need to be updated.
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
         *  @return The tree of concepts, represented as a TreeSet.
         *  The values are Concepts. Each value represents
         *  one concept and each of its narrower concepts.
         */
        @SuppressWarnings("unchecked")
        public TreeSet<Concept> buildTree() {
            // This is a rearranged version of conceptMap, with
            // the concepts arranged in a tree structure based on
            // the broader/narrower relations.
            TreeSet<Concept> conceptSet =
                    new TreeSet<Concept>();
            populateTopConcepts();
            for (Entry<Concept, HashMap<String, Object>> topmostConcept
                    : topmostConcepts.entrySet()) {
                if (topmostConcept.getValue().containsKey("narrower")) {
                    Set<Concept> narrowerList =
                            (Set<Concept>)
                            topmostConcept.getValue().get("narrower");
                    // Replace the narrower HashSet with
                    // the tree structure of narrower concepts.
                    TreeSet<Concept> narrowerSet =
                            new TreeSet<Concept>();
                    topmostConcept.getKey().setNarrower(narrowerSet);
                    for (Concept narrower : narrowerList) {
                        buildNarrower(narrower);
                        narrowerSet.add(narrower);
                    }
                }


                conceptSet.add(topmostConcept.getKey());
            }
            return conceptSet;
        }

        /** Resolve all narrower concepts of a concept.
         * NB: This method is recursive.
         * @param concept The Concept to have its narrower Concepts filled out.
         */
        private void buildNarrower(final Concept concept) {
            if (conceptMap.get(concept) == null) {
                logger.error("buildNarrower Orphan key: " + concept);
                return;
            }
            HashMap<String, Object> narrowerConcepts = conceptMap.get(concept);
            if (narrowerConcepts.containsKey("narrower")) {
                // The fact that we are dealing with a HashSet means
                // we only come here once. With the previous implementation
                // based on ArrayLists, this cast would fail if there
                // were two instances of an IRI in the same narrower list
                // (which would happen if the input RDF contained duplicate
                // triples).
                @SuppressWarnings("unchecked")
                Set<Concept> narrowerList =
                        (Set<Concept>)
                        narrowerConcepts.get("narrower");
                // Replace the narrower HashSet with
                // the tree structure of narrower concepts.
                TreeSet<Concept> narrowerSet =
                        new TreeSet<Concept>();
                concept.setNarrower(narrowerSet);

                for (Concept narrower : narrowerList) {
                    buildNarrower(narrower);
                    narrowerSet.add(narrower);
                }
            }
        }

        /** Populate the top-most concepts.
         * A concept is considered to be "top-most" if it is a SKOS Concept
         * and it
         * does not specify any broader concepts.
         * This (probably) catches both concepts explicitly
         * labelled as top concepts, and also any "dangling"
         * concepts.
         */
        private void populateTopConcepts() {
            for (Entry<Concept, HashMap<String, Object>>
            concept : conceptMap.entrySet()) {
                HashMap<String, Object> propertyMap = concept.getValue();
                if (!propertyMap.isEmpty()
                        && CONCEPT_SHORT_FORM.equals(propertyMap.get("type"))
                        && propertyMap.get("broader") == null) {
                    topmostConcepts.put(concept.getKey(), propertyMap);
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
