/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.converter;

import au.org.ands.vocabs.registry.db.internal.VersionJson;

/** MapStruct mapper from Version database to schema. */
public abstract class VersionDbSchemaMapperDecorator
    implements VersionDbSchemaMapper {

    /** The delegate mapper. */
    private final VersionDbSchemaMapper delegate;

    /** Constructor that accepts a delegate.
     * @param aDelegate The delegate mapper.
     */
    public VersionDbSchemaMapperDecorator(
            final VersionDbSchemaMapper aDelegate) {
        delegate = aDelegate;
    }

    /** Decorator method that extends the default mapping behaviour
     * with extraction of the JSON data.
     * (Don't make this method final; it is extended by the implementation
     * class.)
     */
    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public au.org.ands.vocabs.registry.schema.vocabulary201701.Version
    sourceToTarget(final au.org.ands.vocabs.registry.db.entity.Version
            source) {
        if (source == null) {
            return null;
        }
        au.org.ands.vocabs.registry.schema.vocabulary201701.Version
            target = delegate.sourceToTarget(source);
        if (source.getData() != null) {
            VersionJson data =
                    JSONSerialization.deserializeStringAsJson(source.getData(),
                            VersionJson.class);
            jsonDataIntoTarget(data, target);
        }
        return target;
    }

}
