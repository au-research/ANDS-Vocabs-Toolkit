/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.converter;

import java.util.HashMap;

import org.mapstruct.MappingTarget;

import au.org.ands.vocabs.registry.db.internal.ApApiSparql;
import au.org.ands.vocabs.registry.db.internal.ApCommon;
import au.org.ands.vocabs.registry.db.internal.ApFile;
import au.org.ands.vocabs.registry.db.internal.ApSesameDownload;
import au.org.ands.vocabs.registry.db.internal.ApSissvoc;
import au.org.ands.vocabs.registry.db.internal.ApWebPage;
import au.org.ands.vocabs.registry.enums.AccessPointType;

/** MapStruct mapper from AccessPoint database to schema. */
@SuppressWarnings("checkstyle:DesignForExtension")
public abstract class AccessPointDbSchemaMapperDecorator
    implements AccessPointDbSchemaMapper {

    /** Map from AccessPointType to the corresponding subclass of the
     * database JSON ApCommon class that represents it. */
    private HashMap<AccessPointType, Class<? extends ApCommon>>
        dbJsonClassMap = new HashMap<>();

    /** Map from AccessPointType to the corresponding subclass of the
     * schema JSON ApCommon class that represents it. */
    private HashMap<AccessPointType, Class<? extends
            au.org.ands.vocabs.registry.schema.vocabulary201701.ApCommon>>
        schemaJsonClassMap = new HashMap<>();

    /** The delegate mapper. */
    private final AccessPointDbSchemaMapper delegate;

    /** Constructor that accepts a delegate.
     * @param aDelegate The delegate mapper.
     */
    public AccessPointDbSchemaMapperDecorator(
            final AccessPointDbSchemaMapper aDelegate) {
        delegate = aDelegate;

        dbJsonClassMap.put(AccessPointType.API_SPARQL, ApApiSparql.class);
        dbJsonClassMap.put(AccessPointType.FILE, ApFile.class);
        dbJsonClassMap.put(AccessPointType.SESAME_DOWNLOAD,
                ApSesameDownload.class);
        dbJsonClassMap.put(AccessPointType.SISSVOC, ApSissvoc.class);
        dbJsonClassMap.put(AccessPointType.WEB_PAGE, ApWebPage.class);

        schemaJsonClassMap.put(AccessPointType.API_SPARQL,
                au.org.ands.vocabs.registry.schema.vocabulary201701.
                ApApiSparql.class);
        schemaJsonClassMap.put(AccessPointType.FILE,
                au.org.ands.vocabs.registry.schema.vocabulary201701.
                ApFile.class);
        schemaJsonClassMap.put(AccessPointType.SESAME_DOWNLOAD,
                au.org.ands.vocabs.registry.schema.vocabulary201701.
                ApSesameDownload.class);
        schemaJsonClassMap.put(AccessPointType.SISSVOC,
                au.org.ands.vocabs.registry.schema.vocabulary201701.
                ApSissvoc.class);
        schemaJsonClassMap.put(AccessPointType.WEB_PAGE,
                au.org.ands.vocabs.registry.schema.vocabulary201701.
                ApWebPage.class);
    }

    /** Decorator method that extends the default mapping behaviour
     * with extraction of the JSON data.
     * (Don't make this method final; it is extended by the implementation
     * class.)
     */
    @Override
    public au.org.ands.vocabs.registry.schema.vocabulary201701.AccessPoint
    sourceToTarget(final au.org.ands.vocabs.registry.db.entity.AccessPoint
            source) {
        if (source == null) {
            return null;
        }
        au.org.ands.vocabs.registry.schema.vocabulary201701.AccessPoint
            target = delegate.sourceToTarget(source);
        if (source.getData() != null) {
            ApCommon data;
            Class<? extends ApCommon> dbTargetClass =
                    dbJsonClassMap.get(source.getType());
            data = JSONSerialization.deserializeStringAsJson(source.getData(),
                            dbTargetClass);
            Class<? extends au.org.ands.vocabs.registry.schema.
                    vocabulary201701.ApCommon> schemaTargetClass =
                    schemaJsonClassMap.get(source.getType());
            try {
                au.org.ands.vocabs.registry.schema.
                    vocabulary201701.ApCommon ap =
                    schemaTargetClass.newInstance();
                target.setAP(ap);
                jsonDataIntoTarget(data, ap);
            } catch (InstantiationException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return target;
    }

    /** Hand-coded mapper from ApCommon into an existing
     * schema object. It is basically a big "switch" statement
     * that delegates to the mappers for the various subclasses
     * of ApCommon.
     * It's a shame to have to code it this way. See
     * https://github.com/mapstruct/mapstruct/issues/131
     * for an open issue for MapStruct that would simplify the
     * implementation of this method.
     * @param data The AccessPoint JSON data from the database.
     * @param target The schema version of the access point to be updated.
     */
    // See https://github.com/checkstyle/checkstyle/issues/3850
    // for the defect report that means I need the Checkstyle suppression.
    // Follow that issue to see when/how the suppression can be removed.
    @SuppressWarnings("checkstyle:WhitespaceAfter")
    void jsonDataIntoTarget(final ApCommon data,
              @MappingTarget final
              au.org.ands.vocabs.registry.schema.vocabulary201701.ApCommon
              target) {
        if (target instanceof au.org.ands.vocabs.registry.schema.
                vocabulary201701.ApApiSparql) {
            jsonDataIntoTarget((au.org.ands.vocabs.registry.db.internal.
                    ApApiSparql)
                    data, (au.org.ands.vocabs.registry.schema.
                            vocabulary201701.ApApiSparql)
                    target);
        } else if (target instanceof au.org.ands.vocabs.registry.schema.
                vocabulary201701.ApFile) {
            jsonDataIntoTarget((au.org.ands.vocabs.registry.db.internal.
                    ApFile)
                    data, (au.org.ands.vocabs.registry.schema.
                            vocabulary201701.ApFile)
                    target);
        } else if (target instanceof au.org.ands.vocabs.registry.schema.
                vocabulary201701.ApSesameDownload) {
            jsonDataIntoTarget((au.org.ands.vocabs.registry.db.internal.
                    ApSesameDownload)
                    data, (au.org.ands.vocabs.registry.schema.
                            vocabulary201701.ApSesameDownload)
                    target);
        } else if (target instanceof au.org.ands.vocabs.registry.schema.
                vocabulary201701.ApSissvoc) {
            jsonDataIntoTarget((au.org.ands.vocabs.registry.db.internal.
                    ApSissvoc)
                    data, (au.org.ands.vocabs.registry.schema.
                            vocabulary201701.ApSissvoc)
                    target);
        } else if (target instanceof au.org.ands.vocabs.registry.schema.
                vocabulary201701.ApWebPage) {
            jsonDataIntoTarget((au.org.ands.vocabs.registry.db.internal.
                    ApWebPage)
                    data, (au.org.ands.vocabs.registry.schema.
                            vocabulary201701.ApWebPage)
                    target);
        }
    }



}
