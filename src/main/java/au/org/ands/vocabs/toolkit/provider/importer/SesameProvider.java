package au.org.ands.vocabs.toolkit.provider.importer;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Abstract class representing importer providers. */
public class SesameProvider extends ImporterProvider {

     /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL to access the Sesame server. */
    private String sesameServer =
            PROPS.getProperty("SesameImporter.serverUrl");


    /** Return information about the provider.
     * @return The information.
     */
    @Override
    public final Collection<?> getInfo() {
        RepositoryManager manager = null;
        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);
            Collection<RepositoryInfo> infos =
                    manager.getAllRepositoryInfos(true);
            return infos;
        } catch (RepositoryConfigException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /** Do an import. Update the message parameter with the result
     * of the import.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the import succeeded.
     */
    @Override
    public final boolean doImport(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        return true;
    }

}
