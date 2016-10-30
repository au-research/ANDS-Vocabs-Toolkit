/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.backup;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities for backup providers. */
public final class BackupProviderUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private BackupProviderUtils() {
    }

    /** Get the provider based on the name providerType.
     * @param providerType The name of the provider type.
     * @return The provider, or null if there is an error during
     *      instantiation.
     */
    public static BackupProvider getProvider(final String providerType) {
        String s = "au.org.ands.vocabs.toolkit.provider.backup."
                + providerType
                + "BackupProvider";
        Class<?> c;
        try {
            c = Class.forName(s);
        } catch (ClassNotFoundException e) {
            LOGGER.error("BackupProviderUtils.getProvider(): "
                    + "no such provider: " + providerType);
            return null;
        }
        BackupProvider provider = null;
        try {
            provider = (BackupProvider) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("BackupProviderUtils.getProvider(): "
                    + "can't instantiate provider class for provider type: "
                    + providerType, e);
            return null;
        }
        if (!(provider instanceof BackupProvider)) {
            LOGGER.error("BackupProvider.getProvider() bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type BackupProvider");
            return null;
        }
        return provider;
    }

}
