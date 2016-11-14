/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.utils;

import java.util.Hashtable;
import java.util.Locale;

import org.openrdf.rio.RDFFormat;

/** Utility methods for working with RDF data. */
public final class RDFUtils {

    /** This is a utility class. No instantiation. */
    private RDFUtils() {
    }

    /** Mapping of (PoolParty) formats to filename extensions. */
    public static final Hashtable<String, String> FORMAT_TO_FILEEXT_MAP =
            new Hashtable<String, String>();

    static {
        FORMAT_TO_FILEEXT_MAP.put("rdf/xml", ".rdf");
        FORMAT_TO_FILEEXT_MAP.put("trig", ".trig");
        FORMAT_TO_FILEEXT_MAP.put("trix", ".trix");
        FORMAT_TO_FILEEXT_MAP.put("turtle", ".ttl");
        FORMAT_TO_FILEEXT_MAP.put("n3", ".n3");
        FORMAT_TO_FILEEXT_MAP.put("n-triples", ".nt");
    }

    /** Mapping of (PoolParty) formats to RDFFormats. */
    private static final Hashtable<String, RDFFormat> FORMAT_TO_RDFFORMAT_MAP =
            new Hashtable<String, RDFFormat>();

    static {
        FORMAT_TO_RDFFORMAT_MAP.put("rdf/xml", RDFFormat.RDFXML);
        FORMAT_TO_RDFFORMAT_MAP.put("trig", RDFFormat.TRIG);
        FORMAT_TO_RDFFORMAT_MAP.put("trix", RDFFormat.TRIX);
        FORMAT_TO_RDFFORMAT_MAP.put("turtle", RDFFormat.TURTLE);
        FORMAT_TO_RDFFORMAT_MAP.put("n3", RDFFormat.N3);
        FORMAT_TO_RDFFORMAT_MAP.put("n-triples", RDFFormat.NTRIPLES);
    }

    /** Get the RDFFormat for an RDF format name. Throws a
     * NullPointerException if there is no such format.
     * @param name The name of the RDF format, e.g., "TriG, "Turtle".
     *      Names are converted to lower-case before lookup, so there
     *      is no need to apply case conversion before invoking this method.
     * @return The RDFFormat corresponding to the name.
     */
    public static RDFFormat getRDFFormatForName(final String name) {
        return FORMAT_TO_RDFFORMAT_MAP.get(name.toLowerCase(Locale.ROOT));
    }

}
