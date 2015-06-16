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

    /** Private contructor for utility class. */
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
        TaskInfo taskInfo = new TaskInfo(task, vocab, version);
        if (version.getVocabId() != task.getVocabularyId()) {
            logger.error("getTaskInfo: version's vocab id does not match"
                    + " task's version id; "
                    + "task id:"
                    + taskId + "; version id:" + task.getVersionId());
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
        Path path = Paths.get(ToolkitConfig.DATA_FILES_PATH)
                .resolve(UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getOwner()),
                        UriComponent.Type.PATH_SEGMENT))
                .resolve(UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getSlug()),
                        UriComponent.Type.PATH_SEGMENT))
                .resolve(UriComponent.encode(
                        makeSlug(taskInfo.getVersion().getTitle()),
                        UriComponent.Type.PATH_SEGMENT));
        if (extraPath != null && (!extraPath.isEmpty())) {
            path = path.resolve(extraPath);
        }
        return path.toString();
    }

    /** Get the full path of the directory used to store all
     * harvested data referred to by the task.
     * @param taskInfo The TaskInfo object representing the task.
     * at the end. If not required, pass in null or an empty string.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getTaskHarvestOutputPath(final TaskInfo taskInfo) {
        return getTaskOutputPath(taskInfo, ToolkitConfig.HARVEST_DATA_PATH);
    }

    /** Get the full path of the temporary directory used to store all
     * harvested data for metadata extraction for a PoolParty vocabulary.
     * @param projectId The PoolParty projectId.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getMetadataOutputPath(final String projectId) {
        Path path = Paths.get(ToolkitConfig.METADATA_TEMP_FILES_PATH)
                .resolve(UriComponent.encode(
                        ToolkitFileUtils.makeSlug(projectId),
                        UriComponent.Type.PATH_SEGMENT));
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
                .resolve(UriComponent.encode(
                        ToolkitFileUtils.makeSlug(projectId),
                        UriComponent.Type.PATH_SEGMENT));
        return path.toString();
    }

    /** Apply slug conventions. In practice, this means (a) replacing
     * whitespace with hyphen, (b) converting to lowercase.
     * @param aString The string that is to be converted.
     * @return The value of aString with slug conventions applied.
     */
    public static String makeSlug(final String aString) {
        return aString.replaceAll("\\s", "-").toLowerCase();
    }

    /**
     * Get the Sesame repository ID for a vocabulary's version
     * referred to by the task.
     *
     * @param taskInfo
     *            The TaskInfo object representing the task.
     * @return The repository id for the vocabulary with this version.
     */
    public static String getTaskRepositoryId(final TaskInfo taskInfo) {
        return UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getOwner()),
                        UriComponent.Type.PATH_SEGMENT)
                + "_"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getSlug()),
                        UriComponent.Type.PATH_SEGMENT)
                + "_"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVersion().getTitle()),
                        UriComponent.Type.PATH_SEGMENT);
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
        return UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getOwner()),
                        UriComponent.Type.PATH_SEGMENT)
                + "/"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getSlug()),
                        UriComponent.Type.PATH_SEGMENT)
                + "/"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVersion().getTitle()),
                        UriComponent.Type.PATH_SEGMENT);
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
        String projectSlug = UriComponent.encode(
                ToolkitFileUtils.makeSlug(projectId),
                UriComponent.Type.PATH_SEGMENT);
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
                System.out.println("copy: " + e.getName());
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
                logger.debug("Compressing and deleting file "
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
