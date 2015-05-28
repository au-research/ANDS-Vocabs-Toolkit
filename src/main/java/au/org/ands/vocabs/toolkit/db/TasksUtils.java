package au.org.ands.vocabs.toolkit.db;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            logger.info("getTaskInfo: getTaskById returned null; task id:"
                    + taskId);
            return null;
        }
        Vocabularies vocab = getVocabularyById(task.getVocabularyId());
        Versions version = getVersionById(task.getVersionId());
        TaskInfo taskInfo = new TaskInfo(task, vocab, version);
        return taskInfo;
    }

    /** Set the status and data fields for a task.
     * @param task The task being updated
     * @param status The updated status information
     * @param data The updated data
     */
    public static void setTaskStatusAndData(final Task task,
            final String status, final String data) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        task.setStatus(status);
        task.setData(data);
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
        if ("ERROR".equals(status)) {
            logger.error(details);
        } else {
            logger.info(details);
        }
        results.put("status", details);
        TasksUtils.setTaskStatusAndData(task, status,
                hashMapToJSONString(results));
    }

    /** Convert a HashMap to a string containing JSON.
     * @param map The HashMap to be converted
     * @return The resulting string
     */
    public static String hashMapToJSONString(
            final HashMap<String, String> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "{\"exception\":\"Exception while "
                    + "converting map to JSON\"}";
        }
    }

    /** Get the full path of the directory used to store the
     * vocabulary referred to by the task.
     * @param taskInfo The TaskInfo object representing the task.
     * @return The full path of the directory used to store the
     * vocabulary data.
     */
    public static String getTaskOutputPath(final TaskInfo taskInfo) {
        Path path = Paths.get(ToolkitConfig.DATA_FILES_PATH)
                .resolve(taskInfo.getVocabulary().getOwner())
                .resolve(taskInfo.getVocabulary().getSlug())
                .resolve(taskInfo.getVersion().getTitle());
        return path.toString();
    }

}
