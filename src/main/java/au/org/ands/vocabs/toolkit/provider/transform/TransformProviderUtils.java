/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.transform;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities for transform providers. */
public final class TransformProviderUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private TransformProviderUtils() {
    }

    /** Get the provider based on the name providerType.
     * @param providerType The name of the provider type.
     * @return The provider, or null if there is an error during
     *      instantiation.
     */
    public static TransformProvider getProvider(final String providerType) {
        String s = "au.org.ands.vocabs.toolkit.provider.transform."
                + providerType
                + "TransformProvider";
        Class<?> c;
        try {
            c = Class.forName(s);
        } catch (ClassNotFoundException e) {
            LOGGER.error("TransformProviderUtils.getProvider(): "
                    + "no such provider: " + providerType);
            return null;
        }
        TransformProvider provider = null;
        try {
            provider = (TransformProvider) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("TransformProviderUtils.getProvider(): "
                    + "can't instantiate provider class for provider type: "
                    + providerType, e);
            return null;
        }
        if (!(provider instanceof TransformProvider)) {
            LOGGER.error("TransformProviderUtils.getProvider() bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type TransformProvider");
            return null;
        }
        return provider;
    }

}
