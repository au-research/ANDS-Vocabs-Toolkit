/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.ClientProperties;

/** Utility methods for working with the network. */
public final class ToolkitNetUtils {

    /** A shared Client resource, initialized on class loading.
     * Follows redirects. */
    // According to the documentation, FOLLOW_REDIRECTS is the default.
    // Specify it anyway, just in case that changes.
    private static Client client = ClientBuilder.newClient().
            property(ClientProperties.FOLLOW_REDIRECTS, false);

    /** A shared Client resource, initialized on class loading.
     * Does not follow redirects. */
    private static Client clientNoRedirects = ClientBuilder.newClient().
            property(ClientProperties.FOLLOW_REDIRECTS, false);

    /** Private constructor for a utility class. */
    private ToolkitNetUtils() {
    }

    /** Get the shared Client resource that follows redirects.
     * @return The shared Client resource.
     */
    public static Client getClient() {
        return client;
    }

    /** Get the shared Client resource that does not follow redirects.
     * @return The shared Client resource.
     */
    public static Client getClientNoRedirects() {
        return clientNoRedirects;
    }

    /** Prepare for shutdown. Call this only in webapp context shutdown! */
    public static void doShutdown() {
        client.close();
        clientNoRedirects.close();
    }

}
