/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.restlet;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.DBContext;
import au.org.ands.vocabs.toolkit.db.model.Todo;

/** Testing restlet. */
@Path("testingDB")
public class TestRestletDB1 {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** getMessage.
     * @return the message. */
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final String getMessage() {
        logger.info("Running TestRestletDB1.getMessage().");

        // Read the existing entries and log
        Query q = DBContext.getEntityManager().
                createQuery("select t from Todo t");
        @SuppressWarnings("unchecked")
        List<Todo> todoList = q.getResultList();
        for (Todo todo : todoList) {
          logger.info(todo.toString());
        }
        logger.info("Size: " + todoList.size());

        return "Hello World! Again!";

        }


}
