package au.org.ands.vocabs.registry.api.context;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/** Custom Jackson {@link ObjectMapper} provider for JSON deserialisation.
 * Used by Jackson when generating results from API calls.
 * The customizations applied are:
 * (a) JAXB annotations are taken into account; (b) only non-null values
 * are included (c) only properties with getter methods are taken into account,
 * (d) the values of XmlElementWrapper annotations are used, where given.
 * Note that despite (c), Jackson uses reflection to get values, rather
 * than calling getter methods. The difference is apparent for JAXB-generated
 * properties of List types, in which the getter method initializes the value
 * to an empty list, if the value is null.
 */
@Provider
public class ObjectMapperContextResolver
    implements ContextResolver<ObjectMapper> {

    /** Singleton ObjectMapper for JSON generation. It is OK for
     * this to be a singleton, as ObjectMappers are thread-safe.
     * The value is customized in a static block. */
    private static final ObjectMapper OBJECTMAPPER = new ObjectMapper();

    static {
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        OBJECTMAPPER.registerModule(module);
        OBJECTMAPPER.setSerializationInclusion(Include.NON_NULL);
        // For now, do serialize empty lists as empty lists. Revisit
        // later, if necessary.
//        OBJECTMAPPER.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS,
//                false);
        OBJECTMAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);
        OBJECTMAPPER.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
    }

    /** Returns the (singleton) ObjectMapper instance to be used for
     * JSON deserialization.
     * @return The singleton ObjectMapper instance.
     */
    @SuppressWarnings("checkstyle:DesignForExtension")
    @Override
    public ObjectMapper getContext(final Class<?> type) {
        return OBJECTMAPPER;
    }

}
