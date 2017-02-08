/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/** Base class for Arquillian tests. Defines the standard deployment. */
@Test(groups = "arquillian")
public class ArquillianBaseTest extends Arquillian {

    /** Logger for this class. */
    private static Logger logger;

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
    }

    /** Path to test resources that are to be deployed. */
    private static final String RESOURCES_DEPLOY_PATH =
            "src/test/resources/deploy";

    /* This is what is output by Tomcat on context shutdown
     * if the MySQL driver is included in the deployment (or in
     * the classpath, for that matter). Why?
     * The test output does not seem to be affected, but it is a worry.
     * I haven't been able to find an instance of a web page that
     * talks about this.
     *
     *
     *  [testng] SEVERE: The web application [/test] created a ThreadLocal
     *  with key of type [java.lang.InheritableThreadLocal]
     *  (value [java.lang.InheritableThreadLocal@5a6482a9]) and a value
     *  of type [org.testng.internal.TestResult] (value [[TestResult name=""
     *  status=SUCCESS method=AllArquillianTests.testGetAllTasks()[pri:0,
     *  instance:au.org.ands.vocabs.toolkit.test.arquillian.
     *    AllArquillianTests@7ec71837] output={null}]]) but failed to
     *  remove it when the web application was stopped.
     *  Threads are going to be renewed over time to try and avoid
     *  a probable memory leak.
     */


    /** Create deployment for testing.
     * The deployment is a WAR file, that contains most of the
     * source classes directly under WEB-INF/classes.
     * There are two persistence units; each of these is packaged
     * into a JAR file. (Each JAR file contains its own
     * persistence.xml, and the entity classes that belong to
     * the persistence unit.)
     * Note that for running under Tomcat, we rely on Hibernate
     * generously providing more that it is required to in (what is
     * properly) a Java SE environment, i.e., that it does the
     * correct entity scanning without the need to specify each
     * class in persistence.xml.
     * @return The WebArchive to be deployed for testing.
     */
    @Deployment
    public static WebArchive createTestArchive() {
        // Create WAR, and add all classes except those in packages
        // that we provide as JAR files.
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true,
                        Filters.exclude("/au/org/ands/vocabs/"
                                + "toolkit/db/model/.*.class|"
                                + "/au/org/ands/vocabs/"
                                + "registry/db/entity/.*.class"),
                        "au.org.ands.vocabs");
        try {
            JavaArchive toolkitDbModelJar =
                    ShrinkWrap.create(JavaArchive.class,
                            "toolkit-db-model.jar");
            toolkitDbModelJar.addPackage("au.org.ands.vocabs.toolkit.db.model");
            toolkitDbModelJar.addAsManifestResource(new File(
                    "src/main/java/au/org/ands/vocabs/toolkit/db/model/"
                            + "META-INF/persistence.xml"));
            toolkitDbModelJar.addManifest();
            logger.info("toolkitDbModelJar = "
                    + toolkitDbModelJar.toString(Formatters.VERBOSE));
            war.addAsLibrary(toolkitDbModelJar);

            JavaArchive registryDbModelJar =
                    ShrinkWrap.create(JavaArchive.class,
                            "registry-db-model.jar");
            registryDbModelJar.addPackage(
                    "au.org.ands.vocabs.registry.db.entity");
            registryDbModelJar.addAsManifestResource(new File(
                    "src/main/java/au/org/ands/vocabs/registry/db/entity/"
                            + "META-INF/persistence.xml"));
            registryDbModelJar.addManifest();
            logger.info("registryDbModelJar = "
                    + registryDbModelJar.toString(Formatters.VERBOSE));
            war.addAsLibrary(registryDbModelJar);

            // Add all the JAR files from the lib directory.
            Files.walk(Paths.get("lib"))
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".jar"))
                .filter(p -> !p.getParent().getFileName().
                        toString().startsWith("javamelody"))
                // We need the MySQL driver for testing
                // au.org.ands.vocabs.toolkit.utils.RewriteCurrent;
                // it uses
                // com.mysql.jdbc.exceptions.jdbc4.CommunicationsException.
                // However, RewriteCurrent runs as a _standalone_ app,
                // not inside the container. So in fact we don't
                // need the MySQL driver here, for now.
                // If in future we need the driver, comment out the next
                // two lines. HOWEVER: there is a side effect!
                // See comment above that includes the output on
                // context shutdown when this is commented out.
                .filter(p -> !p.getParent().getFileName().
                        toString().startsWith("mysql"))
                .forEach(p -> war.addAsLibrary(p.toFile()));
            // Add all the needed JAR files from the libtest directory.
            Files.walk(Paths.get("libtest"))
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".jar"))
                .forEach(p -> war.addAsLibrary(p.toFile()));
            // Add META-INF content
            Files.walk(Paths.get("WebContent/META-INF"))
                .filter(Files::isRegularFile)
                .forEach(p -> war.addAsManifestResource(p.toFile()));
            // Add WEB-INF content
            Files.walk(Paths.get("WebContent/WEB-INF"))
                .filter(Files::isRegularFile)
                .forEach(p -> war.addAsWebInfResource(p.toFile()));
            // Add test data
            Files.walk(Paths.get(RESOURCES_DEPLOY_PATH))
                .filter(Files::isRegularFile)
                .forEach(p -> war.addAsResource(p.toFile(),
                        p.toString().substring(
                                RESOURCES_DEPLOY_PATH.length())));
            // Uncomment the following, if log4j configuration required.
            //war.addAsResource(new File("conf/logging.properties"),
            //        "logging.properties");
            // Logback logging configuration.
            war.addAsResource(new File("conf-test/logback-test.xml"),
                    "logback.xml");
            //war.addAsResource(new File("conf/toolkit-h2.properties"),
            //        "toolkit.properties");
            try {
                // Optional resource.
                war.addAsResource(new File("conf/toolkit-h2.properties"),
                        "toolkit-h2.properties");
            } catch (IllegalArgumentException e) {
                // No problem if these files don't exist.
            }
            try {
                // Optional resource.
                war.addAsResource(new File("conf/registry-h2.properties"),
                        "registry-h2.properties");
            } catch (IllegalArgumentException e) {
                // No problem if these files don't exist.
            }
            try {
                // Optional resource.
                war.addAsResource(new File("conf/toolkit-h2-bamboo.properties"),
                        "toolkit-h2-bamboo.properties");
            } catch (IllegalArgumentException e) {
                // No problem if these files don't exist.
            }
            try {
                // Optional resource.
                war.addAsResource(
                        new File("conf/registry-h2-bamboo.properties"),
                        "registry-h2-bamboo.properties");
            } catch (IllegalArgumentException e) {
                // No problem if these files don't exist.
            }
            war.addAsResource(new File("conf/version.properties"),
                    "version.properties");

        } catch (IOException e) {
            logger.error("Exception during packaging", e);
        }
        logger.info(war.toString(Formatters.VERBOSE));
        return war;
    }

}
