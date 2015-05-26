package au.org.ands.vocabs.toolkit.db;

import javax.persistence.EntityManager;

import au.org.ands.vocabs.toolkit.db.model.Task;

/** Work with database tasks. */
public final class TasksUtils {

    /** Private constructor for a utility class. */
    private TasksUtils() {
    }

    /** Get a task by ID.
     * @param id task id
     * @return the task
     */
    public static Task getTaskById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Task t = em.find(Task.class, id);
        em.close();
        return t;
    }


}
