/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.backup;
import java.util.HashMap;
import java.util.Properties;

import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Abstract class representing backup providers. */
public abstract class BackupProvider {

    // Uncomment the following if/when it is needed.
//    /** Logger for this class. */
//    private final Logger logger = LoggerFactory.getLogger(
//            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS =
            ToolkitProperties.getProperties();

    /** Do a backup. Update the message parameter with the result
     * of the process.
     * @param projectId (optional) to identify the project to backup.
     * If the value is null, backup all projects.
     * @return All information and files produced by the backup.
     */
    public abstract HashMap<String, Object> backup(final String projectId);


}
