package au.org.ands.vocabs.toolkit.tasks;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;
import au.org.ands.vocabs.toolkit.provider.harvest.HarvestProvider;
import au.org.ands.vocabs.toolkit.provider.harvest.HarvestProviderUtils;

/** Top level runner for tasks. */
public class TaskRunner {

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** The TaskInfo object for this task. */
    private TaskInfo taskInfo;

    /** The Task object for this task. */
    private Task task;

    /** The Vocabularies object for this task. */
    private Vocabularies vocab;

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
     * This is broken. It should use the Task's type to determine
     * what to do.
     */
    public final void runTask() {
        task = taskInfo.getTask();
        vocab = taskInfo.getVocabulary();
        results.put("task_id", task.getId().toString());
        if (task.getType() == null
                || task.getType().isEmpty()) {
            status = "ERROR";
            TasksUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "No task type specified. Nothing to do.");
            return;
        }
        results.put("task_type", task.getType());
        if (task.getType().equalsIgnoreCase("HARVEST-TRANSFORM-IMPORT")) {
            boolean success = runHarvest();
            if (success) {
                success = runTransform();
                if (success) {
                    success = runImport();
                    if (success) {
                        status = "SUCCESS";
                        TasksUtils.updateMessageAndTaskStatus(logger,
                                task, results,
                                status, "Completed");
                    }
                }
            }
        }
    }

    /** Run a harvest.
     * @return True, iff the harvest was successful.
     */
    public final boolean runHarvest() {
        HarvestProvider provider;
        String providerName = "PoolParty";
        status = "HARVESTING";
        results.put("repository_id", TasksUtils.getTaskRepositoryId(taskInfo));
        results.put("output_path", TasksUtils.getTaskOutputPath(taskInfo));
        TasksUtils.updateMessageAndTaskStatus(logger, task, results,
                status, "Harvest in progress");
        if (vocab.getPoolPartyId() == null
                || vocab.getPoolPartyId().isEmpty()) {
            status = "ERROR";
            TasksUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "No PoolParty id specified. Nothing to do.");
            return false;
        }
        try {
            provider = HarvestProviderUtils.getProvider(providerName);
        } catch (ClassNotFoundException e) {
            logger.error("runTask exception: ", e);
            results.put("exception", e.toString());
            return false;
        } catch (InstantiationException e) {
            logger.error("runTask exception: ", e);
            results.put("exception", e.toString());
            return false;
        } catch (IllegalAccessException e) {
            logger.error("runTask exception: ", e);
            results.put("exception", e.toString());
            return false;
        }

        if (provider == null) {
            status = "ERROR";
            TasksUtils.updateMessageAndTaskStatus(logger, task, results,
                    status, "Could not find Provider: " + providerName);
            return false;
        }
        return provider.harvest(taskInfo, results);
    }

    /** Run a transform.
     * @return True, iff the transform was successful.
     */
    public final boolean runTransform() {
        return true;
    }

    /** Run an import.
     * @return True, iff the import was successful.
     */
    public final boolean runImport() {
        return true;
    }



    /** Return the results of running the task.
     * @return The HashMap for the results. */
    public final HashMap<String, String> getResults() {
        results.put("status", status);
        logger.info("getResults results:" + results.toString());
        return results;
    }

//    /** Return the results of running the task as a String.
//     * @return The results as a String. */
//    public final String getResultsString() {
//        return getResults().toString();
//    }



}
