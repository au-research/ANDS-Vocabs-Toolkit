package au.org.ands.vocabs.toolkit.db;

import javax.persistence.EntityManager;

import au.org.ands.vocabs.toolkit.db.model.Vocabularies;

/** Work with database vocabularies. */
public final class VocabulariesUtils {

    /** Private constructor for a utility class. */
    private VocabulariesUtils() {
    }

    /** Get vocabulary by vocabulary id.
     * @param id vocabulary id
     * @return the task
     */
    public static Vocabularies getVocabularyById(final int id) {
        EntityManager em = DBContext.getEntityManager();
        Vocabularies v = em.find(Vocabularies.class, id);
        em.close();
        return v;
    }



}
