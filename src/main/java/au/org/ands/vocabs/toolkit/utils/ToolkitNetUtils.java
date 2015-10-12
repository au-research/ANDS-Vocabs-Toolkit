/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/** Utility methods for working with the network. */
public final class ToolkitNetUtils {

    /** A shared Client resource, initialized on class loading. */
    private static Client client = ClientBuilder.newClient();

    /** Private constructor for a utility class. */
    private ToolkitNetUtils() {
    }

    /** Get the shared Client resource.
     * @return The shared Client resource.
     */
    public static Client getClient() {
        return client;
    }

    /** Prepare for shutdown. Call this only in webapp context shutdown! */
    public static void doShutdown() {
        client.close();
    }

}
