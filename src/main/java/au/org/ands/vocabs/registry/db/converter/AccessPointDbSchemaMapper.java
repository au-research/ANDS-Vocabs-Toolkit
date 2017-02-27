/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.converter;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import au.org.ands.vocabs.registry.db.internal.ApApiSparql;
import au.org.ands.vocabs.registry.db.internal.ApFile;
import au.org.ands.vocabs.registry.db.internal.ApSesameDownload;
import au.org.ands.vocabs.registry.db.internal.ApSissvoc;
import au.org.ands.vocabs.registry.db.internal.ApWebPage;

/** MapStruct mapper from AccessPoint database to schema. */
@Mapper
//@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(AccessPointDbSchemaMapperDecorator.class)
public interface AccessPointDbSchemaMapper {

    /** Singleton instance of this mapper. */
    AccessPointDbSchemaMapper INSTANCE =
            Mappers.getMapper(AccessPointDbSchemaMapper.class);

    /** MapStruct-generated Mapper from Version database to schema.
     * @param source The Version entity from the database.
     * @return The schema version of the version.
     */
    @Mapping(source = "accessPointId", target = "id")
    @Mapping(target = "AP", ignore = true)
    // Ignore here the fields that are extracted from JSON data.
    au.org.ands.vocabs.registry.schema.vocabulary201701.AccessPoint
      sourceToTarget(au.org.ands.vocabs.registry.db.entity.AccessPoint source);

    /** MapStruct-generated Mapper from ApApiSparql into an existing
     * schema object.
     * @param data The AccessPoint JSON data from the database.
     * @param target The schema version of the access point to be updated.
     */
    void jsonDataIntoTarget(ApApiSparql data,
              @MappingTarget
              au.org.ands.vocabs.registry.schema.vocabulary201701.
              ApApiSparql target);

    /** MapStruct-generated Mapper from ApFile into an existing
     * schema object.
     * @param data The AccessPoint JSON data from the database.
     * @param target The schema version of the access point to be updated.
     */
    void jsonDataIntoTarget(ApFile data,
              @MappingTarget
              au.org.ands.vocabs.registry.schema.vocabulary201701.
              ApFile target);

    /** MapStruct-generated Mapper from ApSesameDownload into an existing
     * schema object.
     * @param data The AccessPoint JSON data from the database.
     * @param target The schema version of the access point to be updated.
     */
    void jsonDataIntoTarget(ApSesameDownload data,
              @MappingTarget
              au.org.ands.vocabs.registry.schema.vocabulary201701.
              ApSesameDownload target);

    /** MapStruct-generated Mapper from ApSissvoc into an existing
     * schema object.
     * @param data The AccessPoint JSON data from the database.
     * @param target The schema version of the access point to be updated.
     */
    void jsonDataIntoTarget(ApSissvoc data,
              @MappingTarget
              au.org.ands.vocabs.registry.schema.vocabulary201701.
              ApSissvoc target);

    /** MapStruct-generated Mapper from ApWebPage into an existing
     * schema object.
     * @param data The AccessPoint JSON data from the database.
     * @param target The schema version of the access point to be updated.
     */
    void jsonDataIntoTarget(ApWebPage data,
              @MappingTarget
              au.org.ands.vocabs.registry.schema.vocabulary201701.
              ApWebPage target);


}
