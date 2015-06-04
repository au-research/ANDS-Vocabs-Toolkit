package au.org.ands.vocabs.toolkit.provider.publish;
import java.util.HashMap;
import java.util.Properties;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Abstract class representing publish providers. */
public abstract class PublishProvider {

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

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
    public abstract boolean publish(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results);

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
