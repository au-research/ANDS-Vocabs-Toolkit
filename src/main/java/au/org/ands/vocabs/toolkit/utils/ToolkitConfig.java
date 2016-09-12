/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.nio.file.Paths;
import java.util.Hashtable;

/** Utility class providing access to toolkit properties. */
public final class ToolkitConfig {

    /** This is a utility class. No instantiation. */
    private ToolkitConfig() {
    }

    // If it is necessary to debug instantiation of this class,
    // uncomment the following, and add some logging in a static
    // block.
    //    /** Logger. */
    //    private static Logger logger;

    //    static {
    //        logger = LoggerFactory.getLogger(
    //                MethodHandles.lookup().lookupClass());
    //    }

    /** Subdirectory of the data directory used to store harvested data. */
    public static final String HARVEST_DATA_PATH = "harvest_data";

    /** Path to the default top-level output directory used to store files. */
    public static final String ROOT_FILES_PATH =
            ToolkitProperties.getProperty(PropertyConstants.TOOLKIT_STORAGEPATH,
                    "/tmp/vocabs");

    /** Path to the directory used to store vocabulary files. */
    public static final String DATA_FILES_PATH =
            ToolkitProperties.getProperty(PropertyConstants.TOOLKIT_VOCABSPATH,
                    "/tmp/vocabs");

    /** Path to the directory used to store temporary files. */
    public static final String TEMP_FILES_PATH =
            ToolkitProperties.getProperty(PropertyConstants.TOOLKIT_TEMPPATH,
                    "/tmp/vocabs/temp");

    /** Path to the directory used to store temporary files. */
    public static final String METADATA_TEMP_FILES_PATH =
            Paths.get(TEMP_FILES_PATH).resolve("metadata").toString();

    /** Path to the directory used to store backup files. */
    public static final String BACKUP_FILES_PATH =
            Paths.get(ROOT_FILES_PATH).resolve("backup").toString();

    /** Mapping of (PoolParty) formats to filename extensions. */
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
