/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.converter;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import au.org.ands.vocabs.registry.db.internal.VersionJson;
import au.org.ands.vocabs.registry.db.internal.VocabularyJson;

/** Utility class that provides serialization and deserialization
 * of JSON data. */
public final class JSONSerialization {

    /** Private constructor for a utility class. */
    private JSONSerialization() {
    }

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Jackson ObjectMapper used for serializing JSON data into Strings.
     * It is initialized by a static block. */
    private static ObjectMapper jsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        // Enable the use of the JAXB annotations in the classes
        // being serialized.
        // Registering this module also has the effect that
        // for a key/value pair, if the value is an empty array,
        // the key/value is omitted. (The NON_NULL serialization
        // inclusion setting doesn't do this.)
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        jsonMapper.registerModule(module);
        // Don't serialize null values.  With this, but without the
        // JaxbAnnotationModule module registration
        // above, empty arrays that are values of a key/value pair
        // _would_ still be serialized.
        jsonMapper.setSerializationInclusion(Include.NON_NULL);
        // We don't really care about alphabetical sorting for
        // persisting values. Rather, we do this only so we
        // can get a canonical representation of related entities.
        // This is relied on by the canonicalizeRelatedEntityJson()
        // method, used by the various migrateRelatedXXX()
        // methods.
        jsonMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,
                true);
    }

    /** Deserialize a String in JSON format into one of our custom
     * JSON storage objects.
     * @param jsonString The String to be deSerialized. It should be
     *      an instance of one of our custom JSON storage objects,
     *      such as {@link VocabularyJson} or {@link VersionJson}.
     * @param <T> Type parameter of the target JSON storage object class.
     * @param jsonClass The target JSON storage object class.
     * @return The deserialization as a JSON storage object of jsonString.
     */
    public static <T> T deserializeStringAsJson(final String jsonString,
            final Class<T> jsonClass) {
        try {
            return jsonMapper.readValue(jsonString, jsonClass);
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize JSON", e);
            return null;
        }
    }

}
