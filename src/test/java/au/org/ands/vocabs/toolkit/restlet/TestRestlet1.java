package au.org.ands.vocabs.toolkit.restlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Testing restlet. */
@Path("testing")
public class TestRestlet1 {

    /** Servlet context. */
    @Context
    private ServletContext context;

    /** getMessage.
     * @return the message. */
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final String getMessage() {
        Logger logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        logger.info("Running TestRestlet1.getMessage().");
        logger.info("My path is: " + context.getRealPath("."));
        testProperties(logger);

        return "Hello World! Again!";
        }

    /** getMessage.
     * @return the message. */
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public final String getMessageJson() {
        return "{\"hello\":\"Hello JSON!\"}";
        }

    /** Test getting a property.
     * @param logger Logger. */
    public final void testProperties(final Logger logger) {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(context.getRealPath("test.properties"));

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            logger.info(prop.getProperty("test.property1"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
