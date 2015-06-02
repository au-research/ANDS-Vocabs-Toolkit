package au.org.ands.vocabs.toolkit.provider.transform;
import java.lang.invoke.MethodHandles;

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
     * @return The provider.
     * @throws ClassNotFoundException If there is no such class
     * @throws InstantiationException If instantiation failed
     * @throws IllegalAccessException If instantiation failed
     */
    public static TransformProvider getProvider(final String providerType)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        String s = "au.org.ands.vocabs.toolkit.provider.transform."
                + providerType
                + "TransformProvider";

        Class<?> c = Class.forName(s);
        TransformProvider provider =  (TransformProvider) c.newInstance();
        if (!(provider instanceof TransformProvider)) {
            LOGGER.error("getProvider bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type Provider");
            provider = null;
        }
        return provider;
    }

}
