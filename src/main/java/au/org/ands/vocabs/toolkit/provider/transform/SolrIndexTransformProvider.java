package au.org.ands.vocabs.toolkit.provider.transform;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Transform provider for generating Solr Index files as JSON from RDF. */
public class SolrIndexTransformProvider extends TransformProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    @Override
    public final String getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        Path dir = Paths.get(ToolkitFileUtils.getTaskHarvestOutputPath(
                taskInfo));
        ConceptHandler conceptHandler = new ConceptHandler();
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(dir)) {
            for (Path entry: stream) {
                RDFFormat format = Rio.getParserFormatForFileName(
                        entry.toString());
                RDFParser rdfParser = Rio.createParser(format);
                rdfParser.setRDFHandler(conceptHandler);
                FileInputStream is = new FileInputStream(entry.toString());
                rdfParser.parse(is, entry.toString());

                logger.debug("Reading RDF:"
                        + entry.toString());

            }
        } catch (DirectoryIteratorException
                | IOException
                | RDFParseException
                | RDFHandlerException ex) {
            // I/O error encountered during the iteration,
            // the cause is an IOException
            results.put(TaskStatus.EXCEPTION,
                    "Exception in SolrIndexTransform while Parsing RDF");
            logger.error("Exception in SolrIndexTransform while Parsing RDF:",
                    ex);
            return false;
        }

        String resultFileName = ToolkitFileUtils.getTaskOutputPath(taskInfo,
                "concepts_solr.json");
        try {
            FileOutputStream out = new FileOutputStream(resultFileName);
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("concepts_count", conceptHandler.getCountedPrefLabels());
            results.put("concepts_count", Integer.toString(
                            conceptHandler.getCountedPrefLabels()));
            job.add("concepts_text", conceptHandler.getConceptText());
            results.put("concepts_solr", resultFileName);

            JsonWriter jsonWriter = Json.createWriter(out);
            jsonWriter.writeObject(job.build());
            jsonWriter.close();
        } catch (FileNotFoundException ex) {
            results.put(TaskStatus.EXCEPTION,
                    "Exception in SolrIndexTransform while generating result");
            logger.error("Exception in SolrIndexTransform generating result:",
                    ex);
            return false;
        }
        return true;
    }

    /** RDF Handler to extract prefLabels and concept count. */
    class ConceptHandler extends RDFHandlerBase {
        /** Number of prefLabel properties. */
        private int countedPrefLabels = 0;
        /** space separated String of all labels. */
        private String conceptText = "";

        @Override
        public void handleStatement(final Statement st) {
            if (st.getPredicate().equals(SKOS.PREF_LABEL)) {
                countedPrefLabels++;
                conceptText += st.getObject().stringValue() + " ";
            }
        }

        /** Getter for prefLabel count. */
        /** @return The number of prefLabel properties. */
        public int getCountedPrefLabels() {
            return countedPrefLabels;
        }

        /** Getter for concepts text. */
        /** @return The concatenation of the prefLabels. */
        public String getConceptText() {
            return conceptText;
        }


    }

}
