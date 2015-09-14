/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;
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
     * Currently the return type is String.
     * Currently, no subclass implements this method!
     * So, once we work out what we are going to do with this method,
     * the return type can be changed.
     * @return The information.
     */
    public abstract String getInfo();

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

    /** Undo a transform. Update the message parameter with the result
     * of the untransform. Note: not all transforms can be undone!
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The specification of this untransform subtask
     * @param results HashMap representing the result of the untransform.
     * @return True, iff the untransform succeeded.
     */
    public abstract boolean untransform(final TaskInfo taskInfo,
            JsonNode subtask,
            final HashMap<String, String> results);


}
