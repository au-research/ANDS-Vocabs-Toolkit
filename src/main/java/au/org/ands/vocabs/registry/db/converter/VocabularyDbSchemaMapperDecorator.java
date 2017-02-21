/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.converter;

import au.org.ands.vocabs.registry.db.internal.VocabularyJson;

/** MapStruct mapper from Vocabulary database to schema. */
public abstract class VocabularyDbSchemaMapperDecorator
    implements VocabularyDbSchemaMapper {

    /** The delegate mapper. */
    private final VocabularyDbSchemaMapper delegate;

    /** Constructor that accepts a delegate.
     * @param aDelegate The delegate mapper.
     */
    public VocabularyDbSchemaMapperDecorator(
            final VocabularyDbSchemaMapper aDelegate) {
        delegate = aDelegate;
    }

    /** Decorator method that extends the default mapping behaviour
     * with extraction of the JSON data.
     * (Don't make this method final; it is extended by the implementation
     * class.)
     */
    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public au.org.ands.vocabs.registry.schema.vocabulary201701.Vocabulary
    sourceToTarget(final au.org.ands.vocabs.registry.db.entity.Vocabulary
            source) {
        if (source == null) {
            return null;
        }
        au.org.ands.vocabs.registry.schema.vocabulary201701.Vocabulary
            target = delegate.sourceToTarget(source);
        if (source.getData() != null) {
            VocabularyJson data =
                    JSONSerialization.deserializeStringAsJson(source.getData(),
                            VocabularyJson.class);
            jsonDataIntoTarget(data, target);
        }
        return target;
    }

}
