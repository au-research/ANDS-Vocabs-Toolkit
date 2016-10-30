/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.provider.harvest;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities for harvester providers. */
public final class HarvestProviderUtils {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Private constructor for a utility class. */
    private HarvestProviderUtils() {
    }

    /** Get the provider based on the name providerType.
     * @param providerType The name of the provider type.
     * @return The provider, or null if there is an error during
     *      instantiation.
     */
    public static HarvestProvider getProvider(final String providerType) {
        String s = "au.org.ands.vocabs.toolkit.provider.harvest."
                + providerType
                + "HarvestProvider";
        Class<?> c;
        try {
            c = Class.forName(s);
        } catch (ClassNotFoundException e) {
            LOGGER.error("HarvestProviderUtils.getProvider(): "
                    + "no such provider: " + providerType);
            return null;
        }
        HarvestProvider provider = null;
        try {
            provider = (HarvestProvider) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOGGER.error("HarvestProviderUtils.getProvider(): "
                    + "can't instantiate provider class for provider type: "
                    + providerType, e);
            return null;
        }
        if (!(provider instanceof HarvestProvider)) {
            LOGGER.error("HarvestProviderUtils.getProvider() bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type HarvestProvider");
            return null;
        }
        return provider;
    }

}
