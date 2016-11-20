/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.publish;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;

/** Abstract class representing publish providers. */
public abstract class PublishProvider {

    /** Return information about the provider.
     * @return The information.
     */
    public abstract String getInfo();

    /** Do a publish. Update the message parameter with the result
     * of the publish.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the publish.
     * @return True, iff the publish succeeded.
     */
    public abstract boolean publish(TaskInfo taskInfo,
            JsonNode subtask,
            HashMap<String, String> results);

    /** Do an unpublish. Update the message parameter with the result
     * of the unpublish.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the unpublish.
     * @return True, iff the unpublish succeeded.
     */
    public abstract boolean unpublish(TaskInfo taskInfo, JsonNode subtask,
            HashMap<String, String> results);

}
