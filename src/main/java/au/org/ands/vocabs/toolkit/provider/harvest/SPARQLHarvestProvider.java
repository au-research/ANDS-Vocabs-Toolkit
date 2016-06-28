/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.harvest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.HashMap;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

import com.fasterxml.jackson.databind.JsonNode;

/** Harvest provider for a SPARQL endpoint. */
public class SPARQLHarvestProvider extends HarvestProvider {

    /** The logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Not implemented for this provider. Returns an empty String.
     */
    @Override
    public final String getInfo() {
        // No info available.
        return "";
    }


    /** Do a harvest. Update the result parameter with the result
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
        if (subtask.get("sparql_endpoint") == null
                || subtask.get("sparql_endpoint").textValue().isEmpty()) {
            TaskUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "No sparql_endpoint specified. Nothing to do.");
            return false;
        }

        String sparqlEndpoint = subtask.get("sparql_endpoint").textValue();

        logger.debug("Doing harvest from SPARQL endpoint:" + sparqlEndpoint);

        ToolkitFileUtils.requireDirectory(
                ToolkitFileUtils.getTaskHarvestOutputPath(taskInfo));

        String outputFileRDF = Paths.get(
                ToolkitFileUtils.getTaskHarvestOutputPath(taskInfo)).
                resolve("sparql_harvest.rdf").toString();

        Repository remoteRepository;
        RepositoryConnection conn;

        try {
            logger.debug("SPARQL harvest attempting to connect to remote "
                    + "endpoint: " + sparqlEndpoint);
            remoteRepository = new SPARQLRepository(sparqlEndpoint);
            remoteRepository.initialize();
            conn = remoteRepository.getConnection();
            String queryString = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
            GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL,
                    queryString);

            OutputStream output = new FileOutputStream(outputFileRDF);
//            RDFXMLWriter rdfxmlWriter = new RDFXMLWriter(output);
            RDFWriter rdfxmlWriter = Rio.createWriter(RDFFormat.RDFXML, output);

            // Evaluate the query and store the results.
            query.evaluate(rdfxmlWriter);

        // If needed: here's code to generate JSON output.
//        String outputFileJSON =
//                TasksUtils.getTaskOutputPath(taskInfo, "sparql_harvest.json");
//        // open a file to write the result to it in JSON format
//        OutputStream out = new FileOutputStream(
//                outputFileJSON);
//        TupleQueryResultHandler writer = new SPARQLResultsJSONWriter(out);
//
//        // execute the query and write the result directly to file
//        query.evaluate(writer);
//        logger.debug("exportSesame Saved RDF as :" + outputFileJSON);

            output.write('\n');
            output.close();
            logger.debug("SPARQL harvest saved RDF as: " + outputFileRDF);
            conn.close();
            remoteRepository.shutDown();
        } catch (RepositoryException | IOException
                | RDFHandlerException | MalformedQueryException
                | QueryEvaluationException e) {
            results.put(TaskStatus.EXCEPTION, "Exception in SPARQL harvest");
            logger.error("Exception in SPARQL harvest", e);
            return false;
        }
        results.put("sparql_harvest", outputFileRDF);
        return true;
    }

    /** Not implemented for this provider. Returns null.
     */
    @Override
    public final HashMap<String, String> getMetadata(final String projectId) {
        return null;
    }

}
