/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;

/**
 * Transform provider for running a SPARQL update on a Sesame repository. In
 * case we need to do a transform provider that operates on "raw" RDF files, see
 * https://groups.google.com/d/msg/sesame-users/fJctKX_vNEs/a1gm7rqD3L0J for how
 * to do it.
 */
public class SesameSPARQLUpdateTransformProvider extends TransformProvider {

    /** Logger for this class. */
    private final Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    @Override
    public final String getInfo() {
        // Not implemented.
        return null;
    }

    @Override
    public final boolean transform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        if (subtask.get("sparql_update") == null) {
            TaskUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "No SPARQL update statement specified.");
            return false;
        }
        String sparqlUpdateText = subtask.get("sparql_update").asText();
        return SesameTransformUtils.runUpdate(taskInfo, results,
                sparqlUpdateText, new HashMap<String, Value>());
    }

    @Override
    public final boolean untransform(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        return false;
    }

}
