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
public class SesameImporterProvider extends ImporterProvider {

     /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL to access the Sesame server. */
    private String sesameServer =
            PROPS.getProperty("SesameImporter.serverUrl");

    /** Force loading of HttpClientUtils, so that shutdown works
     * properly. Revisit this when using a later version of Tomcat,
     * as the problem may be caused by a defect in Tomcat.
     * For now (Tomcat 7.0.61), without this, you get an error
     * on Tomcat shutdown:
     * <pre>
     * Exception in thread "RepositoryProvider-shutdownHook"
     *  java.lang.NoClassDefFoundError:
     *   org/apache/http/client/utils/HttpClientUtils
     *    at org.openrdf.http.client.SesameClientImpl.shutDown(
     *    SesameClientImpl.java:102)
     * at org.openrdf.repository.manager.RemoteRepositoryManager.shutDown(
     *    RemoteRepositoryManager.java:156)
     * at org.openrdf.repository.manager.
     *   RepositoryProvider$SynchronizedManager.shutDown(
     *     RepositoryProvider.java:68)
     * at org.openrdf.repository.manager.RepositoryProvider$1.run(
     *   RepositoryProvider.java:81)
     * Caused by: java.lang.ClassNotFoundException:
     *    org.apache.http.client.utils.HttpClientUtils
     *  at org.apache.catalina.loader.WebappClassLoader.loadClass(
     *     WebappClassLoader.java:1720)
     *  at org.apache.catalina.loader.WebappClassLoader.loadClass(
     *     WebappClassLoader.java:1571)
     *  ... 4 more
     *  </pre>
     *
     */
    @SuppressWarnings("unused")
    private static final Class<org.apache.http.client.utils.HttpClientUtils>
        HTTPCLIENTUTILS_CLASS =
            org.apache.http.client.utils.HttpClientUtils.class;

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
        } catch (RepositoryConfigException | RepositoryException e) {
            logger.error("Exception in Sesame getInfo()", e);
        }
        return null;
    }

    /** Do an import. Update the message parameter with the result
     * of the import.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the task.
     * @return True, iff the import succeeded.
     */
    @Override
    public final boolean doImport(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        // Create repository
        createRepository(taskInfo, results);
        // Upload the RDF
        uploadRDF(taskInfo, results);
        return true;
    }

    /** Create the repository within Sesame.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the task.
     * @return True, iff the repository creation succeeded.
     */
    public final boolean createRepository(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        return true;
    }

    /** Upload the RDF data into the Sesame repository.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the task.
     * @return True, iff the upload succeeded.
     */
    public final boolean uploadRDF(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        return true;
    }

}
