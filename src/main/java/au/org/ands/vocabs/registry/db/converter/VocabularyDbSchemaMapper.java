/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.converter;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import au.org.ands.vocabs.registry.db.internal.VocabularyJson;
import au.org.ands.vocabs.registry.schema.vocabulary201701.Vocabulary;

/** MapStruct mapper from Vocabulary database to schema. */
@Mapper
//@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(VocabularyDbSchemaMapperDecorator.class)
public interface VocabularyDbSchemaMapper {

    /** Singleton instance of this mapper. */
    VocabularyDbSchemaMapper INSTANCE =
            Mappers.getMapper(VocabularyDbSchemaMapper.class);

    /** MapStruct-generated Mapper from Vocabulary database to schema.
     * @param source The Vocabulary entity from the database.
     * @return The schema version of the vocabulary.
     */
    @Mapping(source = "vocabularyId", target = "id")
    @Mapping(target = "acronym", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "licence", ignore = true)
    @Mapping(target = "note", ignore = true)
    @Mapping(target = "otherLanguage", ignore = true)
    @Mapping(target = "poolpartyProject", ignore = true)
    @Mapping(target = "primaryLanguage", ignore = true)
//    @Mapping(target = "relatedEntity", ignore = true)
    @Mapping(target = "revisionCycle", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "topConcept", ignore = true)
//    @Mapping(target = "version", ignore = true)
    au.org.ands.vocabs.registry.schema.vocabulary201701.Vocabulary
      sourceToTarget(au.org.ands.vocabs.registry.db.entity.Vocabulary source);

    /** MapStruct-generated Mapper from VocabularyJson into an existing
     * schema object.
     * @param data The Vocabulary JSON data from the database.
     * @param target The schema version of the vocabulary to be updated.
     */
    @Mapping(source = "otherLanguages", target = "otherLanguage")
    @Mapping(source = "subjects", target = "subject")
    @Mapping(source = "topConcepts", target = "topConcept")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
//    @Mapping(target = "relatedEntity", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "status", ignore = true)
//    @Mapping(target = "version", ignore = true)
    void jsonDataIntoTarget(VocabularyJson data,
              @MappingTarget
              au.org.ands.vocabs.registry.schema.vocabulary201701.Vocabulary
              target);

    /** MapStruct-generated Mapper from database language data
     * to schema.
     * @param source The language data from the database.
     * @return The schema version of the language data.
     */
//     Vocabulary.Language languageSourceToTarget(String source);

    /** MapStruct-generated Mapper from database subject data
     * to schema.
     * @param source The subject data from the database.
     * @return The schema version of the subject data.
     */
    Vocabulary.Subject subjectSourceToTarget(
            au.org.ands.vocabs.registry.db.internal.VocabularyJson.
            Subjects source);

    /** MapStruct-generated Mapper from database PoolParty data
     * to schema.
     * @param source The PoolParty data from the database.
     * @return The schema version of the PoolParty data.
     */
    Vocabulary.PoolpartyProject poolpartyProjectSourceToTarget(
            au.org.ands.vocabs.registry.db.internal.VocabularyJson.
            PoolpartyProject source);

}
