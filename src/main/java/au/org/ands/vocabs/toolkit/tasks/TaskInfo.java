package au.org.ands.vocabs.toolkit.tasks;

import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Versions;
import au.org.ands.vocabs.toolkit.db.model.Vocabularies;

/** Class encapsulating all information about a task. */
public class TaskInfo {

    /** Task object for this task. */
    private Task task;

    /** Vocabularies object for this task. */
    private Vocabularies vocabulary;

    /** Versions object for this task. */
    private Versions version;

    /** Constructor.
     * @param aTask The Task object
     * @param aVocabularies The Vocabularies object
     * @param aVersions The Versions object
     */
    public TaskInfo(final Task aTask, final Vocabularies aVocabularies,
            final Versions aVersions) {
        task = aTask;
        vocabulary = aVocabularies;
        version = aVersions;
    }

    /** Getter for the Task object.
     * @return The Task object.
     */
    public final Task getTask() {
        return task;
    }

    /** Getter for the Vocabularies object.
     * @return The Vocabularies object.
     */
    public final Vocabularies getVocabulary() {
        return vocabulary;
    }

    /** Getter for the Versions object.
     * @return The Versions object.
     */
    public final Versions getVersion() {
        return version;
    }

}
