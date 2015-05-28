package au.org.ands.vocabs.toolkit.db;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;

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

    /** Set the status and data fields for a task. */
    public static void setTaskStatusAndData(final Task task, final String status,
            final String data) {
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
     * @param message The message JsonObjectBuilder object.
     * @param status Status (SUCCESS, ERROR, ...)
     * @param details Detailed message data
     */
    public static void updateMessageAndTaskStatus(final Logger callerLogger,
            final Task task, final JsonObjectBuilder message,
            final String status, final String details) {
        if ("ERROR".equals(status)) {
            logger.error(details);
        } else {
            logger.info(details);
        }
        message.add(status, details);
        TasksUtils.setTaskStatusAndData(task, status,
                message.build().toString());
    }

}
