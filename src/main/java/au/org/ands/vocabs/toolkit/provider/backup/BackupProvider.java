package au.org.ands.vocabs.toolkit.provider.backup;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;
//import au.org.ands.vocabs.toolkit.harvester.HttpsHack;

/** Abstract class representing backup providers. */
public abstract class BackupProvider {

    // /** UriInfo data for a request. */
    // private UriInfo info = null;

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS =
            ToolkitProperties.getProperties();

    /** get all project IDs.
     * @return an Array list with all IDs.
     */
    public abstract ArrayList<String> getProjects();


    /** Do a backup. Update the message parameter with the result
     * of the process.
     * @param projectId (optional) to identify the project to backup.
     * @return a hashmap with all information and files produced by the backup .
     */
    public abstract HashMap<String, Object> doBackup(final String projectId);


}
