package au.org.ands.vocabs.toolkit.provider.importer;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities for importer providers. */
public final class ImporterProviderUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private ImporterProviderUtils() {
    }

    /** Get the provider based on the name providerType.
     * @param providerType The name of the provider type.
     * @return The provider.
     * @throws ClassNotFoundException If there is no such class
     * @throws InstantiationException If instantiation failed
     * @throws IllegalAccessException If instantiation failed
     */
    public static ImporterProvider getProvider(final String providerType)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        String s = "au.org.ands.vocabs.toolkit.provider.importer."
                + providerType
                + "Provider";

        Class<?> c = Class.forName(s);
        ImporterProvider provider =  (ImporterProvider) c.newInstance();
        if (!(provider instanceof ImporterProvider)) {
            LOGGER.error("getProvider bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type Provider");
            provider = null;
        }
        return provider;
    }

}
