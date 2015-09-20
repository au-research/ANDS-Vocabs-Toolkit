/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;

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
            callerLogger.error(details);
        } else {
            callerLogger.debug(details);
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

}
