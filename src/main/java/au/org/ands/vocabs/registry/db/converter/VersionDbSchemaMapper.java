/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.converter;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import au.org.ands.vocabs.registry.db.internal.VersionJson;

/** MapStruct mapper from Version database to schema. */
@Mapper
//@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(VersionDbSchemaMapperDecorator.class)
public interface VersionDbSchemaMapper {

    /** Singleton instance of this mapper. */
    VersionDbSchemaMapper INSTANCE =
            Mappers.getMapper(VersionDbSchemaMapper.class);

    /** MapStruct-generated Mapper from Version database to schema.
     * @param source The Version entity from the database.
     * @return The schema version of the version.
     */
    @Mapping(source = "versionId", target = "id")
    // Ignore here the fields that are extracted from JSON data.
    @Mapping(target = "note", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "doImport", ignore = true)
    @Mapping(target = "doPublish", ignore = true)
    au.org.ands.vocabs.registry.schema.vocabulary201701.Version
      sourceToTarget(au.org.ands.vocabs.registry.db.entity.Version source);

    /** MapStruct-generated Mapper from VersionJson into an existing
     * schema object.
     * @param data The Version JSON data from the database.
     * @param target The schema version of the version to be updated.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "releaseDate", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
    void jsonDataIntoTarget(VersionJson data,
              @MappingTarget
              au.org.ands.vocabs.registry.schema.vocabulary201701.Version
              target);

}
