/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.test;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.inferencer.fc.config.
            ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;


/** Test creating a repository. */
public final class TestCreateRepo {

    /** Private constructor. */
    private TestCreateRepo() {
    }

    /** main program.
     * @param args Command-line arguments. */
  public static void main(final String[] args) {

    String sesameServer =
              "http://vocabs.ands.org.au/repository/openrdf-sesame/";
    RepositoryManager manager;
    try {
        manager = RepositoryProvider.getRepositoryManager(sesameServer);

    // create a configuration for the SAIL stack
    boolean persist = true;
    SailImplConfig backendConfig = new MemoryStoreConfig(persist);

    // stack an inferencer config on top of our backend-config
    backendConfig = new ForwardChainingRDFSInferencerConfig(backendConfig);

    // create a configuration for the repository implementation
    RepositoryImplConfig repositoryTypeSpec =
          new SailRepositoryConfig(backendConfig);

    String repositoryID = "rifcs16";
    String repositoryTitle = "RIFCS v1.6 Vocabularies";

    RepositoryConfig repConfig =
          new RepositoryConfig(repositoryID, repositoryTitle,
                  repositoryTypeSpec);
    manager.addRepositoryConfig(repConfig);

    // Repository repository = manager.getRepository(repositoryID);
    } catch (RepositoryConfigException e) {
        e.printStackTrace();
    } catch (RepositoryException e) {
        e.printStackTrace();
    }


  }


}
