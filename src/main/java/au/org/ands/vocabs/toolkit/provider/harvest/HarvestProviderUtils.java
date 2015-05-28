package au.org.ands.vocabs.toolkit.provider.harvest;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utilities for Providers. */
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
                + "Provider";

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

    // public static HarvesterHandler getHarvestHandler(final UriInfo info)
    //   throws ClassNotFoundException, InstantiationException,
    //   IllegalAccessException
    //    {
    //        HarvesterHandler hh = null;
    //            String provider_type = info.getQueryParameters().
    //            getFirst("provider_type");
    //        String s = "au.org.ands.vocabs.toolkit.harvester."
    //        + provider_type
    //        + "Harvester";
    //
    //        Class<?> c = Class.forName(s);
    //        Constructor<?> constructor;
    //        try {
    //            constructor = c.getConstructor(UriInfo.class);
    //        hh =  (HarvesterHandler)constructor.newInstance(info);
    //        if (!(hh instanceof HarvesterHandler))
    //        {
    //            logger.error("bad class:" + hh.getClass().getName()
    //             + ". Class not of type HarvestThread");
    //            hh = null;
    //        }
    //        } catch (NoSuchMethodException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        } catch (SecurityException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        } catch (IllegalArgumentException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        } catch (InvocationTargetException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //        return hh;
    //    }

    //    public static void main(final String[] args) {
    //        // TODO Auto-generated method stub
    //        String harvestId = "1";
    //        String remoteUrl = "https://ands.poolparty.biz/PoolParty/";
    //        String providerType = "PoolParty";
    //        Harvester h = new Harvester();
    //        try{
    //        HarvesterHandler hh = h.getHarvestHandler(harvestId,
    //            remoteUrl, providerType);
    //        hh.harvest();
    //        }
    //        catch(Exception e){
    //            h.logger.error("bad class:" + e.toString());
    //        }
    //        System.out.print("Finished harvesting\n");
    //    }

}
