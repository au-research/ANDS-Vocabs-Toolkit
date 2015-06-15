package au.org.ands.vocabs.toolkit.provider.harvest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

import com.fasterxml.jackson.databind.JsonNode;

/** Harvest provider for Sesame. */
public class SesameHarvestProvider extends HarvestProvider {

    /** The logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    @Override
    public final String getInfo() {
        // Future work: get info from a remote Sesame repository
        return "Not yet implemented";
    }

    /** Do a harvest. Update the message parameter with the result
     * of the harvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    @Override
    public final boolean harvest(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {

        if (subtask.get("repository_base") == null) {
            TasksUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "No Sesame repository_base specified.");
            return false;
        }

        String remoteBase = subtask.get("repository_base").textValue();
        if (remoteBase.isEmpty()) {
            TasksUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "Blank Sesame repository_base specified.");
            return false;
        }

        if (subtask.get("repository_id") == null) {
            TasksUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "No Sesame repository_id specified.");
            return false;
        }

        String repositoryId = subtask.get("repository_id").textValue();
        if (repositoryId.isEmpty()) {
            TasksUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "Blank Sesame repository_id specified.");
            return false;
        }

        // Future work: support accessing via basic authentication.
//        String username = PROPS.getProperty("PoolPartyHarvester.username");
//        String password = PROPS.getProperty("PoolPartyHarvester.password");

        logger.debug("Getting project from " + remoteBase
                + ", repository id " + repositoryId);

        RepositoryManager manager = null;
        try {
            manager = RepositoryProvider.getRepositoryManager(remoteBase);

            Repository repository = manager.getRepository(repositoryId);
            if (repository == null) {
                // Repository is missing. This is bad.
                logger.error("Sesame import, repository missing");
                return false;
            }

            RepositoryConnection con = null;
            try {
                con = repository.getConnection();

                Path dir = Paths.get(ToolkitFileUtils.getTaskHarvestOutputPath(
                        taskInfo));
                ToolkitFileUtils.requireDirectory(dir.toString());

                // Future work: support getting just one context.
//                String contextUri = null;
//                Resource context;
//                if (contextUri != null &&  !(contextUri.isEmpty())) {
//                    context = repository.getValueFactory()
//                            .createURI(contextUri);
//                    results.put("contextUri", contextUri);
//                }

                File outputFile = new File(
                        dir.resolve(repositoryId + ".rdf").toString());
                OutputStream output = new FileOutputStream(outputFile);
                RDFXMLWriter rdfxmlfWriter = new RDFXMLWriter(output);
                con.export(rdfxmlfWriter);
//                output.write('\n');

            } catch (FileNotFoundException e) {
                results.put(TaskStatus.EXCEPTION,
                        "Sesame harvest, can't create output file");
                logger.error("Sesame harvest, can't create output file: ", e);
                return false;
            } catch (RDFHandlerException e) {
                results.put(TaskStatus.EXCEPTION,
                        "Sesame harvest, can't serialize");
                logger.error("Sesame harvest, can't serialize: ", e);
                return false;
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        } catch (RepositoryConfigException | RepositoryException e) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in Sesame harvest");
            logger.error("Exception in Sesame harvest", e);
            return false;
        }
        return true;
    }

    /** Extract the metadata for a given repository from a Sesame server.
     * @param repositoryId.
     * @return The metadata for the repository.
     */
    @Override
    public final HashMap<String, String> getMetadata(
            final String repositoryId) {
        HashMap<String, String> result =
                new HashMap<String, String>();
        result.put("error", "Not Implemented yet");
        return result;
    }

}
