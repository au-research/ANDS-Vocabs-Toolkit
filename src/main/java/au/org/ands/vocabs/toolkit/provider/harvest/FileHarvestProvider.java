/** See the file "LICENSE" for the full license governing this code. */
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

import au.org.ands.vocabs.toolkit.db.AccessPointsUtils;
import au.org.ands.vocabs.toolkit.db.TasksUtils;
import au.org.ands.vocabs.toolkit.db.model.AccessPoints;
import au.org.ands.vocabs.toolkit.db.model.Versions;
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
     * @param version The version to which access points are to be added.
     * @param format The format of the file(s) to be harvested.
     * @param filePath The path to the file or directory to be harvested.
     * @param outputPath The directory in which to store output files.
     * @param results HashMap representing the result of the harvest.
     * @return True, iff the harvest succeeded.
     */
    public final boolean getHarvestFiles(
            final Versions version,
            final String format,
            final String filePath,
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
                        Path target = outputPathPath.resolve(
                                entry.getFileName());
                        Files.copy(entry, target,
                                StandardCopyOption.REPLACE_EXISTING);
                        AccessPointsUtils.createFileAccessPoint(version,
                                format, target);
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
                Path target = outputPathPath.resolve(
                        filePathPath.getFileName());
                Files.copy(filePathPath, target,
                        StandardCopyOption.REPLACE_EXISTING);
                AccessPointsUtils.createFileAccessPoint(version,
                        format, target);
            } catch (IOException e) {
                results.put(TaskStatus.EXCEPTION,
                        "Exception in getHarvestFiles while copying file");
                logger.error(
                        "Exception in getHarvestFiles while copying file:", e);
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
        String format;
        if (subtask.get("format") == null
                || subtask.get("format").textValue().isEmpty()) {
            format = null;
        } else {
            format = subtask.get("format").textValue();
        }
        return getHarvestFiles(taskInfo.getVersion(), format, filePath,
                ToolkitFileUtils.getTaskHarvestOutputPath(taskInfo),
                results);
    }

    /** Remove any file access points for the version.
     * @param taskInfo The TaskInfo object describing the entire task.
     * @param subtask The details of the subtask
     * @param results HashMap representing the result of the unharvest.
     */
    @Override
    public final void unharvestProviderSpecific(final TaskInfo taskInfo,
            final JsonNode subtask,
            final HashMap<String, String> results) {
        AccessPointsUtils.deleteAccessPointsForVersionAndType(
                taskInfo.getVersion().getId(), AccessPoints.FILE_TYPE);
    }

    /** Not implemented for this provider. Returns null.
     */
    @Override
    public final HashMap<String, String> getMetadata(final String filePath) {
        return null;
    }


}
