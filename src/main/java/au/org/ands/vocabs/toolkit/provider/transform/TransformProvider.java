package au.org.ands.vocabs.toolkit.provider.transform;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Abstract class representing transform providers. */
public abstract class TransformProvider {

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** Return information about the provider.
     * @return The information.
     */
    public abstract Collection<?> getInfo();

    /** Do a transform. Update the message parameter with the result
     * of the transform.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The specification of this transform subtask
     * @param results HashMap representing the result of the transform.
     * @return True, iff the transform succeeded.
     */
    public abstract boolean transform(final TaskInfo taskInfo,
            JsonNode subtask,
            final HashMap<String, String> results);

}
