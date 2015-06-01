package au.org.ands.vocabs.toolkit.provider.publish;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Abstract class representing publish providers. */
public abstract class PublishProvider {

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** Return information about the provider.
     * @return The information.
     */
    public abstract Collection<?> getInfo();

    /** Do a publish. Update the message parameter with the result
     * of the publish.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the publish.
     * @return True, iff the import succeeded.
     */
    public abstract boolean publish(final TaskInfo taskInfo,
            final HashMap<String, String> results);

}
