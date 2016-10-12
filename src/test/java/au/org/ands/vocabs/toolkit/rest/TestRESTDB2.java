/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.rest;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.vocabs.toolkit.db.VocabularyUtils;
import au.org.ands.vocabs.toolkit.db.model.Vocabulary;

/** Testing REST web service. */
@Path("testingDB2")
public class TestRESTDB2 {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** getMessage.
     * @param vocabId the vocabulary id.
     * @return the message. */
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public final String getMessage(@QueryParam("vocab_id") final int vocabId) {
        logger.info("Running TestRESTDB2.getMessage().");

        // Get the vocabulary object
        // Read the existing entries and log
        Vocabulary v = VocabularyUtils.getVocabularyById(vocabId);
        if (v == null) {
            logger.info("Vocab not found; id: " + vocabId);
            return "Vocab not found";
        }

        logger.info("Title: " + v.getTitle());
        return "Vocab title is: " + v.getTitle();
    }


}
