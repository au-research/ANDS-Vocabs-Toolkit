package au.org.ands.vocabs.toolkit.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Utility methods for working with files. */
public final class ToolkitFileUtils {

//    /** Logger for this class. */
//    private static Logger logger;

    /** Private contructor for utility class. */
    private ToolkitFileUtils() {
    }

//    static {
//        logger = LoggerFactory.getLogger(
//                MethodHandles.lookup().lookupClass());
//    }

    /** Require the existence of a directory. Create it, if it
     * does not already exist.
     * @param dir The full pathname of the required directory.
     */
    public static void requireDirectory(final String dir) {
        File oDir = new File(dir);
        if (!oDir.exists()) {
            oDir.mkdirs();
        }
    }

    /** Save data to a file.
     * @param dirName The full directory name
     * @param fileName The base name of the file to create
     * @param format The format to use; a key in
     *  ToolkitConfig.FORMAT_TO_FILEEXT_MAP.
     * @param data The data to be written
     * @return The complete, full path to the file.
     */
    public static String saveFile(final String dirName, final String fileName,
            final String format, final String data) {
        String fileExtension =
                ToolkitConfig.FORMAT_TO_FILEEXT_MAP.get(format.toLowerCase());
        String filePath = dirName
                + File.separator + fileName + fileExtension;
        FileWriter writer = null;
        try {
            requireDirectory(dirName);
            File oFile = new File(filePath);
            writer = new FileWriter(oFile);
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            return "Exception: " + e.toString();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    return "Exception: " + e.toString();
                }
            }
        }
        return filePath;
    }


}
