/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.harvest;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

/** Abstract class representing harvester providers. */
public abstract class HarvestProvider {

    // /** UriInfo data for a request. */
    // private UriInfo info = null;

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Return information about the provider.
     * @return The information.
     */
    public abstract String getInfo();


    /** Get the metadata about a vocabulary.
     * @return The information.
     * @param repositoryId the repository_id of the given vocabulary
     */
    public abstract HashMap<String, String> getMetadata(
            String repositoryId);

    /** Do a harvest. Update the message parameter with the result
     * of the harvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    public abstract boolean harvest(TaskInfo taskInfo,
            JsonNode subtask,
            HashMap<String, String> results);

    /** Do an unharvest. Update the message parameter with the result
     * of the unharvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the unharvest.
     * @return True, iff the unharvest succeeded.
     */
    public final boolean unharvest(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        try {
            unharvestProviderSpecific(taskInfo, subtask, results);
            FileUtils.deleteDirectory(new File(
                    ToolkitFileUtils.getTaskHarvestOutputPath(taskInfo)));
            return true;
        } catch (IOException e) {
            // This may mean a file permissions problem, so do log it.
            logger.error("Unharvest failed", e);
        }
        return false;
    }

    /** Do the provider-specific part of an unharvest.
     * Update the message parameter with the result
     * of the unharvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the unharvest.
     */
    @SuppressWarnings("unused")
    protected void unharvestProviderSpecific(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
    }

}
