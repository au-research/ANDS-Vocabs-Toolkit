package au.org.ands.vocabs.toolkit.provider.harvest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import au.org.ands.vocabs.toolkit.harvester.HttpsHack;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitConfig;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Abstract class representing harvester providers. */
public abstract class HarvestProvider {

    // /** UriInfo data for a request. */
    // private UriInfo info = null;

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS =
            ToolkitProperties.getProperties();

    /** Return information about the provider.
     * @return The information.
     */
    public abstract String getInfo();

    /** Do a harvest. Update the message parameter with the result
     * of the harvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    public abstract boolean harvest(final TaskInfo taskInfo,
            final HashMap<String, String> results);

    /** Get the contents of an InputStream as a String.
     * @param is The InputStream
     * @return The contenxt of the InputStream as a String
     * @throws IOException An IOException
     */
    public final String getFragment(final InputStream is) throws IOException {
        BufferedReader data = new BufferedReader(new InputStreamReader(is));
        StringBuffer buf = new StringBuffer();

        int aChar = -1;

        while ((aChar = data.read()) != -1) {
            buf.append((char) aChar);
        }

        data.close();

        return buf.toString();
    }

    /** Create the Solr document in JSON format represented
     *  as a String.
     * @param inputFormat The input format
     * @param is The InputStream
     * @param projectId The PoolParty project id
     * @return The full path of the file that was written.
     */
    public final String createSolrJson(final RDFFormat inputFormat,
            final InputStream is, final String projectId) {
        Model model;
        String filePath = "/temp/temp/json";
        try {
            model = Rio.parse(is,
                    "", inputFormat);
            FileOutputStream out;
            ToolkitFileUtils.requireDirectory(
                    ToolkitConfig.DATA_FILES_PATH +  projectId);
            filePath =  ToolkitConfig.DATA_FILES_PATH
                    + projectId + File.separator + "concepts_tree.json";
            out = new FileOutputStream(filePath);
            Rio.write(model, out, RDFFormat.RDFJSON);
            // String solrJson = Rio.
        } catch (RDFParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedRDFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RDFHandlerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return filePath;
    }
    /** Write RDF or NTriples to a file
     *  To debug SSL, add -Djavax.net.debug=ssl,handshake to the Tomcat JVM
     *  command line.
     *  @param sesameServer The URL of the Sesame server.
     *  @param repositoryID The repository ID
     *  @param contextUri The context URI
     *  @param outputFile2 The name of the output file
     *  @return Metadata representing the result of the export
     *  @throws Exception An exception
     */
    public final JsonObject exportSesame(final String sesameServer,
            final String repositoryID, final String contextUri,
            String outputFile2) throws Exception {
        JsonObjectBuilder message = Json.createObjectBuilder();
        logger.info("exportSesame From Server:" + sesameServer
                + " repositoryID: " + repositoryID
                + " contextUri: " + contextUri);
        RepositoryManager manager;

        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);
            message.add("sesameServer", sesameServer);
            Repository repository = manager.getRepository(repositoryID);
            message.add("repositoryID", repositoryID);
            RepositoryConnection conn = repository.getConnection();
            Repository myRepository = conn.getRepository();
            URI context = null;
            if (contextUri != null &&  !(contextUri.isEmpty())) {
                context = myRepository.getValueFactory().createURI(contextUri);
                message.add("contextUri", contextUri);
            }
            if (outputFile2 == null) {
                outputFile2 = ToolkitConfig.DATA_FILES_PATH
                        + repositoryID + ".rdf";
            }
            OutputStream output = new FileOutputStream(outputFile2);
            RDFXMLWriter rdfxmlfWriter = new RDFXMLWriter(output);
            conn.export(rdfxmlfWriter);
            output.write('\n');

            String queryString = "SELECT * WHERE {?s ?p ?o . }";
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL,
                    queryString);

            // open a file to write the result to it in JSON format
            OutputStream out = new FileOutputStream(
                    ToolkitConfig.DATA_FILES_PATH + repositoryID + ".json");
            TupleQueryResultHandler writer = new SPARQLResultsJSONWriter(out);

            // execute the query and write the result directly to file
            query.evaluate(writer);
            logger.info("exportSesame Saved RDF as :" + outputFile2);
            message.add("outputFile", outputFile2);

        } catch (Exception e) {
            logger.error("\nException while Writing RDF: " + e.toString());
            JsonObjectBuilder job = Json.createObjectBuilder();
            return job.add("exception", e.toString()).build();
        }
        return message.build();
    }
}
