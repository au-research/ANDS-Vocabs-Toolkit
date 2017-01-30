/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.test;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Test uploading an RDF file. */
public final class TestUploadRDF {

    /** Private constructor. */
    private TestUploadRDF() {
    }

    /** main program.
     * @param args Command-line arguments. */
    public static void main(final String[] args) {

    Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());
    logger.info("Running TestUploadRDF.");

    String sesameServer =
              "http://vocabs.ands.org.au/repository/openrdf-sesame/";
    RepositoryManager manager;
    try {
        manager = RepositoryProvider.getRepositoryManager(sesameServer);


    String repositoryID = "rifcs16";

    Repository repository = manager.getRepository(repositoryID);

    File file =
            new File("/Users/rwalker/vocab-exports-from-devl/"
                     + "export-rifcs16.rdf");


   RepositoryConnection con = repository.getConnection();
   try {
      con.add(file, "", RDFFormat.RDFXML);

     // con.add(url, url.toString(), RDFFormat.RDFXML);

    } catch (RDFParseException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
          con.close();
       }

    } catch (RepositoryConfigException e) {
        e.printStackTrace();
    } catch (RepositoryException e) {
        e.printStackTrace();
    }

  }


}
