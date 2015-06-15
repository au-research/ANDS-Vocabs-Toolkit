package au.org.ands.vocabs.toolkit.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        return path.toString().toLowerCase();
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
        return path.toString().toLowerCase();
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
        return path.toString().toLowerCase();
    }

    /** Apply slug conventions. In practice, this means replacing
     * whitespace with hyphen.
     * @param aString The string that is to be converted.
     * @return The value of aString with slug conventions applied.
     */
    public static String makeSlug(final String aString) {
        return aString.replaceAll("\\s", "-");
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
        return (UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getOwner()),
                        UriComponent.Type.PATH_SEGMENT)
                + "_"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getSlug()),
                        UriComponent.Type.PATH_SEGMENT)
                + "_"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVersion().getTitle()),
                        UriComponent.Type.PATH_SEGMENT)).toLowerCase();
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
        return (UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getOwner()),
                        UriComponent.Type.PATH_SEGMENT)
                + "/"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVocabulary().getSlug()),
                        UriComponent.Type.PATH_SEGMENT)
                + "/"
                + UriComponent.encode(
                        makeSlug(taskInfo.getVersion().getTitle()),
                        UriComponent.Type.PATH_SEGMENT)).toLowerCase();
    }


}
