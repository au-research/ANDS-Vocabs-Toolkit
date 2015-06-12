package au.org.ands.vocabs.toolkit.db;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.glassfish.jersey.uri.UriComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/** Work with database tasks. */
public final class TasksUtils {

    /** Logger for this class. */
    private static Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private TasksUtils() {
    }

    /** Get a task by ID.
     * @param id task id
     * @return The task
     */
    public static Task getTaskById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Task t = em.find(Task.class, id);
        em.close();
        return t;
    }

    /** Get all tasks.
     * @return A list of Tasks
     */
    @SuppressWarnings("unchecked")
    public static List<Task> getAllTasks() {
        EntityManager em = DBContext.getEntityManager();
        Query query = em.createNamedQuery(Task.GET_ALL_TASKS);
        List<Task> tasks = query.getResultList();
        em.close();
        return tasks;
    }


    /** Get a vocabulary by ID.
     * @param id vocabulary id
     * @return The Vocabularies object
     */
    public static Vocabularies getVocabularyById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Vocabularies v = em.find(Vocabularies.class, id);
        em.close();
        return v;
    }

    /** Get a version by ID.
     * @param id version id
     * @return The Versions object
     */
    public static Versions getVersionById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Versions v = em.find(Versions.class, id);
        em.close();
        return v;
    }


    /** Construct a TaskInfo object based on a task id.
     * @param taskId The task's task id
     * @return The TaskInfo object
     */
    public static TaskInfo getTaskInfo(final int taskId) {
        Task task = getTaskById(taskId);
        if (task == null) {
            logger.error("getTaskInfo: getTaskById returned null; task id:"
                    + taskId);
            return null;
        }
        Vocabularies vocab = getVocabularyById(task.getVocabularyId());
        if (vocab == null) {
            logger.error("getTaskInfo: getVocabularyById returned null; "
                    + "task id:"
                    + taskId + "; vocab id:" + task.getVocabularyId());
            return null;
        }
        Versions version = getVersionById(task.getVersionId());
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

    /** Set the status and data fields for a task.
     * @param task The task being updated
     * @param status The updated status information
     * @param response The updated response
     */
    public static void setTaskStatusAndData(final Task task,
            final String status, final String response) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        task.setStatus(status);
        task.setResponse(response);
        em.merge(task);
        em.getTransaction().commit();
        em.close();
    }

    /** Update both message and task status.
     * @param callerLogger The Logger to use.
     * @param task The Task object.
     * @param results The HashMap of results.
     * @param status Status (SUCCESS, ERROR, ...)
     * @param details Detailed message data
     */
    public static void updateMessageAndTaskStatus(final Logger callerLogger,
            final Task task, final HashMap<String, String> results,
            final String status, final String details) {
        if (TaskStatus.ERROR.equals(status)
                || TaskStatus.EXCEPTION.equals(status)) {
            logger.error(details);
        } else {
            logger.debug(details);
        }
        results.put("status", status);
        setTaskStatusAndData(task, status, hashMapToJSONString(results));
    }

    /** Convert a HashMap to a string containing JSON.
     * @param map The HashMap to be converted
     * @return The resulting string
     */
    public static String hashMapToJSONString(
            final HashMap<String, ?> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            logger.error("Exception in hashMapToJSONString", e);
            return "{\"exception\":\"Exception while "
                    + "converting map to JSON\"}";
        }
    }

    /** Parse a string containing JSON into a JsonNode.
     * @param jsonString The String in JSON format to be converted
     * @return The resulting JSON structure
     */
    public static JsonNode jsonStringToTree(
            final String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(jsonString);
        } catch (IOException e) {
            logger.error("Exception in jsonStringToTree", e);
            return null;
        }
    }

    /** Parse a task params value containing JSON into a list of subtasks.
     * @param jsonString The String in JSON format to be converted
     * @return The resulting JSON structure
     */
    public static ArrayNode getSubtasks(
            final String jsonString) {
        JsonNode root = jsonStringToTree(jsonString);
        if (root == null) {
            logger.error("getSubtasks got a bad params string: "
                    + jsonString);
            return null;
        }
        if (!(root instanceof ArrayNode)) {
            logger.error("getSubtasks didn't get an array:"
                    + jsonString);
            return null;
        }
        return (ArrayNode) root;
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
                        makeSlug(projectId),
                        UriComponent.Type.PATH_SEGMENT));
        return path.toString().toLowerCase();
    }

    /** Get the full path of the backup directory used to store all
     * backup data for metadata extraction for a PoolParty project.
     * @param projectId The PoolParty projectId.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getMetadataBackupPath(final String projectId) {
        Path path = Paths.get(ToolkitConfig.METADATA_BACKUP_FILES_PATH)
                .resolve(UriComponent.encode(
                        makeSlug(projectId),
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
