/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.tasks;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.provider.harvest.HarvestProvider;
import au.org.ands.vocabs.toolkit.provider.harvest.HarvestProviderUtils;
import au.org.ands.vocabs.toolkit.provider.importer.ImporterProvider;
import au.org.ands.vocabs.toolkit.provider.importer.ImporterProviderUtils;
import au.org.ands.vocabs.toolkit.provider.publish.PublishProvider;
import au.org.ands.vocabs.toolkit.provider.publish.PublishProviderUtils;
import au.org.ands.vocabs.toolkit.provider.transform.TransformProvider;
import au.org.ands.vocabs.toolkit.provider.transform.TransformProviderUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

/** Top level runner for tasks. */
public class TaskRunner {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** The TaskInfo object for this task. */
    private TaskInfo taskInfo;

    /** The Task object for this task. */
    private Task task;

    /** Status of the task. */
    private String status;

    /** The results of running the task. */
    private HashMap<String, String> results = new HashMap<String, String>();

    /** Constructor.
     * @param aTaskInfo The TaskInfo structure describing this task.
     */
    public TaskRunner(final TaskInfo aTaskInfo) {
        taskInfo = aTaskInfo;
    }

    /** Get the task status.
     * @return The task status.
     */
    public final String getStatus() {
        return status;
    }

    /** Run the task.
     */
    public final void runTask() {
        status = TaskStatus.SUCCESS;
        task = taskInfo.getTask();
        results.put("task_id", task.getId().toString());
        ArrayNode subtasks = TaskUtils.getSubtasks(task.getParams());
        if (subtasks == null || subtasks.size() == 0) {
            status = TaskStatus.ERROR;
            results.put("runTask", "No subtasks specified, or invalid"
                    + " format.");
            addTimestamp(results);
            TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "No subtasks specified. Nothing to do.");
            return;
        }
        boolean success = false;
        for (JsonNode subtask : subtasks) {
            logger.debug("Got subtask: " + subtask.toString());
            if (!(subtask instanceof ObjectNode)) {
                logger.error("runTask() didn't get an object:"
                        + subtask.toString());
                status = TaskStatus.ERROR;
                addTimestamp(results);
                TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                        status, "Bad subtask specification.");
                return;
            }
            logger.debug("subtask type: " + subtask.get("type"));
            String thisTask = subtask.get("type").textValue();
            switch (thisTask) {
                case "HARVEST":
                case "UNHARVEST":
                    success = runHarvest(subtask, thisTask);
                    break;
                case "TRANSFORM":
                case "UNTRANSFORM":
                    success = runTransform(subtask, thisTask);
                    break;
                case "IMPORT":
                case "UNIMPORT":
                    success = runImport(subtask, thisTask);
                    break;
                case "PUBLISH":
                case "UNPUBLISH":
                    success = runPublish(subtask, thisTask);
                    break;
//                case "DELETE":
//                    success = runDelete(subtask);
//                    break;
                default:
                    status = TaskStatus.ERROR;
                    results.put("invalid_sub_task", thisTask);
                    TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                            status, "Invalid subtask specification.");
                break;
            }
            if (!success) {
                logger.error("ERROR while running task: " + thisTask);
                results.put("error_subtask", thisTask);
                status = TaskStatus.ERROR;
                addTimestamp(results);
                TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                        status, "Error in subtask.");
                return;
            }
        }
        status = TaskStatus.SUCCESS;
        results.put("output_path", ToolkitFileUtils.getTaskOutputPath(taskInfo,
                null));
        addTimestamp(results);
        TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                status, "All tasks completed.");
    }

    /** Run a harvest.
     * @param subtask Details of the subtask
     * @param taskType The type of harvest operation to be performed.
     * @return True, iff the harvest was successful.
     */
    public final boolean runHarvest(final JsonNode subtask,
            final String taskType) {
        HarvestProvider provider;
        String providerName = subtask.get("provider_type").textValue();
        logger.debug("runHarvest, task type: " + taskType);
        status = taskType + "ING";
        TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                status, "Harvest in progress");
        try {
            provider = HarvestProviderUtils.getProvider(providerName);
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException e) {
            logger.error("runHarvest exception: ", e);
            results.put(TaskStatus.EXCEPTION, e.toString());
            return false;
        }

        if (provider == null) {
            status = TaskStatus.ERROR;
            TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "Could not find Provider: " + providerName);
            return false;
        }
        switch (taskType) {
        case "HARVEST":
            return provider.harvest(taskInfo, subtask, results);
        case "UNHARVEST":
            return provider.unharvest(taskInfo, subtask, results);
        default:
            return false;
        }
    }

    /** Run a transform.
     * @param subtask Details of the subtask
     * @param taskType The type of transform operation to be performed.
     * @return True, iff the transform was successful.
     */
    public final boolean runTransform(final JsonNode subtask,
            final String taskType) {
        logger.debug("runTransform");
        TransformProvider provider;
        String providerName = subtask.get("provider_type").textValue();
        status = "TRANSFORMING";
        TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                status, "Transform in progress");
         try {
            provider = TransformProviderUtils.getProvider(providerName);
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException e) {
            logger.error("runTransform exception: ", e);
            results.put(TaskStatus.EXCEPTION, e.toString());
            return false;
        }

        if (provider == null) {
            status = TaskStatus.ERROR;
            TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "Could not find Provider: " + providerName);
            return false;
        }
        switch (taskType) {
        case "TRANSFORM":
            return provider.transform(taskInfo, subtask, results);
        case "UNTRANSFORM":
            return provider.untransform(taskInfo, subtask, results);
        default:
            return false;
        }
    }

    /** Run an import operation.
     * @param subtask Details of the subtask
     * @param taskType The type of import operation to be performed.
     * @return True, iff the import was successful.
     */
    public final boolean runImport(final JsonNode subtask,
            final String taskType) {
        ImporterProvider provider;
        String providerName = subtask.get("provider_type").textValue();
        logger.debug("runImport, task type: " + taskType);
        status = taskType + "ING";
        TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                status, "Import in progress");
         try {
            provider = ImporterProviderUtils.getProvider(providerName);
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException e) {
            logger.error("runImport exception: ", e);
            results.put(TaskStatus.EXCEPTION, e.toString());
            return false;
        }

        if (provider == null) {
            status = TaskStatus.ERROR;
            TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "Could not find Provider: " + providerName);
            return false;
        }
        switch (taskType) {
        case "IMPORT":
            return provider.doImport(taskInfo, subtask, results);
        case "UNIMPORT":
            return provider.unimport(taskInfo, subtask, results);
        default:
            return false;
        }
    }

    /** Run a publish operation.
     * @param subtask Details of the subtask
     * @param taskType The type of publish operation to be performed.
     * @return True, iff the publish was successful.
     */
    public final boolean runPublish(final JsonNode subtask,
            final String taskType) {
        PublishProvider provider;
        String providerName = subtask.get("provider_type").textValue();
        logger.debug("runPublish, task type: " + taskType);
        status = taskType + "ING";
        TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                status, "Publish in progress");
         try {
            provider = PublishProviderUtils.getProvider(providerName);
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException e) {
            logger.error("runPublish exception: ", e);
            results.put(TaskStatus.EXCEPTION, e.toString());
            return false;
        }

        if (provider == null) {
            status = TaskStatus.ERROR;
            TaskUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "Could not find Provider: " + providerName);
            return false;
        }
        switch (taskType) {
        case "PUBLISH":
            return provider.publish(taskInfo, subtask, results);
        case "UNPUBLISH":
            return provider.unpublish(taskInfo, subtask, results);
        default:
            return false;
        }
    }

    // Possible future work: support a "DELETE" task type.
//    /** Run a Delete.
//     * @param subtask Details of the subtask
//     * @return True, iff the deleting was successful.
//     */
//    public final boolean runDelete(final JsonNode subtask) {
//        logger.debug("runDelete");
//        return true;
//    }

    /** Format to use for timestamps. */
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Add a timestamp to the results map.
     * @param resultsMap The results map to which the timestamp is to be added.
     */
    public final void addTimestamp(final HashMap<String, String> resultsMap) {
        final SimpleDateFormat dateFormat =
                new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.ROOT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // In case you need to undo the conversion in some other part
        // of the code, here's how to do it:
//        final SimpleDateFormat dateFormat =
//                new SimpleDateFormat(TIMESTAMP_FORMAT);
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        // Get timestamp some other way than this, of course.
//        String timestamp = dateFormat.format(new Date());
//        try {
//            Date date = dateFormat.parse(timestamp);
//            logger.debug("Timestamp string is: " + timestamp);
//            logger.debug("Date is: " + date);
//        } catch (ParseException e) {
//            logger.error("Exception parsing timestamp", e);
//        }

        resultsMap.put("timestamp", dateFormat.format(new Date()));
    }

    /** Return the results of running the task.
     * @return The HashMap for the results. */
    public final HashMap<String, String> getResults() {
        results.put("status", status);
        logger.debug("getResults results:" + results.toString());
        return results;
    }

    // Code previously considered but rejected for now.
//    /** Return the results of running the task as a String.
//     * @return The results as a String. */
//    public final String getResultsString() {
//        return getResults().toString();
//    }

}
