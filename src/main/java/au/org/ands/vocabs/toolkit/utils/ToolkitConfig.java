package au.org.ands.vocabs.toolkit.utils;

import java.util.Hashtable;


/** Utility class providing access to toolkit properties. */
public final class ToolkitConfig {

    //    /** Logger. */
    //    private static Logger logger;

    /** This is a utility class. No instantiation. */
    private ToolkitConfig() {
    }

    //    static {
    //        logger = LoggerFactory.getLogger(
    //                MethodHandles.lookup().lookupClass());
    //    }

    /** Path to the directory used to store vocabulary files. */
    public static final String DATA_FILES_PATH =
            ToolkitProperties.getProperty("HarvesterHandler.dataPath",
                    "/temp");

    /** Mapping of (PoolParty) formats to filename extensions . */
    public static final Hashtable<String, String>
    FORMAT_TO_FILEEXT_MAP =
    new Hashtable<String, String>();

    static {
        FORMAT_TO_FILEEXT_MAP.put("rdf/xml", ".rdf");
        FORMAT_TO_FILEEXT_MAP.put("trig", ".trig");
        FORMAT_TO_FILEEXT_MAP.put("trix", ".trix");
        FORMAT_TO_FILEEXT_MAP.put("turtle", ".ttl");
        FORMAT_TO_FILEEXT_MAP.put("n3", ".ttl");
        FORMAT_TO_FILEEXT_MAP.put("n-triples", ".nt");
    }

}
