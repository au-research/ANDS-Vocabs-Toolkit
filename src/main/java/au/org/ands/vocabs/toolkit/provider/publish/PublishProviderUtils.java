/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.publish;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

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
     * @return The provider, or null if there is an error during
     *      instantiation.
     */
    public static PublishProvider getProvider(final String providerType) {
        String s = "au.org.ands.vocabs.toolkit.provider.publish."
                + providerType
                + "PublishProvider";
        Class<?> c;
        try {
            c = Class.forName(s);
        } catch (ClassNotFoundException e) {
            LOGGER.error("PublishProviderUtils.getProvider(): "
                    + "no such provider: " + providerType);
            return null;
        }
        PublishProvider provider = null;
        try {
            provider = (PublishProvider) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("PublishProviderUtils.getProvider(): "
                    + "can't instantiate provider class for provider type: "
                    + providerType, e);
            return null;
        }
        if (!(provider instanceof PublishProvider)) {
            LOGGER.error("PublishProviderUtils.getProvider() bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type PublishProvider");
            return null;
        }
        return provider;
    }

}
