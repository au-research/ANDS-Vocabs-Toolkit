package au.org.ands.vocabs.toolkit.provider.harvest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.net.ssl.HttpsURLConnection;

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

import au.org.ands.vocabs.toolkit.harvester.HttpsHack;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitConfig;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Abstract class representing Providers. */
public abstract class HarvestProvider {

    // /** UriInfo data for a request. */
    // private UriInfo info = null;

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected Properties props = ToolkitProperties.getProperties();

    //public Provider(final UriInfo urlInfo){
    //info = urlInfo;
    //props = ToolkitProperties.getProperties();
    //}

    /** Return information about the provider.
     * @return The information.
     */
    public String getInfo() {
        throw new IllegalArgumentException("Incorrect use of abstract class.");
    }

    /** Do a harvest. Update the message parameter with the result
     * of the harvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param message JSON Structure representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    public boolean harvest(final TaskInfo taskInfo,
            final JsonObjectBuilder message) {
        //message.add("class", "HarvesterHandler");
        //return true;
        throw new IllegalArgumentException("Incorrect use of abstract class.");
    }

    protected InputStream getInputStream(final URL url,
            final String basicAuth) throws IOException {
        HttpURLConnection conn;
        InputStream in = null;
        int responseCode = 0;

        do {
            String protocol = url.getProtocol();
            boolean secure = "https".equals(protocol);
            if (secure) {
                try {
                    HttpsHack.disableCertCheck();
                    conn = (HttpsURLConnection)url.openConnection();
                    //dumpl all cert info
                    //print_https_cert((HttpsURLConnection)conn);

                    //dump all the content
                    // print_content((HttpsURLConnection)conn);
                }
                catch (Exception e) {
                    throw new IOException("unable to allow connection to url: " + url.toExternalForm());
                }
            }
            else {
                conn = (HttpURLConnection)url.openConnection();
            }
            conn.setConnectTimeout(10000);
            if(!(basicAuth.isEmpty())) {
                logger.info("basicAuth " + basicAuth);
                conn.setRequestProperty("Authorization", basicAuth);
            }
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
            conn.setRequestProperty("Accept-Encoding", "compress, gzip, identify");
            try {
                responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode > 299) {
                    conn.disconnect();
                    logger.error("HTTP code " + responseCode + " connecting to url: " + url);
                    throw new IOException("HTTP code " + responseCode + " connecting to url: " + url);
                }
            } catch (IOException ioe) {
                logger.error("HTTP code " + responseCode + " connecting to url: " + url);
                if (responseCode != HttpURLConnection.HTTP_UNAVAILABLE) {
                    conn.disconnect();
                    throw ioe;
                }
            }

            if (responseCode == HttpURLConnection.HTTP_UNAVAILABLE) {
                long retrySeconds = conn.getHeaderFieldInt("Retry-After", -1);
                logger.error("retry seconds: " + retrySeconds);
                if (retrySeconds == -1) {
                    logger.error("could not connect to: " + url.toExternalForm() + " , retrying");
                    long now = (new Date()).getTime();
                    long retryDate = conn.getHeaderFieldDate("Retry-After", now);
                    retrySeconds = retryDate - now;
                }

                if (retrySeconds == 0) {
                    conn.disconnect();
                    logger.error("Too many retries connecting to: " + url.toExternalForm());
                    throw new IOException("Too many retries connecting to: " + url);
                }

                if (retrySeconds > 0) {
                    try {
                        Thread.sleep(retrySeconds * 1000);
                    } catch (InterruptedException ex) {
                        conn.disconnect();
                        logger.error(ex.toString());
                    }
                }
            }
        } while (responseCode == HttpURLConnection.HTTP_UNAVAILABLE);

        String contentEncoding = conn.getHeaderField("Content-Encoding");

        if ("compress".equals(contentEncoding)) {
            ZipInputStream zis = new ZipInputStream(conn.getInputStream());
            zis.getNextEntry();
            in = zis;
            zis.close();
        } else if ("gzip".equals(contentEncoding)) {
            in = new GZIPInputStream(conn.getInputStream());
        } else if ("deflate".equals(contentEncoding)) {
            in = new InflaterInputStream(conn.getInputStream());
        } else {
            in = conn.getInputStream();
        }

        return in;
    }

    public String getFragment(final InputStream is) throws IOException {
        BufferedReader data = new BufferedReader(new InputStreamReader(is));
        StringBuffer buf = new StringBuffer();

        int aChar = -1;

        while ((aChar = data.read()) != -1)
        {
            buf.append((char)aChar);
        }

        data.close();

        return buf.toString();
    }


    public void requireDirectory(final String dir) {
        File oDir = new File(dir);
        if(!oDir.exists()) {
            oDir.mkdirs();
        }
    }

    public String saveFile(final String project_id, final String exportModule,
            final String format, final String data) {
        String fileExtension =
                ToolkitConfig.FORMAT_TO_FILEEXT_MAP.get(format.toLowerCase());
        String filePath =  ToolkitConfig.DATA_FILES_PATH + project_id +
                File.separator + exportModule + fileExtension;
        FileWriter writer = null;
        try {
            requireDirectory(ToolkitConfig.DATA_FILES_PATH + project_id);
            //File oDir = new File(ToolkitConfig.DATA_FILES_PATH +  project_id);
            //if(!oDir.exists()){
            //oDir.mkdirs();
            //}
            File oFile = new File(filePath);
            writer = new FileWriter(oFile);
            writer.write(data);
            writer.close();
        }
        catch (IOException e) {
            return "Exception: " + e.toString();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    return "Exception: " + e.toString();
                }
            }
        }
        return filePath;
    }

    public String createSolrJson(final RDFFormat inputFormat,
            final InputStream is, final String project_id) {
        Model model;
        String filePath = "/temp/temp/json";
        try {
            model = Rio.parse(is,
                    "", inputFormat);
            FileOutputStream out;
            requireDirectory(ToolkitConfig.DATA_FILES_PATH +  project_id);
            filePath =  ToolkitConfig.DATA_FILES_PATH +  project_id + File.separator + "concepts_tree.json";
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
    /**
     * Writing RDF or NTriples to a file
     * To debug SSL, add -Djavax.net.debug=ssl,handshake to the Tomcat JVM
     * command line.
     */
    public JsonObject exportSesame(final String sesameServer, final String repositoryID, final String contextUri, String outputFile2) throws Exception {
        JsonObjectBuilder message = Json.createObjectBuilder();
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("exportSesame From Server:"+ sesameServer+ " repositoryID: " + repositoryID+ " contextUri: "+ contextUri);
        RepositoryManager manager;

        try {
            manager = RepositoryProvider.getRepositoryManager(sesameServer);
            message.add("sesameServer", sesameServer);
            Repository repository = manager.getRepository(repositoryID);
            message.add("repositoryID", repositoryID);
            RepositoryConnection conn = repository.getConnection();
            Repository myRepository = conn.getRepository();
            URI context = null;
            if(contextUri != null &&  !(contextUri.isEmpty()))
            {
                context = myRepository.getValueFactory().createURI(contextUri);
                message.add("contextUri", contextUri);
            }
            if(outputFile2 == null) {
                outputFile2 = ToolkitConfig.DATA_FILES_PATH + repositoryID + ".rdf";
            }
            OutputStream output = new FileOutputStream(outputFile2);
            RDFXMLWriter rdfxmlfWriter = new RDFXMLWriter(output);
            conn.export(rdfxmlfWriter);
            output.write('\n');

            String queryString = "SELECT * WHERE {?s ?p ?o . }";
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            // open a file to write the result to it in JSON format
            OutputStream out = new FileOutputStream(ToolkitConfig.DATA_FILES_PATH + repositoryID + ".json");
            TupleQueryResultHandler writer = new SPARQLResultsJSONWriter(out);

            // execute the query and write the result directly to file
            query.evaluate(writer);
            logger.info("exportSesame Saved RDF as :"+ outputFile2);
            message.add("outputFile", outputFile2);

        }
        catch (Exception e) {
            logger.error("\nException while Writing RDF: " + e.toString());
            JsonObjectBuilder job = Json.createObjectBuilder();
            return job.add("exception", e.toString()).build();
        }
        return message.build();
    }
}
