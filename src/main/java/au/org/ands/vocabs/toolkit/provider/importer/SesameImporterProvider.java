/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.importer;

// Disable LineLength check just for Eclipse-maintained imports.
//CHECKSTYLE:OFF: LineLength
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;
import org.openrdf.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;
//CHECKSTYLE:ON: LineLength

import com.fasterxml.jackson.databind.JsonNode;

/** Sesame importer provider. */
public class SesameImporterProvider extends ImporterProvider {

     /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** URL to access the Sesame server. */
    private String sesameServer =
            PROPS.getProperty("SesameImporter.serverUrl");

    /** URL that is a prefix to all SPARQL endpoints. */
    private String sparqlPrefix =
            PROPS.getProperty("SesameImporter.sparqlPrefix");

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

    @Override
    public final boolean doImport(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        boolean success;
        // Create repository
        success = createRepository(taskInfo, results);
        if (!success) {
            return false;
        }
        // Upload the RDF
        success = uploadRDF(taskInfo, subtask, results);
        if (!success) {
            return false;
        }
        results.put("repository_id", ToolkitFileUtils.getSesameRepositoryId(
                taskInfo));
        // Use the nice JAX-RS libraries to construct the path to
        // the SPARQL endpoint.
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(sparqlPrefix);
        WebTarget sparqlTarget = target
                .path(ToolkitFileUtils.getSesameRepositoryId(taskInfo));
        results.put("sparql_endpoint",
                sparqlTarget.getUri().toString());
        return true;
    }

    /** Create the repository within Sesame.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the task.
     * @return True, iff the repository creation succeeded.
     */
    public final boolean createRepository(final TaskInfo taskInfo,
            final HashMap<String, String> results) {
        RepositoryManager manager = null;
        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);

            String repositoryID = ToolkitFileUtils.getSesameRepositoryId(
                    taskInfo);
            String versionID = taskInfo.getVersion().getTitle();
            String repositoryTitle = taskInfo.getVocabulary().getTitle()
                    + " (Version: " + versionID + ")";

            Repository repository = manager.getRepository(repositoryID);
            if (repository != null) {
                // Already exists.
                // Possible future work: see if the vocabulary title
                // has changed in the database, and if so, update
                // the title in the Sesame repository.
                logger.debug("Sesame createRepository: already exists; "
                        + "reusing");
                return true;
            }

            // create a configuration for the SAIL stack
            SailImplConfig backendConfig;
            if ("current".equals(versionID)) {
                // Create an in-memory store for higher performance.
                boolean persist = true;
                backendConfig = new MemoryStoreConfig(persist);
            } else {
                // Create a native store.
                boolean forceSync = true;
                NativeStoreConfig nativeConfig = new NativeStoreConfig();
                nativeConfig.setForceSync(forceSync);
                backendConfig = nativeConfig;
            }

            // Stack an inferencer config on top of our backend-config.
            backendConfig =
                    new ForwardChainingRDFSInferencerConfig(backendConfig);

            // Create a configuration for the repository implementation.
            RepositoryImplConfig repositoryTypeSpec =
                  new SailRepositoryConfig(backendConfig);

            RepositoryConfig repConfig =
                  new RepositoryConfig(repositoryID, repositoryTitle,
                          repositoryTypeSpec);
            manager.addRepositoryConfig(repConfig);

            return true;
        } catch (RepositoryConfigException | RepositoryException e) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in Sesame createRepository()");
            logger.error("Exception in Sesame createRepository()", e);
        }
        return false;
    }

    /** Upload the RDF data into the Sesame repository.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the task.
     * @return True, iff the upload succeeded.
     */
    public final boolean uploadRDF(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        RepositoryManager manager = null;
        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);

            String repositoryID = ToolkitFileUtils.getSesameRepositoryId(
                    taskInfo);

            Repository repository = manager.getRepository(repositoryID);
            if (repository == null) {
                // Repository is missing. This is bad.
                logger.error("Sesame uploadRDF, repository missing");
                return false;
            }

            RepositoryConnection con = null;
            try {
                con = repository.getConnection();
                // If required, remove all existing triples
                if (subtask.get("clear") != null
                        && subtask.get("clear").booleanValue()) {
                    con.clear();
                }
                Path dir = Paths.get(ToolkitFileUtils.getTaskHarvestOutputPath(
                        taskInfo));
                try (DirectoryStream<Path> stream =
                        Files.newDirectoryStream(dir)) {
                    for (Path entry: stream) {
                        File file = new File(entry.toString());
                        logger.debug("Full path:"
                                + entry.toAbsolutePath().toString());
                        con.add(file, "",
                                Rio.getParserFormatForFileName(
                                        entry.toString()));
                    }
                } catch (DirectoryIteratorException | IOException ex) {
                    // I/O error encountered during the iteration,
                    // the cause is an IOException
                    results.put(TaskStatus.EXCEPTION,
                            "Exception in Sesame uploadRDF");
                    logger.error("Exception in Sesame uploadRDF:", ex);
                    return false;
                }
            } catch (RDFParseException e) {
                results.put(TaskStatus.EXCEPTION,
                        "Exception in Sesame uploadRDF");
                logger.error("Sesame uploadRDF, error parsing RDF: ", e);
                return false;
            } finally {
                if (con != null) {
                    con.close();
                }
            }

            return true;
        } catch (RepositoryConfigException | RepositoryException e) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in Sesame uploadRDF");
            logger.error("Exception in Sesame uploadRDF()", e);
        }
        return false;
    }

    @Override
    public final boolean unimport(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        // Remove the repository from the Sesame server.
        RepositoryManager manager = null;
        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);
            String repositoryID = ToolkitFileUtils.getSesameRepositoryId(
                    taskInfo);
            Repository repository = manager.getRepository(repositoryID);
            if (repository == null) {
                // No such repository; nothing to do.
                 logger.debug("Sesame unimport: nothing to do.");
                return true;
            }
            manager.removeRepository(repositoryID);
            // If we're still here, success, so return true.
            return true;
        } catch (RepositoryConfigException | RepositoryException e) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in Sesame unimport");
            logger.error("Exception in Sesame unimport", e);
        }
        return false;
    }

}
