package au.org.ands.vocabs.toolkit.provider.harvest;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.tasks.TaskInfo;
import au.org.ands.vocabs.toolkit.tasks.TaskStatus;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

import com.fasterxml.jackson.databind.JsonNode;

/** Harvest provider for files and directories. */
public class FileHarvestProvider extends HarvestProvider {

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

    /** Do a harvest. Update the message parameter with the result
     * of the harvest.
     * @param filePath The path to the file or directory to be harvested.
     * @param outputPath The directory in which to store output files.
      * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    public final boolean getHarvestFiles(final String filePath,
            final String outputPath,
            final HashMap<String, String> results) {
        ToolkitFileUtils.requireDirectory(outputPath);
        Path filePathPath = Paths.get(filePath);
        Path outputPathPath = Paths.get(outputPath);
        if (Files.isDirectory(filePathPath)) {
            logger.debug("Harvesting file(s) from directory" + filePath);
            try (DirectoryStream<Path> stream =
                    Files.newDirectoryStream(filePathPath)) {
                for (Path entry: stream) {
                    // Only harvest files. E.g., no recursive
                    // directory searching.
                    if (Files.isRegularFile(entry)) {
                        logger.debug("Harvesting file:" + entry.toString());
                        Files.copy(entry,
                                outputPathPath.resolve(entry.getFileName()),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (DirectoryIteratorException
                    | IOException ex) {
                results.put(TaskStatus.EXCEPTION,
                        "Exception in getHarvestFiles while copying file");
                logger.error("Exception in getHarvestFiles while copying file:",
                        ex);
                return false;
            }
        } else {
            logger.debug("Harvesting file: " + filePath);
            try {
                Files.copy(filePathPath,
                        outputPathPath.resolve(filePathPath.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                results.put(TaskStatus.EXCEPTION,
                        "Exception in getHarvestFiles while copying file");
                logger.error("Exception in getHarvestFiles while copying file:",
                        e);
                return false;
            }
        }
        // If we reached here, success, so return true.
        return true;
    }

    @Override
    public final boolean harvest(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        if (subtask.get("file_path") == null
                || subtask.get("file_path").textValue().isEmpty()) {
            TasksUtils.updateMessageAndTaskStatus(logger, taskInfo.getTask(),
                    results, TaskStatus.ERROR,
                    "No file_path specified. Nothing to do.");
            return false;
        }

        String filePath = subtask.get("file_path").textValue();
        return getHarvestFiles(filePath,
                TasksUtils.getTaskHarvestOutputPath(taskInfo),
                results);
    }

    /** Not implemented for this provider. Returns null.
     */
    @Override
    public final HashMap<String, String> getMetadata(final String filePath) {
        return null;
    }


}
