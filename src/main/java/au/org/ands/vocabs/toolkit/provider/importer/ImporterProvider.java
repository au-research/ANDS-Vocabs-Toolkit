/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.importer;
import java.util.Collection;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

import au.org.ands.vocabs.toolkit.tasks.TaskInfo;

/** Abstract class representing importer providers. */
public abstract class ImporterProvider {

      /** Return information about the provider.
     * @return The information.
     */
    public abstract Collection<?> getInfo();

    /** Do an import. Update the message parameter with the result
     * of the import.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the import.
     * @return True, iff the import succeeded.
     */
    public abstract boolean doImport(TaskInfo taskInfo,
            JsonNode subtask,
            HashMap<String, String> results);

    /** Do an unimport. Update the message parameter with the result
     * of the unimport.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the unimport.
     * @return True, iff the unimport succeeded.
     */
    public abstract boolean unimport(TaskInfo taskInfo,
            JsonNode subtask,
            HashMap<String, String> results);

}
