/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.utils;

import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

/** Definition of constants that refer to names of properties used
 * by the Toolkit for testing. Use these values as the parameter of
 * {@link ToolkitProperties#getProperty(String)} and related methods.
 */
public final class TestPropertyConstants {

    /** Private constructor for a utility class. */
    private TestPropertyConstants() {
    }

    /* Top-level properties. */

    /** The port for the embedded Tomcat that hosts Sesame. */
    public static final String TEST_TOMCAT_PORT =
            "test.Tomcat.port";

    /** The directory to use for the embedded Tomcat work area.
     * Beware: any existing directory will be wiped during test!
     */
    public static final String TEST_TOMCAT_DIRECTORY =
            "test.Tomcat.directory";

    /** The context path for the embedded Tomcat's Sesame . */
    public static final String TEST_TOMCAT_SESAME_CONTEXT =
            "test.Tomcat.sesame.context";

    /** The path to the WAR file for Sesame. */
    public static final String TEST_TOMCAT_SESAME_WAR =
            "test.Tomcat.sesame.war";

//    /** The  for the embedded Tomcat . */
//    public static final String TEST_TOMCAT_ =
//            "";

}
