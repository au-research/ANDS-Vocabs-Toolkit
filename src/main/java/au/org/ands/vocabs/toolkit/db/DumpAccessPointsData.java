/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import au.org.ands.vocabs.toolkit.db.model.AccessPoints;
import au.org.ands.vocabs.toolkit.utils.ToolkitProperties;

import com.fasterxml.jackson.databind.JsonNode;

/** Dump the access_points table, in particular, unpacking the contents of the
 * portal_data and toolkit_data columns. */
public final class DumpAccessPointsData {

    /** Access to the Toolkit properties. */
    protected static final Properties PROPS = ToolkitProperties.getProperties();

    /** Private constructor for a utility class. */
    private DumpAccessPointsData() {
    }

    /**
     * Main program.
     * @param args Command-line arguments
     */
    public static void main(final String[] args) {
        List<AccessPoints> aps = AccessPointsUtils.getAllAccessPoints();
        for (AccessPoints ap: aps) {
            System.out.println(ap.getId());
            System.out.println(ap.getVersionId());
            System.out.println(ap.getType());
            String pd = ap.getPortalData();
            String td = ap.getToolkitData();

            System.out.println("portal_data:");
            JsonNode pdJson = TasksUtils.jsonStringToTree(pd);
            Iterator<Entry<String, JsonNode>> pdJsonIterator =
                    pdJson.fields();
            while (pdJsonIterator.hasNext()) {
                Entry<String, JsonNode> entry = pdJsonIterator.next();
                System.out.println(entry.getKey() + "="
                + entry.getValue().asText());
            }

            System.out.println("toolkit_data:");
            JsonNode tdJson = TasksUtils.jsonStringToTree(td);
            Iterator<Entry<String, JsonNode>> tdJsonIterator =
                    tdJson.fields();
            while (tdJsonIterator.hasNext()) {
                Entry<String, JsonNode> entry = tdJsonIterator.next();
                System.out.println(entry.getKey() + "="
                + entry.getValue().asText());
            }

        }
    }


}
