/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.publish;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities for publisher providers. */
public final class PublishProviderUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private PublishProviderUtils() {
    }

    /** Get the provider based on the name providerType.
     * @param providerType The name of the provider type.
     * @return The provider.
     * @throws ClassNotFoundException If there is no such class
     * @throws InstantiationException If instantiation failed
     * @throws IllegalAccessException If instantiation failed
     */
    public static PublishProvider getProvider(final String providerType)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        String s = "au.org.ands.vocabs.toolkit.provider.publish."
                + providerType
                + "PublishProvider";

        Class<?> c = Class.forName(s);
        PublishProvider provider =  (PublishProvider) c.newInstance();
        if (!(provider instanceof PublishProvider)) {
            LOGGER.error("getProvider bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type Provider");
            provider = null;
        }
        return provider;
    }

}
