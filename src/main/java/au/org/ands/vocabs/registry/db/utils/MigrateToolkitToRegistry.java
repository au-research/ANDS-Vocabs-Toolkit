// CHECKSTYLE:OFF: FileLength
// This class is too long. But how to make it shorter?
/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.registry.db.utils;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import au.org.ands.vocabs.registry.db.context.TemporalConstants;
import au.org.ands.vocabs.registry.db.context.TemporalUtils;
import au.org.ands.vocabs.registry.db.dao.AccessPointDAO;
import au.org.ands.vocabs.registry.db.dao.RelatedEntityDAO;
import au.org.ands.vocabs.registry.db.dao.RelatedEntityIdentifierDAO;
import au.org.ands.vocabs.registry.db.dao.ResourceMapEntryDAO;
import au.org.ands.vocabs.registry.db.dao.ResourceOwnerHostDAO;
import au.org.ands.vocabs.registry.db.dao.TaskDAO;
import au.org.ands.vocabs.registry.db.dao.VersionArtefactDAO;
import au.org.ands.vocabs.registry.db.dao.VersionDAO;
import au.org.ands.vocabs.registry.db.dao.VocabularyDAO;
import au.org.ands.vocabs.registry.db.dao.VocabularyRelatedEntityDAO;
import au.org.ands.vocabs.registry.db.dao.VocabularyRelatedVocabularyDAO;
import au.org.ands.vocabs.registry.db.entity.RelatedEntity;
import au.org.ands.vocabs.registry.db.entity.RelatedEntityIdentifier;
import au.org.ands.vocabs.registry.db.entity.VersionArtefact;
import au.org.ands.vocabs.registry.db.entity.VocabularyRelatedEntity;
import au.org.ands.vocabs.registry.db.entity.VocabularyRelatedVocabulary;
import au.org.ands.vocabs.registry.db.internal.ApApiSparql;
import au.org.ands.vocabs.registry.db.internal.ApCommon;
import au.org.ands.vocabs.registry.db.internal.ApFile;
import au.org.ands.vocabs.registry.db.internal.ApSesameDownload;
import au.org.ands.vocabs.registry.db.internal.ApSissvoc;
import au.org.ands.vocabs.registry.db.internal.ApWebPage;
import au.org.ands.vocabs.registry.db.internal.RelatedEntityCommon;
import au.org.ands.vocabs.registry.db.internal.RelatedPartyJson;
import au.org.ands.vocabs.registry.db.internal.RelatedServiceJson;
import au.org.ands.vocabs.registry.db.internal.RelatedVocabularyJson;
import au.org.ands.vocabs.registry.db.internal.VaCommon;
import au.org.ands.vocabs.registry.db.internal.VaConceptList;
import au.org.ands.vocabs.registry.db.internal.VaConceptTree;
import au.org.ands.vocabs.registry.db.internal.VersionJson;
import au.org.ands.vocabs.registry.db.internal.VocabularyJson;
import au.org.ands.vocabs.registry.enums.AccessPointType;
import au.org.ands.vocabs.registry.enums.ApSource;
import au.org.ands.vocabs.registry.enums.RelatedEntityIdentifierType;
import au.org.ands.vocabs.registry.enums.RelatedEntityRelation;
import au.org.ands.vocabs.registry.enums.RelatedEntityType;
import au.org.ands.vocabs.registry.enums.RelatedVocabularyRelation;
import au.org.ands.vocabs.registry.enums.TaskStatus;
import au.org.ands.vocabs.registry.enums.VersionArtefactStatus;
import au.org.ands.vocabs.registry.enums.VersionArtefactType;
import au.org.ands.vocabs.registry.enums.VersionStatus;
import au.org.ands.vocabs.registry.enums.VocabularyStatus;
import au.org.ands.vocabs.toolkit.db.AccessPointUtils;
import au.org.ands.vocabs.toolkit.db.DBContext;
import au.org.ands.vocabs.toolkit.db.TaskUtils;
import au.org.ands.vocabs.toolkit.db.model.AccessPoint;
import au.org.ands.vocabs.toolkit.db.model.ResourceMapEntry;
import au.org.ands.vocabs.toolkit.db.model.ResourceOwnerHost;
import au.org.ands.vocabs.toolkit.db.model.Task;
import au.org.ands.vocabs.toolkit.db.model.Version;
import au.org.ands.vocabs.toolkit.db.model.Vocabulary;
import au.org.ands.vocabs.toolkit.utils.ToolkitFileUtils;

/** Utility class for migrating the contents of the "toolkit" database
 * to the "registry" database.
 */
@Path("registry/internal")
public final class MigrateToolkitToRegistry {

    /** Name of system user to use as modifiedBy value. */
    private static final String SYSTEM_USER = "SYSTEM";

    /** Logger for this class. */
    private Logger logger = LoggerFactory.getLogger(
            MethodHandles.lookup().lookupClass());

    /** Map of related entity identifier prefixes,
     * to the corresponding identifier type.
     * The contents are initialized in a static initialization block.
     */
    private static HashMap<String, RelatedEntityIdentifierType>
        reiPrefixes = new HashMap<>();

    static {
        reiPrefixes.put("http://nla.gov.au/nla.party-",
                RelatedEntityIdentifierType.AU_ANL_PEAU);
        reiPrefixes.put("http://dx.doi.org/",
                RelatedEntityIdentifierType.DOI);
        reiPrefixes.put("http://hdl.handle.net/",
                RelatedEntityIdentifierType.HANDLE);
        reiPrefixes.put("info:",
                RelatedEntityIdentifierType.INFOURI);
        reiPrefixes.put("http://isni.org/isni/",
                RelatedEntityIdentifierType.ISNI);
        reiPrefixes.put("http://orcid.org/",
                RelatedEntityIdentifierType.ORCID);
        reiPrefixes.put("http://purl.org/",
                RelatedEntityIdentifierType.PURL);
        reiPrefixes.put("http://www.researcherid.com/rid/",
                RelatedEntityIdentifierType.RESEARCHER_ID);
        reiPrefixes.put("http://viaf.org/viaf/",
                RelatedEntityIdentifierType.VIAF);
    }

    /** Map of related entity identifier types, to prefixes that should
     * be removed from the beginning of identifiers of that type.
     * Note that the prefix to remove may or may correspond with the
     * prefix used for identification in {@link #reiPrefixes}.
     * In fact, we don't really need the prefix to remove,
     * just its <i>length</i>. But with the full prefix value, its
     * easier to see that the value is correct.
     * The contents are initialized in a static initialization block.
     */
    private static HashMap<RelatedEntityIdentifierType, String>
        reiPrefixesToRemove = new HashMap<>();

    static {
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.AU_ANL_PEAU,
                "http://nla.gov.au/");
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.DOI,
                "http://dx.doi.org/");
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.HANDLE,
                "http://hdl.handle.net/");
        // info: not removed.
//        reiPrefixesToRemove.put(RelatedEntityIdentifierType.INFOURI,
//                "info:");
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.ISNI,
                "http://isni.org/isni/");
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.ORCID,
                "http://orcid.org/");
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.PURL,
                "http://purl.org/");
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.RESEARCHER_ID,
                "http://www.researcherid.com/rid/");
        reiPrefixesToRemove.put(RelatedEntityIdentifierType.VIAF,
                "http://viaf.org/viaf/");
    }

    /** A map of vocabularies that we have migrated.
     * The keys of the HashMap are the ids of the toolkit instance
     * of the vocabulary. The values are the ids of the migrated
     * registry vocabularies.
     */
    private HashMap<Integer, Integer> migratedVocabularies = new HashMap<>();

    /** A map of versions that we have migrated.
     * The keys of the HashMap are the ids of the toolkit instance
     * of the version. The values are the ids of the migrated
     * registry versions.
     */
    private HashMap<Integer, Integer> migratedVersions = new HashMap<>();

    /** A map of access points that we have migrated.
     * The keys of the HashMap are the ids of the toolkit instance
     * of the access point. The values are the ids of the migrated
     * access points.
     */
    private HashMap<Integer, Integer> migratedAccessPoints = new HashMap<>();

    /** A map of related entities we have already migrated, to the
     * related_entity_id of that entity.
     * The keys of the HashMap are the JSON data of the related entity.
     * To make this work, the related entity should have been parsed and
     * then reserialized in a canonical way, i.e., with keys in
     * alphabetical order.
     * The related entity is not exactly what comes out of the database;
     * it has another key/value pair added: key="owner", and value the
     * owner of the vocabulary.
     * This means that different owners get distinct related entities.
     */
    private HashMap<String, Integer> relatedEntitiesSeen = new HashMap<>();

    /** Internally-related vocabularies can only be matched up once
     * we have migrated all of the vocabularies. So, they are stored
     * here along the way, and the relationships are created in
     * the registry database after all vocabularies have been migrated.
     */
    private HashMap<au.org.ands.vocabs.registry.db.entity.Vocabulary,
        JsonNode> relatedVocabularies = new HashMap<>();

    /** Convert a {@link Date} extracted from the "toolkit" database
     * to a {@link LocalDateTime} in UTC. Note that JDBC has already
     * interpreted the database content according to the local time zone,
     * so the {@link Date} value is already in UTC.
     * @param date The {@link Date} to be converted.
     * @return The date/time value, as a {@link LocalDateTime}, in UTC.
     */
    private LocalDateTime dateToLocalDateTime(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(),
                ZoneOffset.UTC);
    }

    /** Get the published or deprecated verion of a migrated Vocabulary
     * by its slug, if there is one. Otherwise, return null. This method can be
     * used after all the published/deprecated Vocabulary instances
     * have been migrated.
     * @param slug Vocabulary slug.
     * @return The published or deprecated Vocabulary in the "registry"
     *      database, or null, if there isn't one.
     */
    public au.org.ands.vocabs.registry.db.entity.Vocabulary
        getPublishedOrDeprecatedVocabularyBySlug(final String slug) {
        EntityManager em =
                au.org.ands.vocabs.registry.db.context.DBContext.
                getEntityManager();
        // There may be two instances of the record; only one
        // will be current. Select the current one.
        TypedQuery<au.org.ands.vocabs.registry.db.entity.Vocabulary> q =
                em.createQuery(
                "SELECT v FROM Vocabulary v"
                + " WHERE (status="
                + "au.org.ands.vocabs.registry.enums.VocabularyStatus."
                + "PUBLISHED OR status="
                + "au.org.ands.vocabs.registry.enums.VocabularyStatus."
                + "DEPRECATED) AND slug=:slug"
                + TemporalUtils.AND_TEMPORAL_QUERY_VALID_SUFFIX,
                au.org.ands.vocabs.registry.db.entity.Vocabulary.class);
        q.setParameter("slug", slug);
        q = TemporalUtils.setDatetimeConstantParameters(q);
        List<au.org.ands.vocabs.registry.db.entity.Vocabulary>
            vocabularyList = q.getResultList();
        if (vocabularyList.size() > 1) {
            logger.error("There is more than one published or deprecated "
                    + "vocabulary with the slug: " + slug);
        }
        em.close();
        if (vocabularyList.size() == 0) {
            // No published or deprecated instance of this vocabulary.
            return null;
        }
        logger.info("Got vocab with id " + vocabularyList.get(0).getId()
                + " and startDate " + vocabularyList.get(0).getStartDate());
        return vocabularyList.get(0);
    }

    /** Get the draft verion of a migrated Vocabulary by its slug,
     * if there is one. Otherwise, return null. This method can be
     * used after all the draft Vocabulary instances
     * have been migrated.
     * @param slug Vocabulary slug.
     * @return The draft Vocabulary in the "registry" database,
     *      or null, if there isn't one.
     */
    public au.org.ands.vocabs.registry.db.entity.Vocabulary
        getDraftVocabularyBySlug(final String slug) {
        EntityManager em =
                au.org.ands.vocabs.registry.db.context.DBContext.
                getEntityManager();
        TypedQuery<au.org.ands.vocabs.registry.db.entity.Vocabulary> q =
                em.createQuery(
                "SELECT v FROM Vocabulary v "
                + "WHERE v.status=au.org.ands.vocabs.registry.enums."
                + "VocabularyStatus.DRAFT "
                + "AND v.slug=:slug",
                au.org.ands.vocabs.registry.db.entity.Vocabulary.class);
        q.setParameter("slug", slug);
        List<au.org.ands.vocabs.registry.db.entity.Vocabulary>
            vocabularyList = q.getResultList();
        if (vocabularyList.size() > 1) {
            logger.error("There is more than one draft vocabulary "
                    + "with the slug: " + slug);
        }
        em.close();
        if (vocabularyList.size() == 0) {
            // No draft instance of this vocabulary.
            return null;
        }
        return vocabularyList.get(0);
    }

    /** Migrate the contents of the "toolkit" database to
     * the "registry" database.
     * @return Text indicating success.
     */
    @Path("migrateToolkitToRegistry")
    @GET
    public String migrateToolkitToRegistry() {
        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        EntityManager toolkitEm = DBContext.getEntityManager();

        migratePublishedAndDeprecatedVocabularies(formatter, toolkitEm);
        migrateDraftVocabularies(formatter, toolkitEm);
        migrateInternallyRelatedVocabularies();
        migrateTasks(toolkitEm);
        migrateResourceOwnerHosts(toolkitEm);
        migrateResourceMap(toolkitEm);
        toolkitEm.close();
        return "Done.";
    }

    /** Migrate the vocabularies with published or deprecated status.
     * @param formatter The date formatter to use.
     * @param toolkitEm The Toolkit EntityManager to use.
     */
    private void migratePublishedAndDeprecatedVocabularies(
            final SimpleDateFormat formatter, final EntityManager toolkitEm) {
        // First, get all deprecated and published vocabularies.
        List<Vocabulary> vocabularies =
                toolkitEm.createQuery("SELECT v FROM Vocabulary v "
                        + "WHERE v.status='published' "
                        + "OR v.status='deprecated'",
                        Vocabulary.class).
                getResultList();
        logger.info("Got " + vocabularies.size()
            + " published and deprecated vocabularies.");
        for (Vocabulary vocabulary : vocabularies) {
            logger.info("Processing vocabulary with id: "
                    + vocabulary.getId());
            au.org.ands.vocabs.registry.db.entity.Vocabulary
            registryVocabulary =
                new au.org.ands.vocabs.registry.db.entity.Vocabulary();
            // Now copy across all fields.
            copyVocabularyFields(formatter, vocabulary, registryVocabulary,
                    false);
            // Now see if the vocabulary's modification date is different
            // from its creation date.
            // If not, we create one vocabulary record, with startDate
            // equal to the creation date.
            // If it is different, we create two records. The first record
            // will have startDate equal to the creation date, and
            // the endDate equal to the modification date.
            // The second record will have startDate equal to the
            // modification date, and will have and endDate showing
            // that it is still valid.
            registryVocabulary.setStartDate(dateToLocalDateTime(
                    vocabulary.getCreatedDate()));
            logger.info("Converted old created date "
                    + vocabulary.getCreatedDate()
                    + " to start date "
                    + registryVocabulary.getStartDate());
            LocalDateTime modifiedDate = dateToLocalDateTime(
                    vocabulary.getModifiedDate());
            if (modifiedDate.equals(registryVocabulary.getStartDate())) {
                // Create just one record using the combined
                // created/modified date.
                registryVocabulary.setEndDate(
                        TemporalConstants.CURRENTLY_VALID_END_DATE);
                 VocabularyDAO.saveVocabularyWithId(registryVocabulary);
            } else {
                // Create two records using the combined
                // created/modified date.
                registryVocabulary.setEndDate(modifiedDate);
                VocabularyDAO.saveVocabularyWithId(registryVocabulary);
                // registryVocabulary now has the correctly populated
                // vocabularyId. So reuse it. But we have to reset the
                // id first, so that it is not considered to be a
                // detached entity.
                registryVocabulary.setId(null);
                registryVocabulary.setStartDate(modifiedDate);
                registryVocabulary
                        .setEndDate(TemporalConstants.CURRENTLY_VALID_END_DATE);
                // Use saveVocabulary() this time so as not to create a new
                // vocabulary, but only to add a new entry for the same one.
                VocabularyDAO.saveVocabulary(registryVocabulary);
            }
            migratedVocabularies.put(vocabulary.getId(),
                    registryVocabulary.getVocabularyId());
            // Now migrate related entities.
            migrateRelatedEntities(vocabulary, registryVocabulary);
            // Now migrate the versions of the toolkit record.
            migrateVersions(toolkitEm, vocabulary, registryVocabulary, false);
        }
    }

    /** Migrate the vocabularies with draft status.
     * @param formatter The date formatter to use.
     * @param toolkitEm The Toolkit EntityManager to use.
     */
    private void migrateDraftVocabularies(final SimpleDateFormat formatter,
            final EntityManager toolkitEm) {
        List<Vocabulary> vocabularies;
        // Now, get all draft vocabularies. We match draft to published using
        // slugs.
        vocabularies =
                toolkitEm.createQuery("SELECT v FROM Vocabulary v "
                        + "WHERE v.status='draft'",
                        Vocabulary.class).
                getResultList();
        logger.info("Got " + vocabularies.size() + " draft vocabularies.");
        for (Vocabulary vocabulary : vocabularies) {
            logger.info("Processing draft vocabulary with id: "
                    + vocabulary.getId());
            au.org.ands.vocabs.registry.db.entity.Vocabulary
                publishedVocabulary =
                getPublishedOrDeprecatedVocabularyBySlug(vocabulary.getSlug());
            au.org.ands.vocabs.registry.db.entity.Vocabulary
            registryVocabulary =
                new au.org.ands.vocabs.registry.db.entity.Vocabulary();
            // Now copy across all fields.
            copyVocabularyFields(formatter, vocabulary, registryVocabulary,
                    true);
            // NB: we discard the createdDate/modifiedDate
            // elements of draft records. The registryVocabulary's
            // startDate/endDate values are assigned the special values
            // to indicate a draft record.
            registryVocabulary.setStartDate(TemporalConstants.DRAFT_START_DATE);
            registryVocabulary.setEndDate(
                    TemporalConstants.DRAFT_ADDITION_MODIFICATION_END_DATE);
            if (publishedVocabulary == null) {
                // There is no published instance of this vocabulary, so
                // we need to create a new vocabulary ID.
                VocabularyDAO.saveVocabularyWithId(registryVocabulary);

            } else {
                // This exists in published form, so use the same
                // vocabulary ID.
                registryVocabulary.setVocabularyId(
                        publishedVocabulary.getVocabularyId());
                VocabularyDAO.saveVocabulary(registryVocabulary);
            }
            migratedVocabularies.put(vocabulary.getId(),
                    registryVocabulary.getVocabularyId());
            // Now migrate related entities.
            migrateRelatedEntities(vocabulary, registryVocabulary);
            // Now migrate the versions of the toolkit record.
            migrateVersions(toolkitEm, vocabulary, registryVocabulary, true);
        }
    }

    /** Migrate the versions.
     * @param toolkitEm The Toolkit EntityManager to use.
     * @param toolkitVocabulary The "toolkit" vocabulary record that "owns" the
     *      versions being migrated.
     * @param registryVocabulary The vocabulary record
     *      as it has been migrated to the registry database.
     * @param isDraft Whether this is representing a draft record.
     */
    private void migrateVersions(final EntityManager toolkitEm,
            final Vocabulary toolkitVocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final boolean isDraft) {
        List<Version> versions;
        versions =
                toolkitEm.createQuery("SELECT v FROM Version v "
                        + "WHERE v.vocabId = :vocabId",
                        Version.class).
                setParameter("vocabId", toolkitVocabulary.getId()).
                getResultList();
        logger.info("Got " + versions.size() + " version(s).");
        for (Version version : versions) {
            logger.info("Processing version with id: "
                    + version.getId());
            au.org.ands.vocabs.registry.db.entity.Version
            registryVersion =
                new au.org.ands.vocabs.registry.db.entity.Version();
            // Now copy across all other fields.
            copyVersionFields(toolkitEm, toolkitVocabulary,
                    registryVocabulary,
                    version, registryVersion, isDraft);
            // Use the migrated vocabulary record's start and end dates.
            // This is correct for all vocabulary status values.
            registryVersion.setStartDate(registryVocabulary.getStartDate());
            registryVersion.setEndDate(registryVocabulary.getEndDate());
            VersionDAO.saveVersionWithId(registryVersion);
            migratedVersions.put(version.getId(),
                    registryVersion.getVersionId());
            // Now that we have a complete registryVersion entity
            // (i.e., with id and modifiedBy values),
            // we can use it to create related database entities.
            processVersionRelatedDbEntities(registryVocabulary,
                    version, registryVersion);
            migrateAccessPoints(toolkitEm, toolkitVocabulary,
                    version, registryVersion, isDraft);
        }
    }

    /** Migrate the access points.
     * @param toolkitEm The Toolkit EntityManager to use.
     * @param toolkitVocabulary The "toolkit" vocabulary record that "owns" the
     *      versions being migrated.
     * @param toolkitVersion The "toolkit" version record that "owns" the
     *      access points being migrated.
     * @param registryVersion The version record
     *      as it has been migrated to the registry database.
     * @param isDraft Whether this is representing a draft record.
     */
    private void migrateAccessPoints(final EntityManager toolkitEm,
            final Vocabulary toolkitVocabulary,
            final Version toolkitVersion,
            final au.org.ands.vocabs.registry.db.entity.Version registryVersion,
            final boolean isDraft) {
        List<AccessPoint> aps;
        aps =
                toolkitEm.createQuery("SELECT ap FROM AccessPoint ap "
                        + "WHERE ap.versionId = :versionId",
                        AccessPoint.class).
                setParameter("versionId", toolkitVersion.getId()).
                getResultList();
        logger.info("Got " + aps.size() + " access point(s).");
        for (AccessPoint ap : aps) {
            logger.info("Processing access point with id: "
                    + ap.getId());

            au.org.ands.vocabs.registry.db.entity.AccessPoint
            registryAccessPoint =
                new au.org.ands.vocabs.registry.db.entity.AccessPoint();
            // Now copy across all fields.
            copyAccessPointFields(toolkitVocabulary, registryVersion,
                ap, registryAccessPoint, isDraft);
            // Use the migrated version record's start and end dates.
            // This is correct for all version status values.
            registryAccessPoint.setStartDate(registryVersion.getStartDate());
            registryAccessPoint.setEndDate(registryVersion.getEndDate());
            AccessPointDAO.saveAccessPointWithId(registryAccessPoint);
            migratedAccessPoints.put(ap.getId(),
                    registryAccessPoint.getAccessPointId());
        }
    }

    /** Copy the fields from a "toolkit" vocabulary instance
     * to a new "registry" vocabulary instance.
     * @param formatter The formatter to use for date/times.
     * @param vocabulary The "toolkit" vocabulary instance being migrated.
     * @param registryVocabulary The new "registry" vocabulary instance.
     * @param isDraft Whether this is representing a draft record.
     */
    @SuppressWarnings("checkstyle:MethodLength")
    private void copyVocabularyFields(final SimpleDateFormat formatter,
            final Vocabulary vocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final boolean isDraft) {
        String originalData = vocabulary.getData();
        if (originalData == null || originalData.isEmpty()) {
            originalData = "{}";
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(originalData);
        Iterator<Entry<String, JsonNode>> dataJsonIterator =
                dataJson.fields();

        //  The JSON data to be stored into the data field of the new
        // registry record.
        VocabularyJson vocabularyJson = new VocabularyJson();

        // Flags to help make sure we have all required fields.
        boolean hasLanguages = false;
        boolean hasRelatedEntities = false;
        boolean hasSubjects = false;

        while (dataJsonIterator.hasNext()) {
            Entry<String, JsonNode> entry = dataJsonIterator.next();
            logger.info("Processing vocabulary data[" + entry.getKey()
            + "]");
            switch (entry.getKey()) {
            // Fields we do a consistency check on, but don't copy here.
            case "created_date":
                if (isDraft) {
                    logger.info("Ignoring data[created_date] of a draft");
                } else {
                    if (!entry.getValue().asText().
                            equals(formatter.format(
                                    vocabulary.getCreatedDate()))) {
                        logger.error("data[created_date] doesn't match");
                    }
                }
                break;
            case "description":
                if (!entry.getValue().asText().
                        equals(vocabulary.getDescription())) {
                    logger.error("data[description] doesn't match");
                }
                break;
            case "from_vocab_id":
                if (isDraft) {
                    logger.info("Ignoring data[from_vocab_id] of a draft");
                } else {
                    if (entry.getValue().asInt()
                            != vocabulary.getId()) {
                        logger.error("data[from_vocab_id] in a non-draft "
                                + "doesn't match the vocabulary's own id");
                    }
                }
                break;
            case "id":
                if (entry.getValue().asInt() != vocabulary.getId()) {
                    logger.error("data[id] doesn't match");
                }
                break;
            case "licence":
                if (!entry.getValue().asText().
                        equals(vocabulary.getLicence())) {
                    logger.error("data[licence] doesn't match");
                }
                break;
            case "modified_date":
                // Any such value is the modified date of the
                // _next-to-last_ modification! Just use
                // vocabulary.getModifiedDate().
                logger.info("Ignoring data[modified_date]");
                break;
            case "modified_who":
                if ((entry.getValue().isNull()
                        && vocabulary.getModifiedWho() != null)
                        ||
                        (!entry.getValue().isNull()
                                && !entry.getValue().asText().
                                equals(vocabulary.getModifiedWho()))) {
                    logger.error("data[modified_who] doesn't match");
                }
                break;
            case "owner":
                if (!entry.getValue().asText().
                        equals(vocabulary.getOwner())) {
                    logger.error("data[owner] doesn't match");
                }
                break;
            case "pool_party_id":
                if (!entry.getValue().asText().
                        equals(vocabulary.getPoolPartyId())) {
                    logger.error("data[pool_party_id] doesn't match");
                }
                break;
            case "related_entity":
                // Related entities can only be migrated after the
                // vocabulary entity has been persisted, and has an ID.
                // Migration is done in migrateRelatedEntities().
                if (!entry.getValue().isArray()) {
                    logger.error("data[related_entity] is not an array");
                } else if (entry.getValue().size() == 0) {
                    logger.error("data[related_entity] is an empty array");
                } else {
                    hasRelatedEntities = true;
                }
                break;
            case "slug":
                if (!entry.getValue().asText().
                        equals(vocabulary.getSlug())) {
                    logger.error("data[slug] doesn't match");
                }
                break;
            case "status":
                if (!entry.getValue().asText().
                        equals(vocabulary.getStatus())) {
                    logger.error("data[status] doesn't match");
                }
                break;
            case "title":
                if (!entry.getValue().asText().
                        equals(vocabulary.getTitle())) {
                    logger.error("data[title] doesn't match");
                }
                break;
            case "user_owner":
                if (!entry.getValue().asText().
                        equals(vocabulary.getUserOwner())) {
                    logger.error("data[user_owner] doesn't match");
                }
                break;

            // Text Fields we copy here.
            case "acronym":
                vocabularyJson.setAcronym(entry.getValue().asText());
                break;
            case "creation_date":
                vocabularyJson.setCreationDate(entry.getValue().asText());
                break;
            case "note":
                vocabularyJson.setNote(entry.getValue().asText());
                break;
            case "revision_cycle":
                vocabularyJson.setRevisionCycle(entry.getValue().asText());
                break;

            case "language":
                if (!entry.getValue().isArray()) {
                    logger.error("data[language] is not an array");
                } else if (entry.getValue().size() == 0) {
                    logger.error("data[language] is an empty array");
                } else {
                    hasLanguages = true;
                    // Don't assume English will be the primary language;
                    // start with the first language listed.
                    List<String> otherLanguages =
                            vocabularyJson.getOtherLanguages();
                    String primaryLanguage = entry.getValue().get(0).asText();
                    for (JsonNode languageElement : entry.getValue()) {
                        String language = languageElement.asText();
                        if ("English".equals(language)) {
                            // Special case to fix "English". See
                            // ANDS-Registry-Core commit
                            // f31017519229ab1664b6f43f56efb4315374b02f
                            // for the commit that fixed this defect in
                            // vocabs_cms.js.
                            language = "en";
                        }
                        otherLanguages.add(language);
                        if ("en".equals(language)) {
                            // Give preference to English.
                            primaryLanguage = "en";
                        }
                    }
                    vocabularyJson.setPrimaryLanguage(primaryLanguage);
                    otherLanguages.remove(
                            otherLanguages.indexOf(primaryLanguage));
                }
                break;
            case "subjects":
                if (!entry.getValue().isArray()) {
                    logger.error("data[subjects] is not an array");
                } else if (entry.getValue().size() == 0) {
                        logger.error("data[subjects] is an empty array");
                } else {
                    hasSubjects = true;
                    List<VocabularyJson.Subjects> subjectsList =
                            vocabularyJson.getSubjects();
                    for (JsonNode tc : entry.getValue()) {
                        VocabularyJson.Subjects subject =
                                new VocabularyJson.Subjects();
                        subject.setSource(tc.get("subject_source").asText());
                        subject.setLabel(tc.get("subject_label").asText());
                        if (tc.has("subject_notation")) {
                            subject.setNotation(tc.get("subject_notation").
                                    asText());
                        }
                        if (tc.has("subject_iri")) {
                            subject.setIri(tc.get("subject_iri").asText());
                        }
                        subjectsList.add(subject);
                    }
                }
                break;
            case "top_concept":
                if (!entry.getValue().isArray()) {
                    logger.error("data[top_concept] is not an array");
                } else {
                    // Only add to the JSON, if there are any top concepts
                    // defined.
                    for (JsonNode tc : entry.getValue()) {
                        vocabularyJson.getTopConcepts().add(tc.asText());
                    }
                }
                break;
            case "versions":
                // When the current portal loads vocabulary data from
                // database, it overwrites any of these values by
                // reading directly from the versions table.
                logger.info("Ignoring data[versions]");
                break;

            // Ancient fields we don't expect to see.
            case "logo":
                logger.info("Ignoring data[logo]");
                break;
            case "release_date":
                logger.info("Ignoring data[release_date]");
                break;
            case "vocab_uri":
                logger.info("Ignoring data[vocab_uri]");
                break;

            default:
                logger.error("copyVocabularyFields: "
                        + "Unrecognized data element data["
                        + entry.getKey() + "]");
                break;
            }
        }

        // Check we got what we needed.
        if (!hasLanguages) {
            logger.error("Can't proceed with this vocabulary, as "
                    + "there is no language data.");
        }
        if (!hasRelatedEntities) {
            logger.error("Can't proceed with this vocabulary, as "
                    + "there are no related entities.");
        }
        if (!hasSubjects) {
            logger.error("Can't proceed with this vocabulary, as "
                    + "there is no subject data.");
        }

        // Migrate fields that are "top-level" in the "toolkit"
        // instance that go into the "data" field of the "registry" instance.

        vocabularyJson.setTitle(vocabulary.getTitle());

        if (vocabulary.getDescription() == null) {
            logger.error("Can't proceed with this vocabulary, as "
                    + "it has a null description.");
        }
        vocabularyJson.setDescription(vocabulary.getDescription());
        if (!vocabulary.getLicence().isEmpty()) {
            // Only add a license to the JSON, if there is one specified.
            vocabularyJson.setLicence(vocabulary.getLicence());
        }
        if (vocabulary.getModifiedDate() == null) {
            logger.error("Can't proceed with this vocabulary, as "
                    + "it has a null modified date.");
        }
        // No, don't copy modified_date into data. It is used directly
        // as a startDate/endDate value.
        //        jobNewData.add("modified_date",
        //                vocabulary.getModifiedDate().toString());

        if (vocabulary.getModifiedWho() != null
                && !vocabulary.getModifiedWho().isEmpty()) {
            logger.info("Copying modifiedWho value to modifiedBy");
            registryVocabulary.setModifiedBy(vocabulary.getModifiedWho());
        } else {
            logger.info("Setting modifiedBy value to SYSTEM");
            registryVocabulary.setModifiedBy(SYSTEM_USER);
        }

        String poolPartyId = vocabulary.getPoolPartyId();
        if (poolPartyId != null && !poolPartyId.isEmpty()) {
            logger.info("Inserting PoolParty project details");
            // We require the ANDS PoolParty server to be inserted
            // into the poolparty_servers table with id = 1.
            VocabularyJson.PoolpartyProject vocabularyJsonPoolPartyProject =
                    new VocabularyJson.PoolpartyProject();
            vocabularyJsonPoolPartyProject.setServerId(1);
            vocabularyJsonPoolPartyProject.setProjectId(poolPartyId);
            vocabularyJson.setPoolpartyProject(vocabularyJsonPoolPartyProject);
        }

        // For draft records, we can't use the startDate/endDate values to
        // represent record creation/modification times. So we store
        // them in the data field.
        if (isDraft) {
            logger.info("Storing creation_date/modification_date values "
                    + "for draft record");
            vocabularyJson.setDraftCreatedDate(dateToLocalDateTime(
                    vocabulary.getCreatedDate()).toString());
            vocabularyJson.setDraftModifiedDate(dateToLocalDateTime(
                    vocabulary.getModifiedDate()).toString());
        }

        // Now set the other top-level values.
        registryVocabulary.setOwner(vocabulary.getOwner());
        registryVocabulary.setSlug(vocabulary.getSlug());
        registryVocabulary.setStatus(VocabularyStatus.fromValue(
                vocabulary.getStatus()));
        registryVocabulary.setData(serializeJsonAsString(vocabularyJson));
    }

    /** Migrate the related entities from a "toolkit" vocabulary instance
     * to new "registry" related entity and related vocabulary instances.
     * @param vocabulary The "toolkit" vocabulary instance being migrated.
     * @param registryVocabulary The new "registry" vocabulary instance.
     */
    private void migrateRelatedEntities(final Vocabulary vocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary) {
        String originalData = vocabulary.getData();
        if (originalData == null || originalData.isEmpty()) {
            logger.error("No data; skipping related entity migration step");
            return;
        }
        JsonNode relatedEntities = TaskUtils.jsonStringToTree(originalData).
                get("related_entity");
        if (relatedEntities == null) {
            logger.error("No related entity data; skipping related entity "
                    + "migration step");
            return;
        }

        for (JsonNode relatedEntityJson : relatedEntities) {
            if (!relatedEntityJson.has("type")) {
                logger.error("related entity has no type");
                continue;
            }
            switch (relatedEntityJson.get("type").asText()) {
            case "party":
                migrateRelatedParty(vocabulary, registryVocabulary,
                        relatedEntityJson);
                break;
            case "service":
                migrateRelatedService(vocabulary, registryVocabulary,
                        relatedEntityJson);
                break;
            case "vocabulary":
                migrateRelatedVocabulary(vocabulary, registryVocabulary,
                        relatedEntityJson);
                break;
            default:
                logger.error("Unknown type in related entity");
            }
        }
    }

    /** Migrate one related entity that is a party.
     * @param vocabulary The "toolkit" vocabulary instance being migrated.
     * @param registryVocabulary The new "registry" vocabulary instance.
     * @param relatedEntityJson The JSON data for the related entity
     *      that is being migrated.
     */
    private void migrateRelatedParty(final Vocabulary vocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final JsonNode relatedEntityJson) {
        // relatedEntity will be assigned either an existing
        // entity from the registry database, or a new instance will
        // be created.
        RelatedEntity relatedEntity;
        HashSet<RelatedEntityRelation> relations = new HashSet<>();

        String canonicalRelatedEntity = canonicalizeRelatedEntityJson(
                relatedEntityJson, vocabulary);
        logger.info("canonicalRelatedEntity = " + canonicalRelatedEntity);
        if (relatedEntitiesSeen.containsKey(canonicalRelatedEntity)) {
            logger.info("Found an existing party related entity that will do.");
            relatedEntity = RelatedEntityDAO.getRelatedEntityById(
                    relatedEntitiesSeen.get(canonicalRelatedEntity));
            // Update startDate.
            // We rely on the fact that we migrate published and
            // deprecated vocabularies _before_ we see any drafts.
            // So a draft can reuse an existing currently-valid related
            // entity, but we will never have the case of a published or
            // deprecated vocbulary that has a draft related entity.
            if (registryVocabulary.getStartDate().isBefore(
                    relatedEntity.getStartDate())) {
                relatedEntity.setStartDate(registryVocabulary.getStartDate());
                RelatedEntityDAO.updateRelatedEntity(relatedEntity);
            }
            // No need to touch endDate in migration. There are no
            // endDate values other than those defined in TemporalConstants,
            // and as noted above, because of the order of migration,
            // we never make a published/deprecated related entity into
            // a draft, or vice versa.
            extractRelations(relatedEntityJson.get("relationship"),
                    relations);
        } else {
            // We need a new entity.
            relatedEntity = new RelatedEntity();

            Iterator<Entry<String, JsonNode>> relatedEntityJsonIterator =
                    relatedEntityJson.fields();

            RelatedPartyJson relatedPartyJson = new RelatedPartyJson();
            while (relatedEntityJsonIterator.hasNext()) {
                Entry<String, JsonNode> entry =
                        relatedEntityJsonIterator.next();
                logger.info("Processing related entity[" + entry.getKey()
                + "]");
                switch (entry.getKey()) {
                case "email":
                    relatedPartyJson.setEmail(entry.getValue().asText());
                    break;
                case "identifiers":
                    // Just check that it is an array, for now.
                    if (!entry.getValue().isArray()) {
                        logger.error("related entity[identifiers] is not "
                                + "an array");
                    }
                    break;
                case "phone":
                    relatedPartyJson.setPhone(entry.getValue().asText());
                    break;
                case "relationship":
                    extractRelations(entry.getValue(), relations);
                    break;
                case "title":
                    relatedEntity.setTitle(entry.getValue().asText());
                case "type":
                    // We already know what type it is
                    break;
                case "urls":
                    migrateRelatedEntityURLs(entry.getValue(),
                            relatedPartyJson);
                    break;
                default:
                    logger.error("migrateRelatedParty: Unrecognized element ["
                            + entry.getKey() + "]");
                    break;
                }
            }

            relatedEntity.setStartDate(registryVocabulary.getStartDate());
            relatedEntity.setEndDate(registryVocabulary.getEndDate());
            // Set modifiedBy now, so it can be passed as a parameter
            // to makeRelatedEntityIdentifier().
            if (vocabulary.getModifiedWho() != null
                    && !vocabulary.getModifiedWho().isEmpty()) {
                logger.info("Copying vocabulary's modifiedWho value "
                        + "to modifiedBy");
                relatedEntity.setModifiedBy(vocabulary.getModifiedWho());
            } else {
                logger.info("Setting modifiedBy value to SYSTEM");
                relatedEntity.setModifiedBy(SYSTEM_USER);
            }
            // The vocabulary owner also owns this related entity.
            relatedEntity.setOwner(vocabulary.getOwner());
            relatedEntity.setType(RelatedEntityType.PARTY);
            relatedEntity.setData(serializeJsonAsString(relatedPartyJson));

            RelatedEntityDAO.saveRelatedEntityWithId(relatedEntity);
            if (!relatedEntitiesSeen.containsKey(canonicalRelatedEntity)) {
                relatedEntitiesSeen.put(canonicalRelatedEntity,
                        relatedEntity.getRelatedEntityId());
            }

            // We can only process the identifiers after the related entity
            // has been persisted, as each identifier has a foreign
            // key back to the related entity.
            migrateRelatedEntityIdentifiers(relatedEntity,
                    relatedEntityJson.get("identifiers"));
        }
        migrateRelatedEntityRelations(registryVocabulary, relatedEntity,
                relations);
    }

    /** Canonicalize a related entity. This involves removing any
     * relationship data, and adding an owner.
     * @param relatedEntityJson The JSON representation of the related entity,
     *      from the "toolkit" database.
     * @param vocabulary The "toolkit" vocabulary object that "owns" this
     *      related entity.
     * @return The canonical representation of this related entity.
     */
    private String canonicalizeRelatedEntityJson(
            final JsonNode relatedEntityJson, final Vocabulary vocabulary) {
        JsonNode canonicalRelatedEntityJson = relatedEntityJson.deepCopy();
        ((ObjectNode) canonicalRelatedEntityJson).remove("relationship");
        ((ObjectNode) canonicalRelatedEntityJson).put("owner",
                vocabulary.getOwner());
        String canonicalRelatedEntity =
                serializeJsonAsString(canonicalRelatedEntityJson);
        return canonicalRelatedEntity;
    }

    /** Extract the identifiers from the related entity, and create
     * a RelatedEntityIdentifier for each one.
     * @param relatedEntity The related entity.
     * @param identifiersJson The JSON representing the identifiers.
     */
    private void migrateRelatedEntityIdentifiers(
            final RelatedEntity relatedEntity,
            final JsonNode identifiersJson) {
        if (identifiersJson != null) {
            for (JsonNode identifierJson : identifiersJson) {
                makeRelatedEntityIdentifier(relatedEntity,
                        identifierJson,
                        relatedEntity.getModifiedBy());
            }
        }
    }

    /** Create instances of VocabularyRelatedEntity for each relation
     * by which the vocabulary is related to the related entity.
     * @param registryVocabulary The migrated "registry" vocabulary.
     * @param relatedEntity The related entity.
     * @param relations The set of relations.
     */
    private void migrateRelatedEntityRelations(
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final RelatedEntity relatedEntity,
            final HashSet<RelatedEntityRelation> relations) {
        for (RelatedEntityRelation relation : relations) {
            VocabularyRelatedEntity vre = new VocabularyRelatedEntity();
            vre.setStartDate(relatedEntity.getStartDate());
            vre.setEndDate(relatedEntity.getEndDate());
            vre.setVocabularyId(registryVocabulary.getVocabularyId());
            vre.setRelatedEntityId(relatedEntity.getRelatedEntityId());
            vre.setModifiedBy(relatedEntity.getModifiedBy());
            vre.setRelation(relation);
            VocabularyRelatedEntityDAO.saveVocabularyRelatedEntity(vre);
        }
    }

    /** Extract and store the relations between the vocabulary and
     * the related entity.
     * @param relationsJson The JSON containing the relations.
     * @param relations The set storing into which the extracted
     *      relations are stored.
     */
    private void extractRelations(final JsonNode relationsJson,
            final Set<RelatedEntityRelation> relations) {
        if (!relationsJson.isArray()) {
            logger.error("related entity[relationship] is not an array");
        } else {
            for (JsonNode urlJson : relationsJson) {
                try {
                    relations.add(RelatedEntityRelation.fromValue(
                        urlJson.asText()));
                } catch (IllegalArgumentException e) {
                    logger.error("invalid relation: " + urlJson.asText());
                }
            }
        }
    }

    /** Migrate the URLs of a related entity.
     * @param urlsJson The JSON containing the URLs.
     * @param relatedEntityJson The JSON container into which the URLs
     *      are migrated.
     */
    private void migrateRelatedEntityURLs(final JsonNode urlsJson,
            final RelatedEntityCommon relatedEntityJson) {
        if (!urlsJson.isArray()) {
            logger.error("related entity[urls] is not an array");
        } else {
            for (JsonNode urlJson : urlsJson) {
                if (!urlJson.isObject()) {
                    logger.error("related entity[urls] has an element "
                            + "that is not an object");
                } else if (urlJson.size() != 1) {
                    logger.error("related entity[urls] has an element "
                            + "that has not exactly one key/value");
                } else if (!urlJson.has("url")) {
                    logger.error("related entity[urls] has an element "
                            + "that has no url");
                } else {
                    relatedEntityJson.getUri().add(
                            urlJson.get("url").asText());
                }
            }
        }
    }

    /** A regular expression that matches VIAF identifiers.
     * Used for trimming extraneous characters from the end.
     */
    private static Pattern viafPattern = Pattern.compile("^\\d+");

    /** Create and persist a related entity identifier.
     * @param relatedEntity The new "registry" related entity instance.
     *      Used for its startDate and endDate values.
     * @param identifierJson The JSON description of the identifier.
     * @param modifiedBy The modifiedBy value to use for this identifier.
     */
    private void makeRelatedEntityIdentifier(
            final au.org.ands.vocabs.registry.db.entity.RelatedEntity
                relatedEntity,
            final JsonNode identifierJson,
            final String modifiedBy) {
        RelatedEntityIdentifier identifier = new RelatedEntityIdentifier();
        if (!identifierJson.isObject()) {
            logger.error("related entity identifier not an object");
            return;
        }

        String identifierText = null;

        Iterator<Entry<String, JsonNode>> identifierJsonIterator =
                identifierJson.fields();

        while (identifierJsonIterator.hasNext()) {
            Entry<String, JsonNode> entry = identifierJsonIterator.next();
            logger.info("Processing related entity identifier["
                    + entry.getKey() + "]");
            switch (entry.getKey()) {
            case "url":
                // Despite the key "url", this is the value!
                identifierText = entry.getValue().asText().trim();
                break;
            case "id":
                if (!entry.getValue().asText().isEmpty()) {
                    logger.error("non-empty id");
                }
                break;
            default:
                logger.error("makeRelatedEntityIdentifier: "
                        + "Unrecognized element ["
                        + entry.getKey() + "]");
                break;
            }
        }

        if (identifierText == null) {
            logger.info("skipping this identifier, as there is no url value");
            return;
        }
        identifier.setStartDate(relatedEntity.getStartDate());
        identifier.setEndDate(relatedEntity.getEndDate());
        identifier.setRelatedEntityId(relatedEntity.getRelatedEntityId());
        identifier.setModifiedBy(modifiedBy);
        // Try to work out the type.
        RelatedEntityIdentifierType identifierType = null;
        for (Entry<String, RelatedEntityIdentifierType> entry
                : reiPrefixes.entrySet()) {
            if (identifierText.startsWith(entry.getKey())) {
                identifierType = entry.getValue();
                String prefixToRemove =
                        reiPrefixesToRemove.get(identifierType);
                if (prefixToRemove != null) {
                    identifierText =
                            identifierText.substring(prefixToRemove.length());
                }
                break;
            }
        }
        // Special cleanup of some VIAF identifiers
        if (identifierType == RelatedEntityIdentifierType.VIAF) {
            Matcher matcher = viafPattern.matcher(identifierText);
            if (matcher.find()) {
                identifierText = matcher.group();
            }
        }
        // See if it wasn't matched, but is a URL.
        if (identifierType == null && identifierText.startsWith("http")) {
            identifierType = RelatedEntityIdentifierType.URI;
        }
        // Last resort: fall back to "local" type.
        if (identifierType == null) {
            identifierType = RelatedEntityIdentifierType.LOCAL;
        }
        identifier.setIdentifierType(identifierType);
        identifier.setIdentifierValue(identifierText);
        RelatedEntityIdentifierDAO.saveRelatedEntityIdentifierWithId(
                identifier);
    }

    /** Migrate one related entity that is a service.
     * @param vocabulary The "toolkit" vocabulary instance being migrated.
     * @param registryVocabulary The new "registry" vocabulary instance.
     * @param relatedEntityJson The JSON data for the related entity
     *      that is being migrated.
     */
    private void migrateRelatedService(final Vocabulary vocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final JsonNode relatedEntityJson) {
        // relatedEntity will be assigned either an existing
        // entity from the registry database, or a new instance will
        // be created.
        RelatedEntity relatedEntity;
        HashSet<RelatedEntityRelation> relations = new HashSet<>();

        String canonicalRelatedEntity = canonicalizeRelatedEntityJson(
                relatedEntityJson, vocabulary);
        logger.info("canonicalRelatedEntity = " + canonicalRelatedEntity);
        if (relatedEntitiesSeen.containsKey(canonicalRelatedEntity)) {
            logger.info("Found an existing service related entity "
                    + "that will do.");
            relatedEntity = RelatedEntityDAO.getRelatedEntityById(
                    relatedEntitiesSeen.get(canonicalRelatedEntity));
            // Update startDate.
            // We rely on the fact that we migrate published and
            // deprecated vocabularies _before_ we see any drafts.
            // So a draft can reuse an existing currently-valid related
            // entity, but we will never have the case of a published or
            // deprecated vocbulary that has a draft related entity.
            if (registryVocabulary.getStartDate().isBefore(
                    relatedEntity.getStartDate())) {
                relatedEntity.setStartDate(registryVocabulary.getStartDate());
                RelatedEntityDAO.updateRelatedEntity(relatedEntity);
            }
            // No need to touch endDate in migration. There are no
            // endDate values other than those defined in TemporalConstants,
            // and as noted above, because of the order of migration,
            // we never make a published/deprecated related entity into
            // a draft, or vice versa.
            extractRelations(relatedEntityJson.get("relationship"),
                    relations);
        } else {
            // We need a new entity.
            relatedEntity = new RelatedEntity();

            Iterator<Entry<String, JsonNode>> relatedEntityJsonIterator =
                    relatedEntityJson.fields();

            RelatedServiceJson relatedServiceJson = new RelatedServiceJson();
            while (relatedEntityJsonIterator.hasNext()) {
                Entry<String, JsonNode> entry =
                        relatedEntityJsonIterator.next();
                logger.info("Processing related entity[" + entry.getKey()
                + "]");
                switch (entry.getKey()) {
                case "identifiers":
                    // Just check that it is an array, for now.
                    if (!entry.getValue().isArray()) {
                        logger.error("related entity[identifiers] is not "
                                + "an array");
                    }
                    break;
                case "relationship":
                    extractRelations(entry.getValue(), relations);
                    break;
                case "title":
                    relatedEntity.setTitle(entry.getValue().asText());
                case "type":
                    // We already know what type it is
                    break;
                case "urls":
                    migrateRelatedEntityURLs(entry.getValue(),
                            relatedServiceJson);
                    break;
                default:
                    logger.error("migrateRelatedService: Unrecognized element ["
                            + entry.getKey() + "]");
                    break;
                }
            }

            relatedEntity.setStartDate(registryVocabulary.getStartDate());
            relatedEntity.setEndDate(registryVocabulary.getEndDate());
            // Set modifiedBy now, so it can be passed as a parameter
            // to makeRelatedEnttityIdentifier().
            if (vocabulary.getModifiedWho() != null
                    && !vocabulary.getModifiedWho().isEmpty()) {
                logger.info("Copying vocabulary's modifiedWho value "
                        + "to modifiedBy");
                relatedEntity.setModifiedBy(vocabulary.getModifiedWho());
            } else {
                logger.info("Setting modifiedBy value to SYSTEM");
                relatedEntity.setModifiedBy(SYSTEM_USER);
            }
            // The vocabulary owner also owns this related entity.
            relatedEntity.setOwner(vocabulary.getOwner());
            relatedEntity.setType(RelatedEntityType.SERVICE);
            relatedEntity.setData(serializeJsonAsString(relatedServiceJson));

            RelatedEntityDAO.saveRelatedEntityWithId(relatedEntity);
            if (!relatedEntitiesSeen.containsKey(canonicalRelatedEntity)) {
                relatedEntitiesSeen.put(canonicalRelatedEntity,
                        relatedEntity.getRelatedEntityId());
            }

            // We can only process the identifiers after the related entity
            // has been persisted, as each identifier has a foreign
            // key back to the related entity.
            migrateRelatedEntityIdentifiers(relatedEntity,
                    relatedEntityJson.get("identifiers"));
        }
        migrateRelatedEntityRelations(registryVocabulary, relatedEntity,
                relations);
    }

    /** Migrate one related entity that is a vocabulary. Related
     * vocabularies that are external are processed here;
     * interally-related vocabularies are stored away in
     * {@link #relatedVocabularies} for later processing after
     * all vocabularies have been migrated.
     * @param vocabulary The "toolkit" vocabulary instance being migrated.
     * @param registryVocabulary The new "registry" vocabulary instance.
     * @param relatedEntityJson The JSON data for the related entity
     *      that is being migrated.
     */
    private void migrateRelatedVocabulary(final Vocabulary vocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final JsonNode relatedEntityJson) {
        // There are two types of related vocabulary: internal and external.
        // Internally-related vocabularies have one identifier object
        // with a key "slug".
        JsonNode identifiers = relatedEntityJson.get("identifiers");
        if (identifiers != null && identifiers.isObject()
            && identifiers.has("slug")) {
            // Store for now; process later, after all vocabularies
            // have been migrated.
            relatedVocabularies.put(registryVocabulary, relatedEntityJson);
            return;
        }

        // It is an externally-related vocabulary. Treat in a similar
        // way to parties and services.

        // relatedEntity will be assigned either an existing
        // entity from the registry database, or a new instance will
        // be created.
        RelatedEntity relatedEntity;
        HashSet<RelatedEntityRelation> relations = new HashSet<>();

        String canonicalRelatedEntity = canonicalizeRelatedEntityJson(
                relatedEntityJson, vocabulary);
        logger.info("canonicalRelatedEntity = " + canonicalRelatedEntity);
        if (relatedEntitiesSeen.containsKey(canonicalRelatedEntity)) {
            logger.info("Found an existing service related entity "
                    + "that will do.");
            relatedEntity = RelatedEntityDAO.getRelatedEntityById(
                    relatedEntitiesSeen.get(canonicalRelatedEntity));
            // Update startDate.
            // We rely on the fact that we migrate published and
            // deprecated vocabularies _before_ we see any drafts.
            // So a draft can reuse an existing currently-valid related
            // entity, but we will never have the case of a published or
            // deprecated vocbulary that has a draft related entity.
            if (registryVocabulary.getStartDate().isBefore(
                    relatedEntity.getStartDate())) {
                relatedEntity.setStartDate(registryVocabulary.getStartDate());
                RelatedEntityDAO.updateRelatedEntity(relatedEntity);
            }
            // No need to touch endDate in migration. There are no
            // endDate values other than those defined in TemporalConstants,
            // and as noted above, because of the order of migration,
            // we never make a published/deprecated related entity into
            // a draft, or vice versa.
            extractRelations(relatedEntityJson.get("relationship"),
                    relations);
        } else {
            // We need a new entity.
            relatedEntity = new RelatedEntity();

            Iterator<Entry<String, JsonNode>> relatedEntityJsonIterator =
                    relatedEntityJson.fields();

            RelatedVocabularyJson relatedVocabularyJson =
                    new RelatedVocabularyJson();
            while (relatedEntityJsonIterator.hasNext()) {
                Entry<String, JsonNode> entry =
                        relatedEntityJsonIterator.next();
                logger.info("Processing related entity[" + entry.getKey()
                + "]");
                switch (entry.getKey()) {
                case "identifiers":
                    // Just check that it is an array, for now.
                    if (!entry.getValue().isArray()) {
                        logger.error("related entity[identifiers] is not "
                                + "an array");
                    }
                    break;
                case "relationship":
                    extractRelations(entry.getValue(), relations);
                    break;
                case "title":
                    relatedEntity.setTitle(entry.getValue().asText());
                case "type":
                    // We already know what type it is.
                    break;
                case "urls":
                    migrateRelatedEntityURLs(entry.getValue(),
                            relatedVocabularyJson);
                    break;
                default:
                    logger.error("migrateRelatedVocabulary: "
                            + "Unrecognized element [" + entry.getKey() + "]");
                    break;
                }
            }

            relatedEntity.setStartDate(registryVocabulary.getStartDate());
            relatedEntity.setEndDate(registryVocabulary.getEndDate());
            // Set modifiedBy now, so it can be passed as a parameter
            // to makeRelatedEnttityIdentifier().
            if (vocabulary.getModifiedWho() != null
                    && !vocabulary.getModifiedWho().isEmpty()) {
                logger.info("Copying vocabulary's modifiedWho value "
                        + "to modifiedBy");
                relatedEntity.setModifiedBy(vocabulary.getModifiedWho());
            } else {
                logger.info("Setting modifiedBy value to SYSTEM");
                relatedEntity.setModifiedBy(SYSTEM_USER);
            }
            // The vocabulary owner also owns this related entity.
            relatedEntity.setOwner(vocabulary.getOwner());
            relatedEntity.setType(RelatedEntityType.VOCABULARY);
            relatedEntity.setData(serializeJsonAsString(relatedVocabularyJson));

            RelatedEntityDAO.saveRelatedEntityWithId(relatedEntity);
            if (!relatedEntitiesSeen.containsKey(canonicalRelatedEntity)) {
                relatedEntitiesSeen.put(canonicalRelatedEntity,
                        relatedEntity.getRelatedEntityId());
            }

            // We can only process the identifiers after the related entity
            // has been persisted, as each identifier has a foreign
            // key back to the related entity.
            migrateRelatedEntityIdentifiers(relatedEntity,
                    relatedEntityJson.get("identifiers"));
        }
        migrateRelatedEntityRelations(registryVocabulary, relatedEntity,
                relations);
    }

    /** Migrate interally-related vocabularies as stored in
     * {@link #relatedVocabularies}. Invoke this method after
     * all vocabularies have been migrated.
     */
    private void migrateInternallyRelatedVocabularies() {
        for (Entry<au.org.ands.vocabs.registry.db.entity.Vocabulary,
                JsonNode> entry : relatedVocabularies.entrySet()) {
            au.org.ands.vocabs.registry.db.entity.Vocabulary v1
                = entry.getKey();
            JsonNode relatedJson = entry.getValue();
            String slug = relatedJson.get("identifiers").get("slug").asText();
            au.org.ands.vocabs.registry.db.entity.Vocabulary v2
                = getPublishedOrDeprecatedVocabularyBySlug(slug);
            if (v2 == null) {
                // No published or deprecated; try draft.
                v2 = getDraftVocabularyBySlug(slug);
            }
            if (v2 == null) {
                // No vocabulary at all.
                logger.error("Can't relate to internally related vocabulary; "
                        + "no such slug: " + slug);
                continue;
            }
            HashSet<RelatedEntityRelation> relations = new HashSet<>();
            extractRelations(relatedJson.get("relationship"),
                    relations);
            migrateRelatedInternalVocabularyRelations(v1, v2, relations);
        }
    }

    /** Create instances of VocabularyRelatedVocabulary for each relation
     * by which the vocabulary is related to the internally-related vocabulary.
     * @param registryVocabulary The migrated "registry" vocabulary.
     * @param relatedVocabulary The internally-related vocabulary.
     * @param relations The set of relations.
     */
    private void migrateRelatedInternalVocabularyRelations(
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                relatedVocabulary,
            final HashSet<RelatedEntityRelation> relations) {
        for (RelatedEntityRelation relation : relations) {
            VocabularyRelatedVocabulary vrv = new VocabularyRelatedVocabulary();
            LocalDateTime startDate = registryVocabulary.getStartDate();
            LocalDateTime endDate = registryVocabulary.getEndDate();
            // It's possible for vocab A to link to vocab B,
            // where B was created after A. (After creating B, you edit
            // A to add the link.) So compare the start dates of the
            // two vocabularies, and choose the later date.
            // This works for both current and draft vocabularies.
            if (relatedVocabulary.getStartDate().isAfter(startDate)) {
                startDate = relatedVocabulary.getStartDate();
            }
            if (relatedVocabulary.getEndDate().isAfter(endDate)) {
                endDate = relatedVocabulary.getEndDate();
            }
            vrv.setStartDate(startDate);
            vrv.setEndDate(endDate);
            vrv.setVocabularyId(registryVocabulary.getVocabularyId());
            vrv.setRelatedVocabularyId(relatedVocabulary.getVocabularyId());
            vrv.setModifiedBy(registryVocabulary.getModifiedBy());
            vrv.setRelation(RelatedVocabularyRelation.fromValue(
                    relation.value()));
            VocabularyRelatedVocabularyDAO.saveVocabularyRelatedVocabulary(vrv);
        }
    }

    /** Copy the fields from a "toolkit" version instance
     * to a new "registry" version instance.
     * @param toolkitEm The Toolkit EntityManager to use.
     * @param vocabulary The "toolkit" vocabulary which owns the versions
     *      that are being migrated.
     * @param registryVocabulary The vocabulary record
     *      as it has been migrated to the registry database.
     * @param version The "toolkit" version instance being migrated.
     * @param registryVersion The new "registry" version instance.
     * @param isDraft Whether this is representing a draft record. Based
     *      on whether the containing vocabulary is a draft.
     */
    @SuppressWarnings("checkstyle:MethodLength")
    private void copyVersionFields(final EntityManager toolkitEm,
            final Vocabulary vocabulary,
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final Version version,
            final au.org.ands.vocabs.registry.db.entity.Version
                registryVersion,
                final boolean isDraft) {
        String originalData = version.getData();
        if (originalData == null || originalData.isEmpty()) {
            originalData = "{}";
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(originalData);
        Iterator<Entry<String, JsonNode>> dataJsonIterator =
                dataJson.fields();

        //  The JSON data to be stored into the data field of the new
        // registry record.
        VersionJson versionJson = new VersionJson();

        while (dataJsonIterator.hasNext()) {
            Entry<String, JsonNode> entry = dataJsonIterator.next();
            logger.info("Processing version data[" + entry.getKey()
            + "]");
            switch (entry.getKey()) {
            case "access_points":
                // The portal should not use any of these values.
                logger.info("Ignoring data[access_points]");
                break;
            case "concepts_list":
                logger.info("ingoring concept_list version artefact for now");
                break;
            case "concepts_tree":
                logger.info("ignoring concept_tree version artefact for now");
                break;
            case "created_date":
                if (!entry.getValue().asText().
                        equals(null)) {
                    logger.error("data[created_date] doesn't match");
                }
                break;
            case "id":
                if (Integer.parseInt(entry.getValue().asText())
                        != version.getId()) {
                    if (isDraft) {
                        // It seems that it can be that they don't match.
                        // For now, I assume that this only happens for
                        // a draft. But I don't understand how this
                        // can happen.
                        logger.info("ignoring that data[id] of a draft "
                                + "that doesn't match ");
                    } else {
                        logger.info("data[id] doesn't match");
                    }
                }
                break;
            case "note":
                versionJson.setNote(entry.getValue().asText());
                break;
            case "release_date":
                registryVersion.setReleaseDate(entry.getValue().asText());
                break;
            case "status":
                if (!entry.getValue().asText().
                        equals(version.getStatus())) {
                    logger.error("data[status] doesn't match");
                }
                break;
            case "title":
                if (!entry.getValue().asText().
                        equals(version.getTitle())) {
                    logger.error("data[title] doesn't match");
                }
                break;
            case "version_access_points":
                // When the current portal loads vocabulary data from
                // database, it overwrites any of these values by
                // reading directly from the access_points table.
                logger.info("Ignoring data[version_access_points]");
                break;
            case "vocab_id":
                // This value can be bogus! So just ignore it.
                break;

            // Ancient fields we don't expect to see, and fields we ignore.
            case "provider_type":
                // provider_type is used by the CMS page, but doesn't
                // seem to be "new information". If needed on a subsequent
                // edit of a vocab, I think the value could be computed.
                logger.info("Ignoring data[provider_type]");
                break;
            case "repository_id":
                logger.info("Ignoring data[repository_id]");
                break;

            default:
                logger.error("copyVersionFields: "
                        + "Unrecognized data element data["
                        + entry.getKey() + "]");
                break;
            }
        }

        versionJson.setTitle(version.getTitle());

        // For draft records, we can't use the startDate/endDate values to
        // represent record creation/modification times. So we store
        // them in the data field.
        if (isDraft) {
            logger.info("Storing creation_date/modification_date values "
                    + "for draft record");
            versionJson.setDraftCreatedDate(dateToLocalDateTime(
                    vocabulary.getCreatedDate()).toString());
            versionJson.setDraftModifiedDate(dateToLocalDateTime(
                    vocabulary.getModifiedDate()).toString());
        }

        // For the "do_import" flag, see if there are any
        // local apiSparql access points for this version.
        List<AccessPoint> aps;
        aps =
                toolkitEm.createQuery("SELECT ap FROM AccessPoint ap "
                        + "WHERE ap.versionId = :versionId "
                        + "AND ap.type = 'apiSparql'",
                        AccessPoint.class).
                setParameter("versionId", version.getId()).
                getResultList();
        logger.info("Got " + aps.size() + " apiSparql access point(s).");
        // Set import flag based on the presence of a sissvoc access point.
        versionJson.setDoImport(false);
        for (AccessPoint ap : aps) {
            if (AccessPointUtils.getPortalSource(ap).
                    equals(AccessPoint.SYSTEM_SOURCE)) {
                // Found an apiSparql access point with source="system".
                // We consider this to mean that the version is
                // "published".
                logger.info("Found an apiSparql access point with "
                        + "source=system.");
                versionJson.setDoImport(true);
            }
        }

        // For the "do_publish" flag, see if there are any
        // local SISSVoc access points for this version.
        aps =
                toolkitEm.createQuery("SELECT ap FROM AccessPoint ap "
                        + "WHERE ap.versionId = :versionId "
                        + "AND ap.type = 'sissvoc'",
                        AccessPoint.class).
                setParameter("versionId", version.getId()).
                getResultList();
        logger.info("Got " + aps.size() + " sissvoc access point(s).");
        // Set publish flag based on the presence of a sissvoc access point.
        versionJson.setDoPublish(false);
        for (AccessPoint ap : aps) {
            if (AccessPointUtils.getPortalSource(ap).
                    equals(AccessPoint.SYSTEM_SOURCE)) {
                // Found a sissvoc access point with source="system".
                // We consider this to mean that the version is
                // "published".
                logger.info("Found a sissvoc access point with source=system.");
                versionJson.setDoPublish(true);
            }
        }

        // Now set the other top-level values.
        registryVersion.setVocabularyId(registryVocabulary.getVocabularyId());
        registryVersion.setModifiedBy(registryVocabulary.getModifiedBy());
        registryVersion.setStatus(VersionStatus.fromValue(
                version.getStatus()));
        registryVersion.setSlug(ToolkitFileUtils.makeSlug(version.getTitle()));
        registryVersion.setData(serializeJsonAsString(versionJson));
    }

    /** Use the fields in a "toolkit" version instance
     * to create new database entities related to the new registry version.
     * @param registryVocabulary The vocabulary record
     *      as it has been migrated to the registry database.
     * @param version The "toolkit" version instance being migrated.
     * @param registryVersion The new "registry" version instance.
     */
    private void processVersionRelatedDbEntities(
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final Version version,
            final au.org.ands.vocabs.registry.db.entity.Version
                registryVersion) {
        String originalData = version.getData();
        if (originalData == null || originalData.isEmpty()) {
            originalData = "{}";
        }
        JsonNode dataJson = TaskUtils.jsonStringToTree(originalData);
        Iterator<Entry<String, JsonNode>> dataJsonIterator =
                dataJson.fields();

        while (dataJsonIterator.hasNext()) {
            Entry<String, JsonNode> entry = dataJsonIterator.next();
            logger.info("Processing version data[" + entry.getKey()
            + "]");
            switch (entry.getKey()) {
            case "concepts_list":
                logger.info("adding concept_list version artefact");
                VaConceptList vacl = new VaConceptList();
                vacl.setPath(entry.getValue().asText());
                addVersionArtefact(registryVocabulary,
                        registryVersion,
                        VersionArtefactType.CONCEPT_LIST,
                        vacl);
                break;
            case "concepts_tree":
                logger.info("adding concept_tree version artefact");
                VaConceptTree vact = new VaConceptTree();
                vact.setPath(entry.getValue().asText());
                addVersionArtefact(registryVocabulary,
                        registryVersion,
                        VersionArtefactType.CONCEPT_TREE,
                        vact);
                break;

            // These fields are ignored, this time around.
            case "access_points":
            case "created_date":
            case "id":
            case "note":
            case "provider_type":
            case "release_date":
            case "repository_id":
            case "status":
            case "title":
            case "version_access_points":
            case "vocab_id":
                break;

            default:
                logger.error("processVersionRelatedDbEntities: "
                        + "Unrecognized data element data["
                        + entry.getKey() + "]");
                break;
            }
        }

    }

    /** Copy the fields from a "toolkit" access point instance
     * to a new "registry" access point instance.
     * @param vocabulary The "toolkit" vocabulary which owns the access points
     *      that are being migrated.
     * @param registryVersion The version record
     *      as it has been migrated to the registry database.
     * @param accessPoint The "toolkit" access point instance being migrated.
     * @param registryAccessPoint The new "registry" access point instance.
     * @param isDraft Whether this is representing a draft record. Based
     *      on whether the containing vocabulary is a draft.
     */
    private void copyAccessPointFields(final Vocabulary vocabulary,
            final au.org.ands.vocabs.registry.db.entity.Version
                registryVersion,
            final AccessPoint accessPoint,
            final au.org.ands.vocabs.registry.db.entity.AccessPoint
                registryAccessPoint,
                final boolean isDraft) {
        String originalPortalData = accessPoint.getPortalData();
        if (originalPortalData == null || originalPortalData.isEmpty()) {
            originalPortalData = "{}";
        }
        String originalToolkitData = accessPoint.getToolkitData();
        if (originalToolkitData == null || originalToolkitData.isEmpty()) {
            originalToolkitData = "{}";
        }
        JsonNode portalDataJson =
                TaskUtils.jsonStringToTree(originalPortalData);
        JsonNode toolkitDataJson =
                TaskUtils.jsonStringToTree(originalToolkitData);

        //  The JSON data to be stored into the data field of the new
        // registry record.
        ApCommon apData;

        switch (accessPoint.getType()) {
        case "apiSparql":
            registryAccessPoint.setType(AccessPointType.API_SPARQL);
            ApApiSparql apApiSparql = new ApApiSparql();
            apApiSparql.setSource(ApSource.fromValue(
                    portalDataJson.get("source").asText()));
            apApiSparql.setUrl(portalDataJson.get("uri").asText());
            apData = apApiSparql;
            break;
        case "file":
            registryAccessPoint.setType(AccessPointType.FILE);
            ApFile apFile = new ApFile();
            apFile.setFormat(portalDataJson.get("format").asText());
            apFile.setUrl(portalDataJson.get("uri").asText());
            apFile.setPath(toolkitDataJson.get("path").asText());
            apData = apFile;
            break;
        case "sesameDownload":
            registryAccessPoint.setType(AccessPointType.SESAME_DOWNLOAD);
            ApSesameDownload apSesameDownload = new ApSesameDownload();
            String internalUrl = toolkitDataJson.get("uri").asText();
            // The last slash goes into the server base.
            int lastSlashPlusOne = internalUrl.lastIndexOf('/') + 1;
            apSesameDownload.setServerBase(
                    internalUrl.substring(0, lastSlashPlusOne));
            apSesameDownload.setRepository(
                    internalUrl.substring(lastSlashPlusOne));
            apSesameDownload.setUrlPrefix(portalDataJson.get("uri").asText());
            apData = apSesameDownload;
            break;
        case "sissvoc":
            registryAccessPoint.setType(AccessPointType.SISSVOC);
            ApSissvoc apSissvoc = new ApSissvoc();
            apSissvoc.setSource(ApSource.fromValue(
                    portalDataJson.get("source").asText()));
            apSissvoc.setUrlPrefix(portalDataJson.get("uri").asText());
            apData = apSissvoc;
            break;
        case "webPage":
            registryAccessPoint.setType(AccessPointType.WEB_PAGE);
            ApWebPage apWebPage = new ApWebPage();
            apWebPage.setUrl(portalDataJson.get("uri").asText());
            apData = apWebPage;
            break;
        default:
            logger.error("Unrecognized access point type (\""
                    + accessPoint.getType() + "\"); skipping");
            return;
        }

        // For draft records, we can't use the startDate/endDate values to
        // represent record creation/modification times. So we store
        // them in the data field.
        if (isDraft) {
            logger.info("Storing creation_date/modification_date values "
                    + "for draft record");
            apData.setDraftCreatedDate(dateToLocalDateTime(
                    vocabulary.getCreatedDate()).toString());
            apData.setDraftModifiedDate(dateToLocalDateTime(
                    vocabulary.getModifiedDate()).toString());
        }

        // Now set the other top-level values.
        registryAccessPoint.setVersionId(registryVersion.getVersionId());
        registryAccessPoint.setModifiedBy(registryVersion.getModifiedBy());
        registryAccessPoint.setData(serializeJsonAsString(apData));
    }

    /** Create a version artefact related to a registry version.
     * @param registryVocabulary The vocabulary record
     *      as it has been migrated to the registry database.
     * @param registryVersion The version record as it has been migrated
     *      to the registry database. The record must have been persisted,
     *      so that its id and modifiedBy properties are set.
     * @param artefactType The type of the version artefact to be created.
     * @param artefactData The data to be recorded for this artefact.
     */
    private void addVersionArtefact(
            final au.org.ands.vocabs.registry.db.entity.Vocabulary
                registryVocabulary,
            final au.org.ands.vocabs.registry.db.entity.Version
                registryVersion,
            final VersionArtefactType artefactType,
            final VaCommon artefactData) {
        VersionArtefact va = new VersionArtefact();
        // Use the migrated vocabulary record's start and end dates.
        // This is correct for all vocabulary status values.
        va.setStartDate(registryVocabulary.getStartDate());
        va.setEndDate(registryVocabulary.getEndDate());
        va.setVersionId(registryVersion.getVersionId());
        va.setModifiedBy(registryVersion.getModifiedBy());
        va.setStatus(VersionArtefactStatus.CURRENT);
        va.setType(artefactType);
        va.setData(serializeJsonAsString(artefactData));
        VersionArtefactDAO.saveVersionArtefactWithId(va);
    }

    /** Migrate the tasks.
     * @param toolkitEm The Toolkit EntityManager to use.
     */
    private void migrateTasks(final EntityManager toolkitEm) {
        List<Task> tasks;
        tasks =
                toolkitEm.createQuery("SELECT t FROM Task t",
                        Task.class).
                getResultList();
        logger.info("Got " + tasks.size() + " tasks(s).");
        for (Task task : tasks) {
            logger.info("Processing task with id: "
                    + task.getId());
            if (!migratedVersions.containsKey(task.getVersionId())) {
                logger.info("task refers to a no-longer-existing version; "
                        + "skipping");
                continue;
            }
            au.org.ands.vocabs.registry.db.entity.Task registryTask =
                new au.org.ands.vocabs.registry.db.entity.Task();
            registryTask.setVocabularyId(migratedVocabularies.get(
                    task.getVocabularyId()));
            registryTask.setVersionId(migratedVersions.get(
                    task.getVersionId()));
            registryTask.setParams(task.getParams());
            if (task.getResponse() == null) {
                // Cope with a null response.
                task.setResponse("{}");
            }
            registryTask.setResponse(task.getResponse());
            if (task.getStatus() == null) {
                // Cope with a null status.
                task.setStatus("error");
            }
            registryTask.setStatus(TaskStatus.fromValue(
                    task.getStatus().toLowerCase(Locale.ROOT)));
            TaskDAO.saveTask(registryTask);
        }
    }

    /** Migrate the resource map.
     * @param toolkitEm The Toolkit EntityManager to use.
     */
    private void migrateResourceMap(final EntityManager toolkitEm) {
        List<ResourceMapEntry> rmes;
        rmes =
                toolkitEm.createQuery("SELECT rme FROM ResourceMapEntry rme",
                        ResourceMapEntry.class).
                getResultList();
        logger.info("Got " + rmes.size() + " resource map entry(s).");
        for (ResourceMapEntry rme : rmes) {
            logger.info("Processing resource map entry with id: "
                    + rme.getId());
            au.org.ands.vocabs.registry.db.entity.ResourceMapEntry
            registryRme =
                new au.org.ands.vocabs.registry.db.entity.ResourceMapEntry();

            registryRme.setIri(rme.getIri());
            registryRme.setAccessPointId(migratedAccessPoints.get(
                    rme.getAccessPointId()));
            registryRme.setOwned(rme.getOwned());
            registryRme.setResourceType(rme.getResourceType());
            registryRme.setDeprecated(rme.getDeprecated());
            ResourceMapEntryDAO.saveResourceMapEntry(registryRme);
        }
    }

    /** Migrate the resource owner hosts.
     * @param toolkitEm The Toolkit EntityManager to use.
     */
    private void migrateResourceOwnerHosts(final EntityManager toolkitEm) {
        List<ResourceOwnerHost> rohs;
        rohs =
                toolkitEm.createQuery("SELECT roh FROM ResourceOwnerHost roh",
                        ResourceOwnerHost.class).
                getResultList();
        logger.info("Got " + rohs.size() + " resource owner host(s).");
        for (ResourceOwnerHost roh : rohs) {
            logger.info("Processing resource owner host with id: "
                    + roh.getId());
            au.org.ands.vocabs.registry.db.entity.ResourceOwnerHost
            registryRoh =
                new au.org.ands.vocabs.registry.db.entity.ResourceOwnerHost();
            registryRoh.setStartDate(roh.getStartDate());
            registryRoh.setEndDate(roh.getEndDate());
            registryRoh.setOwner(roh.getOwner());
            registryRoh.setHost(roh.getHost());
            ResourceOwnerHostDAO.saveResourceOwnerHost(registryRoh);
        }
    }

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

    /** Serialize one of our custom JSON storage objects as a String,
     * in JSON format. Possible future work if you feel like it:
     * adjust the generation of the custom JSON storage classes so
     * that they implement a particular interface, then use that
     * interface as the type of jsonObject, rather than just
     * {@link Object}.
     * @param jsonObject The object to be serialized. It should be
     *      an instance of one of our custom JSON storage objects,
     *      such as {@link VocabularyJson} or {@link VersionJson}.
     * @return The serialization as a String of jsonObject.
     */
    private String serializeJsonAsString(final Object jsonObject) {
        try {
            return jsonMapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            logger.error("Unable to serialize JSON", e);
            return "";
        }
    }

}
