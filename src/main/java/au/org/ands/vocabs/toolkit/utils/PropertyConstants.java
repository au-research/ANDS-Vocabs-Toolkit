/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.utils;

/** Definition of constants that refer to names of properties used
 * by the Toolkit. Use these values as the parameter of
 * {@link ToolkitProperties#getProperty(String)} and related methods.
 */
public final class PropertyConstants {

    /** Private constructor for a utility class. */
    private PropertyConstants() {
    }

    /* Top-level properties. */

    /** Toolkit version. */
    public static final String TOOLKIT_VERSION =
            "Toolkit.version";

    /** Toolkit version timestamp. */
    public static final String TOOLKIT_VERSIONTIMESTAMP =
            "Toolkit.versionTimestamp";

    /** Toolkit build date. */
    public static final String TOOLKIT_BUILDDATE =
            "Toolkit.buildDate";

    /** Toolkit storage path. */
    public static final String TOOLKIT_STORAGEPATH =
            "Toolkit.storagePath";

    /** Toolkit vocabs path. */
    public static final String TOOLKIT_VOCABSPATH =
            "Toolkit.vocabsPath";

    /** Toolkit temp path. */
    public static final String TOOLKIT_TEMPPATH =
            "Toolkit.tempPath";

    /** Toolkit download prefix. */
    public static final String TOOLKIT_DOWNLOADPREFIX =
            "Toolkit.downloadPrefix";

//  /** Toolkit . */
//  public static final String TOOLKIT_ =
//          "Toolkit.";

    /* Properties for providers. */

    /* Harvesters. */

    /** PoolParty harvester remote URL. */
    public static final String POOLPARTYHARVESTER_REMOTEURL =
            "PoolPartyHarvester.remoteUrl";

    /** PoolParty harvester username. */
    public static final String POOLPARTYHARVESTER_USERNAME =
            "PoolPartyHarvester.username";

    /** PoolParty harvester password. */
    public static final String POOLPARTYHARVESTER_PASSWORD =
            "PoolPartyHarvester.password";

    /** PoolParty harvester default format. */
    public static final String POOLPARTYHARVESTER_DEFAULTFORMAT =
            "PoolPartyHarvester.defaultFormat";

    /** PoolParty harvester default export module. */
    public static final String POOLPARTYHARVESTER_DEFAULTEXPORTMODULE =
            "PoolPartyHarvester.defaultExportModule";

    /* Importers. */

    /** Sesame importer server URL. */
    public static final String SESAMEIMPORTER_SERVERURL =
            "SesameImporter.serverUrl";

    /** Sesame importer SPARQL prefix. */
    public static final String SESAMEIMPORTER_SPARQLPREFIX =
            "SesameImporter.sparqlPrefix";

    /* Publishers. */

    /** SISSVoc spec template. */
    public static final String SISSVOC_SPECTEMPLATE =
            "SISSVoc.specTemplate";

    /** SISSVoc specs path. */
    public static final String SISSVOC_SPECSPATH =
            "SISSVoc.specsPath";

    /** SISSVoc endpoints prefix. */
    public static final String SISSVOC_ENDPOINTSPREFIX =
            "SISSVoc.endpointsPrefix";

    /** SISSVoc template variable DEPLOYPATH. */
    public static final String SISSVOC_VARIABLE_DEPLOYPATH =
            "SISSVoc.variable.DEPLOYPATH";

    /** SISSVoc template variable SERVICE_TITLE. */
    public static final String SISSVOC_VARIABLE_SERVICE_TITLE =
            "SISSVoc.variable.SERVICE_TITLE";

    /** SISSVoc template variable SERVICE_AUTHOR. */
    public static final String SISSVOC_VARIABLE_SERVICE_AUTHOR =
            "SISSVoc.variable.SERVICE_AUTHOR";

    /** SISSVoc template variable SERVICE_AUTHOR_EMAIL. */
    public static final String SISSVOC_VARIABLE_SERVICE_AUTHOR_EMAIL =
            "SISSVoc.variable.SERVICE_AUTHOR_EMAIL";

    /** SISSVoc template variable SERVICE_HOMEPAGE. */
    public static final String SISSVOC_VARIABLE_SERVICE_HOMEPAGE =
            "SISSVoc.variable.SERVICE_HOMEPAGE";

    /** SISSVoc template variable SPARQL_ENDPOINT_PREFIX. */
    public static final String SISSVOC_VARIABLE_SPARQL_ENDPOINT_PREFIX =
            "SISSVoc.variable.SPARQL_ENDPOINT_PREFIX";

    /** SISSVoc template variable HTML_STYLESHEET. */
    public static final String SISSVOC_VARIABLE_HTML_STYLESHEET =
            "SISSVoc.variable.HTML_STYLESHEET";

    /* Transformers */

    /* Yes, this property is not correctly named. The name should not
     * begin with "Toolkit". */
    /** Metadata transform provider metadata rewrite map path. */
    public static final String TOOLKIT_METADATAREWRITEMAPPATH =
            "Toolkit.metadataRewriteMapPath";

//    /** . */
//    public static final String _ =
//            "";

}
