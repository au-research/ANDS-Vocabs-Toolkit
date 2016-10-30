/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.importer;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

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
     * @return The provider, or null if there is an error during
     *      instantiation.
     */
    public static ImporterProvider getProvider(final String providerType) {
        String s = "au.org.ands.vocabs.toolkit.provider.importer."
                + providerType
                + "ImporterProvider";
        Class<?> c;
        try {
            c = Class.forName(s);
        } catch (ClassNotFoundException e) {
            LOGGER.error("ImporterProviderUtils.getProvider(): "
                    + "no such provider: " + providerType);
            return null;
        }
        ImporterProvider provider = null;
        try {
            provider = (ImporterProvider) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("ImporterProviderUtils.getProvider(): "
                    + "can't instantiate provider class for provider type: "
                    + providerType, e);
            return null;
        }
        if (!(provider instanceof ImporterProvider)) {
            LOGGER.error("ImporterProvicerUtils.getProvider() bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type ImporterProvider");
            return null;
        }
        return provider;
    }

}
