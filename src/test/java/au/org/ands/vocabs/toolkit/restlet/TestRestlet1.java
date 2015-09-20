/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.restlet;

import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

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

    /** Test getting properties.
     * @param logger Logger. */
    public final void testProperties(final Logger logger) {
        Properties props = ToolkitProperties.getProperties();
        Enumeration<?> e = props.propertyNames();
        logger.info("All toolkit properties:");
        while (e.hasMoreElements()) {
          String key = (String) e.nextElement();
          logger.info(key + " -- " + props.getProperty(key));
        }
        logger.info("End of toolkit properties.");
    }


}
