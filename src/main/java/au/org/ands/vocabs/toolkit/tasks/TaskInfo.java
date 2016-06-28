/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.tasks;

import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Version;
import au.org.ands.vocabs.toolkit.db.model.Vocabulary;

/** Class encapsulating all information about a task. */
public class TaskInfo {

    /** Task object for this task. */
    private Task task;

    /** Vocabulary object for this task. */
    private Vocabulary vocabulary;

    /** Version object for this task. */
    private Version version;

    /** Constructor.
     * @param aTask The Task object
     * @param aVocabulary The Vocabulary object
     * @param aVersion The Version object
     */
    public TaskInfo(final Task aTask, final Vocabulary aVocabulary,
            final Version aVersion) {
        task = aTask;
        vocabulary = aVocabulary;
        version = aVersion;
    }

    /** Getter for the Task object.
     * @return The Task object.
     */
    public final Task getTask() {
        return task;
    }

    /** Getter for the Vocabulary object.
     * @return The Vocabulary object.
     */
    public final Vocabulary getVocabulary() {
        return vocabulary;
    }

    /** Getter for the Version object.
     * @return The Version object.
     */
    public final Version getVersion() {
        return version;
    }

}
