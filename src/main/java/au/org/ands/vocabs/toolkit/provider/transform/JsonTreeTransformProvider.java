package au.org.ands.vocabs.toolkit.provider.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.io.FileUtils;
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

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Transform provider for generating Solr Index files as JSON from RDF. */
public class JsonTreeTransformProvider extends TransformProvider {

    /** Logger for this class. */
   private final Logger logger = LoggerFactory.getLogger(
           MethodHandles.lookup().lookupClass());

   /** Access to the Toolkit properties. */
   protected static final Properties PROPS = ToolkitProperties.getProperties();
    @Override
    public final Collection<?> getInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        Path dir = Paths.get(TasksUtils.getTaskHarvestOutputPath(
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

                logger.info("Reading RDF:"
                        + entry.toString());

            }
        } catch (DirectoryIteratorException
                | IOException
                | RDFParseException
                | RDFHandlerException ex) {
            // I/O error encountered during the iteration,
            // the cause is an IOException
            logger.error("Exception in JsonTreeTransform while Parsing RDF:"
                    , ex);
            return false;
        }

        String resultFileName = TasksUtils.getTaskOutputPath(taskInfo,
                "concepts_tree.json");
        try {
            File out = new File(resultFileName);
            results.put("concepts_tree", resultFileName);
//            JsonObject jTree = conceptHandler.getJsonTree();
            HashMap<String, HashMap<String, Object>> conceptMap =
                    conceptHandler.getCmap();
//            JsonWriter jsonWriter = Json.createWriter(out);
//            jsonWriter.writeObject(jTree);
//            jsonWriter.writeObject(conceptMap);
//            jsonWriter.close();
            FileUtils.writeStringToFile(out,
                    TasksUtils.hashMapToJSONString(conceptMap));
        } catch (IOException ex) {
            logger.error("Exception in JsonTreeTransform generating result:"
                    , ex);
            return false;
        }
//        RDFWriter writer = Rio.createWriter(RDFFormat.RDFJSON, out);

        return true;
    }
/** RDF Hadler to extract prefLabels and concept count. */
    class ConceptHandler extends RDFHandlerBase {

        /** a Json Builder to build the Tree. */
        private JsonObjectBuilder job = Json.createObjectBuilder();
        /** space separated String of all labels. */
        private HashMap<String, HashMap<String, Object>> cMap =
                new HashMap<String, HashMap<String, Object>>();

        @Override
        public void handleStatement(final Statement st) {
            if (cMap.get(st.getSubject().stringValue()) == null) {
                cMap.put(st.getSubject().stringValue(),
                        new HashMap<String, Object>());
            }
//            JsonObjectBuilder item = Json.createObjectBuilder();
            HashMap<String, Object> item =
                    cMap.get(st.getSubject().stringValue());
            if (st.getPredicate().equals(SKOS.PREF_LABEL)) {
                item.put("prefLabel", st.getObject().stringValue());
            }
            if (st.getPredicate().equals(SKOS.NOTATION)) {
                item.put("notation", st.getObject().stringValue());
            }
            if (st.getPredicate().equals(SKOS.BROADER)) {
                if (item.get("broader") == null) {
                    item.put("broader",
                            new ArrayList<String>());
                }
                @SuppressWarnings("unchecked")
                ArrayList<String> aList =
                        (ArrayList<String>) item.get("broader");
                aList.add(st.getObject().stringValue());
            }
            if (st.getPredicate().equals(SKOS.NARROWER)) {
                if (item.get("narrower") == null) {
                    item.put("narrower",
                            new ArrayList<String>());
                }
                @SuppressWarnings("unchecked")
                ArrayList<String> aList =
                        (ArrayList<String>) item.get("narrower");
                aList.add(st.getObject().stringValue());
            }
        }

/** getter for concepts text. */
/** @return JsonObject */
        public JsonObject getJsonTree() {
            return job.build();
        }

        /** getter for concepts text. */
        /** @return JsonObject */
                public HashMap<String, HashMap<String, Object>> getCmap() {
                    return cMap;
                }


    }

}
