/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.utils;

/** Definition of constants that refer to names of properties used
 * by the Registry. Use these values as the parameter of
 * {@link RegistryProperties#getProperty(String)} and related methods.
 */
public final class PropertyConstants {

    /** Private constructor for a utility class. */
    private PropertyConstants() {
    }

    /* Top-level properties. */

    /** Registry version. */
    public static final String REGISTRY_VERSION =
            "Registry.version";

    /** Registry version timestamp. */
    public static final String REGISTRY_VERSIONTIMESTAMP =
            "Registry.versionTimestamp";

    /** Registry build date. */
    public static final String REGISTRY_BUILDDATE =
            "Registry.buildDate";

    /** Registry storage path. */
    public static final String REGISTRY_STORAGEPATH =
            "Registry.storagePath";

    /** Registry vocabs path. */
    public static final String REGISTRY_VOCABSPATH =
            "Registry.vocabsPath";

    /** Registry temp path. */
    public static final String REGISTRY_TEMPPATH =
            "Registry.tempPath";

    /** Registry download prefix. */
    public static final String REGISTRY_DOWNLOADPREFIX =
            "Registry.downloadPrefix";

//  /** Registry . */
//  public static final String REGISTRY_ =
//          "Registry.";

    /* Configure the swagger view of the API. */

    /** Swagger host. */
    public static final String REGISTRY_SWAGGER_HOST =
            "Registry.swagger.host";
    /** Swagger basePath. */
    public static final String REGISTRY_SWAGGER_BASEPATH =
            "Registry.swagger.basePath";

}
