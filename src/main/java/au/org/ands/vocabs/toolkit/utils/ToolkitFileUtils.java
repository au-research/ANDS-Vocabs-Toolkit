/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.uri.UriComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;

/** Utility methods for working with files. */
public final class ToolkitFileUtils {

    /** Logger for this class. */
    private static Logger logger;

    /** Maximum length of a slug component. All generated slug
     * components are truncated to this length.
     */
    private static final int MAX_SLUG_COMPONENT_LENGTH = 50;

    /** Private constructor for utility class. */
    private ToolkitFileUtils() {
    }

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

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

    /** Require the existence of a directory, but clean it out if
     * it already exists. Create it, if it does not already exist.
     * @param dir The full pathname of the required directory.
     * @throws IOException If the directory already exists but can
     * not be cleaned.
     */
    public static void requireEmptyDirectory(final String dir)
            throws IOException {
        File oDir = new File(dir);
        if (!oDir.exists()) {
            oDir.mkdirs();
        } else {
            try {
                FileUtils.cleanDirectory(new File(dir));
            } catch (IOException e) {
                logger.error("requireEmptyDirectory failed: ", e);
                throw e;
            }
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

    /** Construct a TaskInfo object based on a task id.
     * @param taskId The task's task id
     * @return The TaskInfo object
     */
    public static TaskInfo getTaskInfo(final int taskId) {
        Task task = TasksUtils.getTaskById(taskId);
        if (task == null) {
            logger.error("getTaskInfo: getTaskById returned null; task id:"
                    + taskId);
            return null;
        }
        Vocabularies vocab = TasksUtils.getVocabularyById(
                task.getVocabularyId());
        if (vocab == null) {
            logger.error("getTaskInfo: getVocabularyById returned null; "
                    + "task id:"
                    + taskId + "; vocab id:" + task.getVocabularyId());
            return null;
        }
        Versions version = TasksUtils.getVersionById(task.getVersionId());
        if (version == null) {
            logger.error("getTaskInfo: getVersionById returned null; "
                    + "task id:"
                    + taskId + "; version id:" + task.getVersionId());
            return null;
        }
        if (!version.getVocabId().equals(task.getVocabularyId())) {
            logger.error("getTaskInfo: task's vocab id does not match"
                    + " task's version's vocab id; "
                    + "task id:"
                    + taskId + "; vocab id:" + task.getVocabularyId()
                    + "; version's vocab id:" + version.getVocabId());
            return null;
        }
        if (vocab.getSlug() == null || vocab.getSlug().trim().isEmpty()) {
            logger.error("getTaskInfo: vocab's slug is empty; "
                    + "task id:"
                    + taskId + "; vocab id:" + task.getVocabularyId());
            return null;
        }
        if (vocab.getOwner() == null || vocab.getOwner().trim().isEmpty()) {
            logger.error("getTaskInfo: vocab's owner is empty; "
                    + "task id:"
                    + taskId + "; vocab id:" + task.getVocabularyId());
            return null;
        }
        if (version.getTitle() == null || version.getTitle().trim().isEmpty()) {
            logger.error("getTaskInfo: version's title is empty; "
                    + "task id:"
                    + taskId + "; version id:" + task.getVersionId());
            return null;
        }

        TaskInfo taskInfo = new TaskInfo(task, vocab, version);
        return taskInfo;
    }

    /** Get the full path of the directory used to store all
     * the files referred to by the task.
     * @param taskInfo The TaskInfo object representing the task.
     * @param extraPath An optional additional path component to be added
     * at the end. If not required, pass in null or an empty string.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getTaskOutputPath(final TaskInfo taskInfo,
            final String extraPath) {
        // NB: We call makeSlug() on the vocabulary slug, which should
        // (as of ANDS-Registry-Core commit e365392831ae)
        // not really be necessary.
        Path path = Paths.get(ToolkitConfig.DATA_FILES_PATH)
                .resolve(makeSlug(taskInfo.getVocabulary().getOwner()))
                .resolve(makeSlug(taskInfo.getVocabulary().getSlug()))
                .resolve(makeSlug(taskInfo.getVersion().getTitle()));
        if (extraPath != null && (!extraPath.isEmpty())) {
            path = path.resolve(extraPath);
        }
        return path.toString();
    }

    /** Get the full path of the directory used to store all
     * harvested data referred to by the task.
     * @param taskInfo The TaskInfo object representing the task.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getTaskHarvestOutputPath(final TaskInfo taskInfo) {
        return getTaskOutputPath(taskInfo, ToolkitConfig.HARVEST_DATA_PATH);
    }

    /** Get the full path of (what will be) a new directory used to store
     * transformed data referred to by the task. This is intended
     * to be used as a temporary directory during the transform.
     * If the transform succeeds, call renameTransformTemporaryOutputPath()
     * to rename this directory to become the harvest directory.
     * @param taskInfo The TaskInfo object representing the task.
     * @param transformName The name of the transform being done. This is
     * used in the generation of the path.
     * @return The full path of the directory used to store the
     * transformed data. The directory does not yet exist; it must be
     * created by the caller.
     */
    public static String getTaskTransformTemporaryOutputPath(
            final TaskInfo taskInfo,
            final String transformName) {
        return getTaskOutputPath(taskInfo, "after_" + transformName);
    }

    /** This method is used by transforms that produce new vocabulary
     * data to replace harvested data. If such a transform succeeds,
     * call this method. It renames the original harvest directory, and
     * then renames the temporary directory to become the harvest directory.
     * @param taskInfo The TaskInfo object representing the task.
     * @param transformName The name of the transform that has been done.
     * @return True iff the renaming succeeded.
     */
    public static boolean renameTransformTemporaryOutputPath(
            final TaskInfo taskInfo,
            final String transformName) {
        Path transformOutputPath =
                Paths.get(getTaskOutputPath(taskInfo,
                        "after_" + transformName));
        Path harvestPath =
                Paths.get(getTaskHarvestOutputPath(taskInfo));
        Path harvestPathDestination =
                Paths.get(getTaskOutputPath(taskInfo,
                        "before_" + transformName));
        try {
            // Remove any previous harvestPathDestination
            FileUtils.deleteQuietly(harvestPathDestination.toFile());
            Files.move(harvestPath, harvestPathDestination);
            Files.move(transformOutputPath, harvestPath);
        } catch (IOException e) {
            logger.error("Exception in renameTransformTemporaryOutputPath", e);
            return false;
        }
        return true;
    }

    /** Get the full path of the temporary directory used to store all
     * harvested data for metadata extraction for a PoolParty vocabulary.
     * @param projectId The PoolParty projectId.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getMetadataOutputPath(final String projectId) {
        Path path = Paths.get(ToolkitConfig.METADATA_TEMP_FILES_PATH)
                .resolve(makeSlug(projectId));
        return path.toString();
    }

    /** Get the full path of the backup directory used to store all
     * backup data for a project.
     * @param projectId The project ID. For now, this will be a PoolParty
     * project ID.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getBackupPath(final String projectId) {
        Path path = Paths.get(ToolkitConfig.BACKUP_FILES_PATH)
                .resolve(makeSlug(projectId));
        return path.toString();
    }

    /** Apply slug conventions. In practice, this means
     * (a) replacing punctuation with hyphens,
     * (b) replacing whitespace with hyphen,
     * (c) converting to lowercase,
     * (d) encoding as a URL,
     * (e) replacing percents with hyphens,
     * (f) coalescing multiple consecutive hyphens into one,
     * (g) removing any leading and trailing hyphens,
     * (h) trimming the result to a maximum length of
     *     MAX_SLUG_COMPONENT_LENGTH,
     * (i) removing any remaining trailing hyphen.
     * @param aString The string that is to be converted.
     * @return The value of aString with slug conventions applied.
     */
    public static String makeSlug(final String aString) {
        String slug = StringUtils.strip(
                UriComponent.encode(aString.
                replaceAll("\\p{Punct}", "-").
                replaceAll("\\s", "-").
                toLowerCase(),
                UriComponent.Type.PATH_SEGMENT).
                replaceAll("%", "-").
                replaceAll("-+", "-"),
                "-");

        return StringUtils.stripEnd(
                slug.substring(0, Math.min(MAX_SLUG_COMPONENT_LENGTH,
                slug.length())),
                "-");
    }

    /**
     * Get the Sesame repository ID for a vocabulary's version
     * referred to by the task.
     *
     * @param taskInfo
     *            The TaskInfo object representing the task.
     * @return The repository id for the vocabulary with this version.
     */
    public static String getSesameRepositoryId(final TaskInfo taskInfo) {
        // As of ANDS-Registry-Core commit e365392831ae,
        // now use the vocabulary title slug directly from the database.
        return makeSlug(taskInfo.getVocabulary().getOwner())
                + "_"
                + taskInfo.getVocabulary().getSlug()
                + "_"
                + makeSlug(taskInfo.getVersion().getTitle());
    }

    /**
     * Get the SISSVoc repository ID for a vocabulary's version
     * referred to by the task. It neither begins nor ends with a slash.
     *
     * @param taskInfo
     *            The TaskInfo object representing the task.
     * @return The repository id for the vocabulary with this version.
     */
    public static String getSISSVocRepositoryPath(final TaskInfo taskInfo) {
        // As of ANDS-Registry-Core commit e365392831ae,
        // now use the vocabulary title slug directly from the database.
        return makeSlug(taskInfo.getVocabulary().getOwner())
                + "/"
                + taskInfo.getVocabulary().getSlug()
                + "/"
                + makeSlug(taskInfo.getVersion().getTitle());
    }

    /** Size of buffer to use when writing to a ZIP archive. */
    private static final int BUFFER_SIZE = 4096;

    /** Add a file to a ZIP archive.
     * @param zos The ZipOutputStream representing the ZIP archive.
     * @param file The File which is to be added to the ZIP archive.
     * @return True if adding succeeded.
     * @throws IOException Any exception when reading/writing data.
     */
    private static boolean zipFile(final ZipOutputStream zos, final File file)
            throws IOException {
        if (!file.canRead()) {
            logger.error("zipFile can not read " + file.getCanonicalPath());
            return false;
        }
        zos.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[BUFFER_SIZE];
        int byteCount = 0;
        while ((byteCount = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, byteCount);
        }
        fis.close();
        zos.closeEntry();
        return true;
    }

    /** Compress the files in the backup folder for a project.
     * @param projectId The project ID
     * @throws IOException Any exception when reading/writing data.
     */
    public static void compressBackupFolder(final String projectId)
            throws IOException {
        String backupPath = getBackupPath(projectId);
        if (!Files.isDirectory(Paths.get(backupPath))) {
            // No such directory, so nothing to do.
            return;
        }
        String projectSlug = makeSlug(projectId);
        // The name of the ZIP file that does/will contain all
        // backups for this project.
        Path zipFilePath = Paths.get(backupPath).resolve(projectSlug + ".zip");
        // A temporary ZIP file. Any existing content in the zipFilePath
        // will be copied into this, followed by any other files in
        // the directory that have not yet been added.
        Path tempZipFilePath = Paths.get(backupPath).resolve("temp" + ".zip");

        File tempZipFile = tempZipFilePath.toFile();
        if (!tempZipFile.exists()) {
            tempZipFile.createNewFile();
        }

        ZipOutputStream tempZipOut = new ZipOutputStream(
                new FileOutputStream(tempZipFile));

        File existingZipFile = zipFilePath.toFile();
        if (existingZipFile.exists()) {
            ZipFile zipIn = new ZipFile(existingZipFile);

            Enumeration<? extends ZipEntry> entries = zipIn.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                logger.debug("compressBackupFolder copying: " + e.getName());
                tempZipOut.putNextEntry(e);
                if (!e.isDirectory()) {
                    copy(zipIn.getInputStream(e), tempZipOut);
                }
                tempZipOut.closeEntry();
            }
            zipIn.close();
        }

        File dir = new File(backupPath);
        File[] files = dir.listFiles();

        for (File source : files) {
            if (!source.getName().toLowerCase().endsWith(".zip")) {
                logger.debug("compressBackupFolder compressing and "
                        + "deleting file: "
                        + source.toString());
                if (zipFile(tempZipOut, source)) {
                    source.delete();
                }
            }
        }

        tempZipOut.flush();
        tempZipOut.close();
        tempZipFile.renameTo(existingZipFile);
    }

    /** Size of buffer to use for copying files. */
    private static final int COPY_BUFFER_SIZE = 4096 * 1024;

    /** Copy the contents of an InputStream into an OutputStream.
     * @param input The content to be copied.
     * @param output The destination of the content being copied.
     * @throws IOException Any IOException during read/write.
     */
    public static void copy(final InputStream input,
            final OutputStream output) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[COPY_BUFFER_SIZE];

        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }


}
