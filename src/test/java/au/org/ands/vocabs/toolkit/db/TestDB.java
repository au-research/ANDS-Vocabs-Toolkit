package au.org.ands.vocabs.toolkit.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import au.org.ands.vocabs.toolkit.db.model.Todo;

/** Test of database access. */
public final class TestDB {

    /** The name of the persistence unit. */
    private static final String PERSISTENCE_UNIT_NAME = "ANDS-Vocabs-Toolkit";

    /** EntityManagerFactory. */
    private static EntityManagerFactory factory;

    /** Test class. Not instantiated. */
    private TestDB() {
    }

    /** Main method.
     * @param args Command-line arguments.
     */
    public static void main(final String[] args) {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = factory.createEntityManager();
        // Read the existing entries and write to console
        Query q = em.createQuery("select t from Todo t");
        @SuppressWarnings("unchecked")
        List<Todo> todoList = q.getResultList();
        for (Todo todo : todoList) {
            System.out.println(todo);
        }
        System.out.println("Size: " + todoList.size());

        // Create new todo
        em.getTransaction().begin();
        Todo todo = new Todo();
        todo.setSummary("This is a test");
        todo.setDescription("This is a test");
        em.persist(todo);
        em.getTransaction().commit();

        em.close();
    }
}
