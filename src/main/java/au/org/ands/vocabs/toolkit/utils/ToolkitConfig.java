package au.org.ands.vocabs.toolkit.utils;

import java.nio.file.Paths;
import java.util.Hashtable;


/** Utility class providing access to toolkit properties. */
public final class ToolkitConfig {

    //    /** Logger. */
    //    private static Logger logger;

    /** Subdirectory of the data directory used to store harvested data. */
    public static final String HARVEST_DATA_PATH = "harvest_data";

    /** This is a utility class. No instantiation. */
    private ToolkitConfig() {
    }

    //    static {
    //        logger = LoggerFactory.getLogger(
    //                MethodHandles.lookup().lookupClass());
    //    }

    /** Path to the default top-level output directory used to store files. */
    public static final String ROOT_FILES_PATH =
            ToolkitProperties.getProperty("Toolkit.storagePath",
                    "/tmp/vocabs");


    /** Path to the directory used to store vocabulary files. */
    public static final String DATA_FILES_PATH =
            ToolkitProperties.getProperty("Toolkit.vocabsPath",
                    "/tmp/vocabs");

    /** Path to the directory used to store temporary files. */
    public static final String TEMP_FILES_PATH =
            ToolkitProperties.getProperty("Toolkit.tempPath",
                    "/tmp/vocabs/temp");

    /** Path to the directory used to store temporary files. */
    public static final String METADATA_TEMP_FILES_PATH =
            Paths.get(TEMP_FILES_PATH).resolve("metadata").toString();


    /** Path to the directory used to store backup files. */
    public static final String BACKUP_FILES_PATH =
            Paths.get(ROOT_FILES_PATH).resolve("backup").toString();



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
