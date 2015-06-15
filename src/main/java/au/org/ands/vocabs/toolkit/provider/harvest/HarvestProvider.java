package au.org.ands.vocabs.toolkit.provider.harvest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import au.org.ands.vocabs.toolkit.harvester.HttpsHack;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitConfig;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

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


    /** Get the metadata about a vocabulary.
     * @return The information.
     * @param repositoryId the repository_id of the given vocabulary
     */
    public abstract HashMap<String, String> getMetadata(
            final String repositoryId);

    /** Do a harvest. Update the message parameter with the result
     * of the harvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    public abstract boolean harvest(final TaskInfo taskInfo,
            JsonNode subtask,
            final HashMap<String, String> results);

    /** Do an unharvest. Update the message parameter with the result
     * of the unharvest.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the unharvest.
     * @return True, iff the unharvest succeeded.
     */
    public final boolean unharvest(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        try {
            FileUtils.deleteDirectory(new File(
                    ToolkitFileUtils.getTaskHarvestOutputPath(taskInfo)));
            return true;
        } catch (IOException e) {
            // This may mean a file permissions problem, so do log it.
            logger.error("Unharvest failed", e);
        }
        return false;
    }

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

}
