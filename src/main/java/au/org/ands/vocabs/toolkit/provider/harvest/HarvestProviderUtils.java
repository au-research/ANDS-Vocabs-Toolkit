package au.org.ands.vocabs.toolkit.provider.harvest;
import java.lang.invoke.MethodHandles;

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
     * @return The provider.
     * @throws ClassNotFoundException If there is no such class
     * @throws InstantiationException If instantiation failed
     * @throws IllegalAccessException If instantiation failed
     */
    public static HarvestProvider getProvider(final String providerType)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        String s = "au.org.ands.vocabs.toolkit.provider.harvest."
                + providerType
                + "HarvestProvider";

        Class<?> c = Class.forName(s);
        HarvestProvider provider =  (HarvestProvider) c.newInstance();
        if (!(provider instanceof HarvestProvider)) {
            LOGGER.error("getProvider bad class:"
                    + provider.getClass().getName()
                    + ". Class not of type Provider");
            provider = null;
        }
        return provider;
    }

}
