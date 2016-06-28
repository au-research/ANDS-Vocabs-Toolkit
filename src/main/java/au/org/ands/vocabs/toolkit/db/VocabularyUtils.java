/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import javax.persistence.EntityManager;

import au.org.ands.vocabs.toolkit.db.model.Vocabulary;

/** Work with database vocabularies. */
public final class VocabularyUtils {

    /** Private constructor for a utility class. */
    private VocabularyUtils() {
    }

    /** Get vocabulary by vocabulary id.
     * @param id vocabulary id
     * @return the vocabulary
     */
    public static Vocabulary getVocabularyById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Vocabulary v = em.find(Vocabulary.class, id);
        em.close();
        return v;
    }

}
