<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!--
    Code generation for the registry database.
    The input file is a description of the database as a Liquibase
    change log.
    The output files are Java source code for database entity classes
    and DAO classes.
-->

<xsl:transform version="2.0"
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:dcl="http://www.liquibase.org/xml/ns/dbchangelog"
               xmlns:local="http://dummy/"
               xmlns:xslOut="dummy">

  <xsl:param
      name="generate-serialVersionUIDs">true</xsl:param>

  <!-- Package that contains the DBContext and TemporalUtils classes. -->
  <xsl:param
      name="context-package">au.org.ands.vocabs.registry.db.context</xsl:param>

  <!-- Package into which the entity classes go. -->
  <xsl:param
      name="entity-package">au.org.ands.vocabs.registry.db.entity</xsl:param>

  <!-- Package into which the DAO classes go. -->
  <xsl:param
      name="dao-package">au.org.ands.vocabs.registry.db.dao</xsl:param>

  <!-- All other information required to turn the Liquibase data into Java
       code. E.g., name of the Java entity class, serial version UID,
       queries to generate, ...
  -->
  <xsl:param name="db-entity-mapping"
             select="document('registry-db-entity-map.xml')" />

  <xsl:key name="db-to-entity"
           match="map"
           use="lower-case(@tableName)" />

  <!-- Mapping of database column types to Java types.
       E.g., INTEGER -> Integer. -->
  <xsl:param name="db-type-mapping"
             select="document('db-type-map.xml')" />

  <xsl:key name="db-to-type"
           match="map"
           use="@dbType" />

  <!-- Package that includes the Java enumerated types referred to by
       db-entity-mapping. -->
  <xsl:param name="enum-package">au.org.ands.vocabs.registry.enums</xsl:param>

  <xsl:output method="text">
  </xsl:output>

  <!-- Without appropriate strip-space elements, bogus spaces appear
       in the output. -->
  <!-- Ignore all spaces in the Liquibase data. -->
  <xsl:strip-space elements="dcl:*" />
  <!-- Ignore spaces in some elements of the db-entity-mapping. -->
  <xsl:strip-space elements="foreignKeyQueries" />
  <xsl:strip-space elements="extraQueries" />

  <xsl:template match="dcl:changeSet">
    <xsl:apply-templates />
  </xsl:template>

  <!-- Sub-elements of dcl:changeSet to be ignored during parsing. -->
  <xsl:template match="dcl:comment" />
  <xsl:template match="dcl:createIndex" />

  <!-- =============================================================
       Main template to generate one entity class and its DAO class.
       ============================================================= -->

  <xsl:template match="dcl:createTable">
    <!-- =============== Variable definitions =============== -->
    <!-- Name of the entity, e.g., Vocabulary, Version, ... -->
    <xsl:variable name="entityName"
                  select="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/@entityName" />
    <!-- Serial version UID to use, e.g., 6332868269699981887L, ... -->
    <xsl:variable name="serialVersionUID"
                  select="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/@serialVersionUID" />
    <!-- The idKey element for this entity. There is one of these for
         entities that have a corresponding ...Id entity, e.g.,
         Vocabulary, Version, .... The idKey element has attributes
         keyColumn (the name of the database column that is a "foreign
         key" to the ...Id entity (e.g., vocabulary_id), and
         entityName, which is the name of the ...Id entity (e.g.,
         VocabularyId).
    -->
    <xsl:variable name="idKey"
                  select="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/idKey" />
    <!-- The foreignKeyQueries element of this entity, which contains
         sub-elements of type foreignKeyQuery. -->
    <xsl:variable name="foreignKeyQueries"
                  select="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/foreignKeyQueries" />
    <!-- The extraQueries element of this entity, which contains
         sub-elements of type extraQuery. -->
    <xsl:variable name="extraQueries"
                  select="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/extraQueries" />
    <!-- Whether or not this entity has any columns of type DATETIME.
         This is used to decide whether or not to import the necessary
         Java date/time class. Note that we also cope with
         "datetime(6)", because this is what comes back from
         generateChangelog applied to a live MySQL database.
         We also cope with "TIMESTAMP", because this is what comes
         back from generateChangelog applied to an H2 database.
    -->
    <xsl:variable name="usesDATETIME"
                  select="dcl:column[matches(@type,
                          '^DATETIME|^TIMESTAMP', 'i')]" />
    <!-- Whether or not this entity has columns for
         start/end date/time used to support history.
         This is used to decide whether or not to import the
         supporting TemporalUtils class.
    -->
    <xsl:variable name="hasStartEndDateTime"
                  select="dcl:column[lower-case(@name)='start_date']" />
    <!-- Whether or not this entity uses enumerated types.
         This is used to decide whether or not to import the
         Enumerated annotation class.
    -->
    <xsl:variable name="hasEnumeratedType"
                  select="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/column[@enum]" />
    <!-- The directory into which the generated entity class will go.
         Based on the entity class's package name. -->
    <xsl:variable name="entity-output-directory"
                  select="translate($entity-package,'.','/')"/>
    <!-- The directory into which the generated DAO class will go.
         Based on the DAO class's package name. -->
    <xsl:variable name="dao-output-directory"
                  select="translate($dao-package,'.','/')"/>

    <!-- ======================
         Generate entity class.
         ====================== -->

    <xsl:result-document method="text"
                         href="{$entity-output-directory}/{$entityName}.java">
      <xsl:text>/** See the file "LICENSE" for the full license governing this code. */

/* This file is auto-generated. Do not edit it! */

package </xsl:text>
<xsl:value-of select="$entity-package"/>
<xsl:text>;

import java.io.Serializable;
</xsl:text>

<xsl:choose>
  <xsl:when test="$usesDATETIME">
<xsl:text>import java.time.LocalDateTime;

</xsl:text>
  </xsl:when>
  <xsl:otherwise />
</xsl:choose>

<xsl:text>import javax.persistence.Column;
import javax.persistence.Entity;
</xsl:text>
<xsl:if test="$hasEnumeratedType">import javax.persistence.EnumType;
import javax.persistence.Enumerated;
</xsl:if>
<xsl:text>import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

</xsl:text>
<xsl:choose>
  <xsl:when test="$hasStartEndDateTime">
<xsl:text>import </xsl:text><xsl:value-of select="$context-package"/>.TemporalUtils;
/* import static <xsl:value-of select="$context-package"/>.TemporalUtils.E1; */

</xsl:when>
  <xsl:otherwise />
</xsl:choose>
<!-- Import enumerated types -->
<xsl:for-each select="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/column[@enum]">
<xsl:sort select="@enum"/>import <xsl:value-of select="$enum-package" />.<xsl:value-of select="@enum" />;
</xsl:for-each>
<!-- Did we import any enumerated types? If so, add a blank line. -->
<xsl:if test="key('db-to-entity', lower-case(@tableName),
                          $db-entity-mapping)/column">
<xsl:text>
</xsl:text>
</xsl:if>/** <xsl:value-of select="$entityName" /> model class. */
@Entity
@Table(name = <xsl:value-of select="$entityName" />.TABLE_NAME)
/* Rather than including the text of the queries directly in the
 * annotations, we use constants defined in the class itself.
 * This way, they can be found (fully expanded!) in the generated Javadoc
 * in the "Constant Field Values" page.
 * See package-info.java for an explanation of the named queries.
 */
@NamedQueries({
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                GET_ALL_<xsl:value-of select="upper-case($entityName)" />,
            query = <xsl:value-of select="$entityName" />.
                GET_ALL_<xsl:value-of select="upper-case($entityName)" />_QUERY)<xsl:choose><xsl:when test="$hasStartEndDateTime">,
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                GET_ALL_CURRENT_<xsl:value-of select="upper-case($entityName)" />,
            query = <xsl:value-of select="$entityName" />.
                GET_ALL_CURRENT_<xsl:value-of select="upper-case($entityName)" />_QUERY),
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                GET_ALL_DRAFT_<xsl:value-of select="upper-case($entityName)" />,
            query = <xsl:value-of select="$entityName" />.
                GET_ALL_DRAFT_<xsl:value-of select="upper-case($entityName)" />_QUERY),<xsl:choose><xsl:when test="$idKey">
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                HAS_DRAFT_<xsl:value-of select="upper-case($entityName)" />,
            query = <xsl:value-of select="$entityName" />.
                HAS_DRAFT_<xsl:value-of select="upper-case($entityName)" />_QUERY),</xsl:when></xsl:choose></xsl:when></xsl:choose><xsl:choose><xsl:when test="$idKey">
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_BY_ID,
            query = <xsl:value-of select="$entityName" />.
                GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_BY_ID_QUERY)</xsl:when><xsl:otherwise /></xsl:choose><xsl:apply-templates select="$foreignKeyQueries" mode="annotation">
  <xsl:with-param name="entityName" select="$entityName" />
  <xsl:with-param name="addTemporalVersion" select="$idKey" />
</xsl:apply-templates>
<xsl:apply-templates select="$extraQueries" mode="annotation">
  <xsl:with-param name="entityName" select="$entityName" />
</xsl:apply-templates>
})
public class <xsl:value-of select="$entityName" />
    implements Serializable {

<xsl:choose>
  <xsl:when test="$generate-serialVersionUIDs='true'">    /** Serial version UID for serialization. */
    private static final long serialVersionUID = <xsl:value-of select="$serialVersionUID" />;

</xsl:when>
  <xsl:otherwise />
</xsl:choose>    /** The name of the underlying database table.
     * Use this in the class's {@code @Table} annotation. */
    public static final String TABLE_NAME = "<xsl:value-of select="lower-case(@tableName)" />";

    /** Name of getAll<xsl:value-of select="$entityName" /> query. */
    public static final String GET_ALL_<xsl:value-of select="upper-case($entityName)" /> =
            "getAll<xsl:value-of select="$entityName" />";
    /** Query of getAll<xsl:value-of select="$entityName" /> query. */
    protected static final String GET_ALL_<xsl:value-of select="upper-case($entityName)" />_QUERY =
            "SELECT entity FROM <xsl:value-of select="$entityName" /> entity";

<xsl:choose><xsl:when test="$hasStartEndDateTime">    /** Name of getAllCurrent<xsl:value-of select="$entityName" /> query. */
    public static final String GET_ALL_CURRENT_<xsl:value-of select="upper-case($entityName)" /> =
            "getAllCurrent<xsl:value-of select="$entityName" />";
    /** Query of getAllCurrent<xsl:value-of select="$entityName" /> query. */
    protected static final String
        GET_ALL_CURRENT_<xsl:value-of
    select="upper-case($entityName)" />_QUERY =
        "SELECT entity FROM <xsl:value-of select="$entityName" /> entity"
        + TemporalUtils.WHERE_TEMPORAL_QUERY_VALID_SUFFIX;

<xsl:choose><xsl:when test="$idKey">    /** Name of hasDraft<xsl:value-of select="$entityName" /> query. */
    public static final String HAS_DRAFT_<xsl:value-of select="upper-case($entityName)" /> =
            "hasDraft<xsl:value-of select="$entityName" />";
    /** Query of hasDraft<xsl:value-of select="$entityName" /> query. */
    protected static final String
        HAS_DRAFT_<xsl:value-of
    select="upper-case($entityName)" />_QUERY =
        "SELECT COUNT(entity) > 0 FROM <xsl:value-of select="$entityName" /> entity"
        + " WHERE <xsl:call-template name="CamelCaseNotFirst">
          <xsl:with-param name="text" select="$idKey/@keyColumn" />
        </xsl:call-template> = :id"
        + TemporalUtils.AND_TEMPORAL_QUERY_ALL_DRAFT_SUFFIX;

</xsl:when></xsl:choose>    /** Name of getAllDraft<xsl:value-of select="$entityName" /> query. */
    public static final String GET_ALL_DRAFT_<xsl:value-of select="upper-case($entityName)" /> =
            "getAllDraft<xsl:value-of select="$entityName" />";
    /** Query of getAllDraft<xsl:value-of select="$entityName" /> query. */
    protected static final String
        GET_ALL_DRAFT_<xsl:value-of
    select="upper-case($entityName)" />_QUERY =
        "SELECT entity FROM <xsl:value-of select="$entityName" /> entity"
        + TemporalUtils.WHERE_TEMPORAL_QUERY_ALL_DRAFT_SUFFIX;

</xsl:when></xsl:choose><xsl:choose><xsl:when test="$idKey">    /** Name of getCurrent<xsl:value-of select="$entityName" />ById query. */
    public static final String GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_BY_ID =
            "getCurrent<xsl:value-of select="$entityName" />ById";
    /** Query of getCurrent<xsl:value-of select="$entityName" />ById query. */
    protected static final String
        GET_CURRENT_<xsl:value-of
    select="upper-case($entityName)" />_BY_ID_QUERY =
        "SELECT entity FROM <xsl:value-of select="$entityName" /> entity"
        + " WHERE <xsl:call-template name="CamelCaseNotFirst">
          <xsl:with-param name="text" select="$idKey/@keyColumn" />
        </xsl:call-template> = :id"
        + TemporalUtils.AND_TEMPORAL_QUERY_VALID_SUFFIX;

</xsl:when>
      <xsl:otherwise /></xsl:choose><xsl:apply-templates select="$foreignKeyQueries" mode="constants">
  <xsl:with-param name="entityName" select="$entityName" />
  <xsl:with-param name="addTemporalVersion" select="$idKey" />
</xsl:apply-templates>
<xsl:apply-templates select="$extraQueries" mode="constants">
</xsl:apply-templates>
<xsl:apply-templates />
<xsl:text>}
</xsl:text>
    </xsl:result-document>

    <!-- ===================
         Generate DAO class.
         =================== -->

    <xsl:result-document method="text"
                         href="{$dao-output-directory}/{$entityName}DAO.java">
      <xsl:text>/** See the file "LICENSE" for the full license governing this code. */

/* This file is auto-generated. Do not edit it! */

package </xsl:text>
<xsl:value-of select="$dao-package"/>
<xsl:text>;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import </xsl:text><xsl:value-of select="$context-package"/>.DBContext;
<xsl:choose>
  <xsl:when test="$hasStartEndDateTime">
<xsl:text>import </xsl:text><xsl:value-of select="$context-package"/>.TemporalUtils;
</xsl:when>
  <xsl:otherwise />
</xsl:choose>
import <xsl:value-of select="$entity-package"/>.<xsl:value-of select="$entityName" />;
<xsl:choose>
  <xsl:when test="$idKey">import <xsl:value-of select="$entity-package"/>.<xsl:value-of select="$idKey/@entityName" />;
</xsl:when>
</xsl:choose>
<!-- Commented out for now as we don't need
     any imports for these foreign key queries.
     If we later provide foreign key queries
     that have a parameter of the foreign entity
     type, uncomment this. (You might also want
     to use git blame to find other code that
     was removed at the same time that this
     was commented out!
xsl:apply-templates select="$foreignKeyQueries"
                     mode="import-entities" /-->/** <xsl:value-of select="$entityName" /> DAO class. */
public final class <xsl:value-of select="$entityName" />DAO {

    /** Private constructor for a utility class. */
    private <xsl:value-of select="$entityName" />DAO() {
    }

    /** Get <xsl:value-of select="$entityName" /> by id.
     * @param id <xsl:value-of select="$entityName" /> id.
     * @return The <xsl:value-of select="$entityName" />.
     */
    public static <xsl:value-of select="$entityName" />
        get<xsl:value-of select="$entityName" />ById(
        final int id) {
        EntityManager em = DBContext.getEntityManager();
        <xsl:value-of select="$entityName" /> entity = em.find(
            <xsl:value-of select="$entityName" />.class, id);
        em.close();
        return entity;
    }

    /** Get all <xsl:value-of select="$entityName" /> instances.
     * @return An array of all <xsl:value-of select="$entityName" /> instances.
     */
    public static List&lt;<xsl:value-of select="$entityName" />&gt;
        getAll<xsl:value-of select="$entityName" />() {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery&lt;<xsl:value-of select="$entityName" />&gt; q = em.createNamedQuery(
                <xsl:value-of select="$entityName" />.
                    GET_ALL_<xsl:value-of select="upper-case($entityName)" />,
                <xsl:value-of select="$entityName" />.class);
        List&lt;<xsl:value-of select="$entityName" />&gt; entityList = q.getResultList();
        em.close();
        return entityList;
    }

<xsl:choose><xsl:when test="$hasStartEndDateTime">    /** Get all current <xsl:value-of select="$entityName" /> instances.
     * @return An array of all <xsl:value-of select="$entityName" /> instances.
     */
    public static List&lt;<xsl:value-of select="$entityName" />&gt;
        getAllCurrent<xsl:value-of select="$entityName" />() {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery&lt;<xsl:value-of select="$entityName" />&gt; q = em.createNamedQuery(
                <xsl:value-of select="$entityName" />.
                    GET_ALL_CURRENT_<xsl:value-of select="upper-case($entityName)" />,
                <xsl:value-of select="$entityName" />.class);
        q = TemporalUtils.setDatetimeConstantParameters(q);
        List&lt;<xsl:value-of select="$entityName" />&gt; entityList = q.getResultList();
        em.close();
        return entityList;
    }

<xsl:choose><xsl:when test="$idKey">    /** Determine if <xsl:value-of select="$entityName" /> id has a
     *      draft instance.
     * @param id The <xsl:value-of select="$entityName" />Id of the instance
     *     to be checked for the presence of a draft instance.
     * @return True, if there is a draft instance..
     */
    public static boolean
        hasDraft<xsl:value-of select="$entityName" />(
        final Integer id) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery&lt;Boolean&gt; q = em.createNamedQuery(
                <xsl:value-of select="$entityName" />.
                    HAS_DRAFT_<xsl:value-of select="upper-case($entityName)" />,
                Boolean.class).setParameter("id", id);
        q = TemporalUtils.setDatetimeConstantParameters(q);
        Boolean hasDraft = q.getSingleResult();
        em.close();
        return hasDraft;
    }

</xsl:when></xsl:choose>    /** Get all draft <xsl:value-of select="$entityName" /> instances.
     * @return An array of all draft <xsl:value-of select="$entityName" /> instances.
     */
    public static List&lt;<xsl:value-of select="$entityName" />&gt;
        getAllDraft<xsl:value-of select="$entityName" />() {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery&lt;<xsl:value-of select="$entityName" />&gt; q = em.createNamedQuery(
                <xsl:value-of select="$entityName" />.
                    GET_ALL_DRAFT_<xsl:value-of select="upper-case($entityName)" />,
                <xsl:value-of select="$entityName" />.class);
        q = TemporalUtils.setDatetimeConstantParameters(q);
        List&lt;<xsl:value-of select="$entityName" />&gt; entityList = q.getResultList();
        em.close();
        return entityList;
    }

</xsl:when></xsl:choose><xsl:choose><xsl:when test="$idKey">    /** Get current <xsl:value-of select="$entityName" /> instance by id.
     * If there is no such instance, returns null.
     * @param id The <xsl:value-of select="$entityName" />Id of the instance
     *     to be fetched.
     * @return The current <xsl:value-of select="$entityName" /> instance
     *     with that <xsl:value-of select="$entityName" />Id,
     *     or null, if there is no such instance.
     */
    public static <xsl:value-of select="$entityName" />
        getCurrent<xsl:value-of select="$entityName" />By<xsl:value-of select="$entityName" />Id(
        final Integer id) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery&lt;<xsl:value-of select="$entityName" />&gt; q = em.createNamedQuery(
                <xsl:value-of select="$entityName" />.
                    GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_BY_ID,
                <xsl:value-of select="$entityName" />.class)
                .setParameter("id", id);
        q = TemporalUtils.setDatetimeConstantParameters(q);
        List&lt;<xsl:value-of select="$entityName" />&gt; entityList = q.getResultList();
        em.close();
        if (entityList.isEmpty()) {
            return null;
        }
        return entityList.get(0);
    }

</xsl:when></xsl:choose>

<xsl:apply-templates select="$foreignKeyQueries" mode="method">
  <xsl:with-param name="entityName" select="$entityName" />
  <xsl:with-param name="addTemporalVersion" select="$idKey" />
</xsl:apply-templates>
<xsl:apply-templates select="$extraQueries" mode="method">
</xsl:apply-templates>    /** Save a new <xsl:value-of select="$entityName" /> to the database.
     * @param entity The <xsl:value-of select="$entityName" /> to be saved.
     */
    public static void save<xsl:value-of select="$entityName" />(
        final <xsl:value-of select="$entityName" /> entity) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        em.close();
    }

<xsl:choose>
<xsl:when test="$idKey">    /** Save a new <xsl:value-of select="$entityName" /> to the database,
     * creating an id in the related ID table.
     * @param entity The <xsl:value-of select="$entityName" /> to be saved.
     */
    public static void save<xsl:value-of select="$entityName" />WithId(
        final <xsl:value-of select="$entityName" /> entity) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        <xsl:value-of select="$idKey/@entityName" /> idEntity = new <xsl:value-of select="$idKey/@entityName" />();
        em.persist(idEntity);
        entity.set<xsl:call-template name="CamelCase">
          <xsl:with-param name="text" select="$idKey/@keyColumn" />
        </xsl:call-template>(idEntity.getId());
        em.persist(entity);
        em.getTransaction().commit();
        em.close();
    }

</xsl:when>
  <xsl:otherwise />
</xsl:choose>    /** Update an existing <xsl:value-of select="$entityName" /> in the database.
     * @param entity The <xsl:value-of select="$entityName" /> to be updated.
     */
    public static void update<xsl:value-of select="$entityName" />(
        final <xsl:value-of select="$entityName" /> entity) {
        EntityManager em = DBContext.getEntityManager();
        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();
        em.close();
    }

<xsl:text>}
</xsl:text>
</xsl:result-document>
  </xsl:template>


  <!-- Supporting templates. The following few all
       match against element foreignKeyQuery,
       but have different modes.
  -->

  <!-- Generate NamedQuery definition for q query to get a list of
       entities that are looked up by a "foreign key"-type key.
       For insertion into the @NamedQueries section of the
       entity class.
  -->
  <xsl:template match="foreignKeyQuery" mode="annotation">
    <xsl:param name="entityName" />
    <xsl:param name="addTemporalVersion" />
    <xsl:variable name="formalParameterName">
      <xsl:call-template name="FormalParameterNameForEntity">
        <xsl:with-param name="text" select="@entityName" />
      </xsl:call-template>
    </xsl:variable>,
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />,
            query = <xsl:value-of select="$entityName" />.
                GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_QUERY)<xsl:choose>
<!-- Add a temporal version of the query, if this is a temporal table. -->
<xsl:when test="$addTemporalVersion">,
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />,
            query = <xsl:value-of select="$entityName" />.
                GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_QUERY)</xsl:when></xsl:choose></xsl:template>


  <!-- Generate constant definitions for a query to get a list of
       entities that are looked up by a "foreign key"-type key.  For
       insertion into the body of the entity class.
  -->
  <xsl:template match="foreignKeyQuery" mode="constants">
    <xsl:param name="entityName" />
    <xsl:param name="addTemporalVersion" />
    <xsl:variable name="formalParameterName">
      <xsl:call-template name="FormalParameterNameForEntity">
        <xsl:with-param name="text" select="@entityName" />
      </xsl:call-template>
    </xsl:variable>    /** Name of get<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" /> query. */
    public static final String GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" /> =
            "get<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" />";
    /** Name of get<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" /> query's <xsl:value-of select="@keyColumn" /> parameter. */
    public static final String GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_<xsl:value-of select="upper-case(@keyColumn)" /> =
            "<xsl:value-of select="@keyColumn" />";
    /** Query of get<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" /> query. */
    protected static final String GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_QUERY =
            "SELECT entity FROM <xsl:value-of select="$entityName" /> entity WHERE entity.<xsl:value-of select="@keyColumn" /> = :"
            + GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_<xsl:value-of select="upper-case(@keyColumn)" />;

<xsl:choose>
<!-- Add a temporal version of the query, if this is a temporal table. -->
<xsl:when test="$addTemporalVersion">    /** Name of getCurrent<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" /> query. */
    public static final String GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" /> =
            "getCurrent<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" />";
    /** Query of get<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" /> query. */
    protected static final String
        GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_QUERY =
            "SELECT entity FROM <xsl:value-of select="$entityName" /> entity WHERE entity.<xsl:value-of select="@keyColumn" /> = :"
            + GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_<xsl:value-of select="upper-case(@keyColumn)" />
            + TemporalUtils.AND_TEMPORAL_QUERY_VALID_SUFFIX;

</xsl:when>
</xsl:choose>
</xsl:template>

  <!-- Generate a sorted, distinct list of imports of entities
       referred to by "foreign key"-type queries.  For insertion into
       the import section of the DAO class.
  -->
  <xsl:template match="foreignKeyQueries" mode="import-entities">
    <xsl:variable name="allNeededEntities"
                  select="distinct-values(foreignKeyQuery/@entityName)"/>
    <xsl:for-each select="$allNeededEntities">
      <xsl:sort />import <xsl:value-of select="$entity-package" />.<xsl:value-of select="." />;
</xsl:for-each>
</xsl:template>


  <!-- Generate methods to get a list of entities that
       are looked up by a "foreign key"-type key.
       For insertion into the DAO class.
  -->
  <xsl:template match="foreignKeyQuery" mode="method">
    <xsl:param name="entityName" />
    <xsl:param name="addTemporalVersion" />    /** Get all <xsl:value-of select="$entityName" /> instances for a <xsl:value-of select="@entityName" />.
     * @param id The <xsl:value-of select="@entityName" />Id.
     * @return The list of <xsl:value-of select="$entityName" /> instances for this <xsl:value-of select="@entityName" />.
     */
    public static List&lt;<xsl:value-of select="$entityName" />&gt; get<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" />(
            final Integer id) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery&lt;<xsl:value-of select="$entityName" />&gt; q = em.createNamedQuery(
                <xsl:value-of select="$entityName" />.GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />,
                <xsl:value-of select="$entityName" />.class).
                setParameter(
                        <xsl:value-of select="$entityName" />.GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_<xsl:value-of select="upper-case(@keyColumn)" />,
                        id);
        List&lt;<xsl:value-of select="$entityName" />&gt; entityList = q.getResultList();
        em.close();
        return entityList;
    }

<xsl:choose>
<!-- Add a temporal version of the query, if this is a temporal table. -->
<xsl:when test="$addTemporalVersion">    /** Get all current <xsl:value-of select="$entityName" /> instances for a <xsl:value-of select="@entityName" />.
     * @param id The <xsl:value-of select="@entityName" />.
     * @return The list of current <xsl:value-of select="$entityName" /> instances for this <xsl:value-of select="@entityName" />.
     */
    public static List&lt;<xsl:value-of select="$entityName" />&gt; getCurrent<xsl:value-of select="$entityName" />ListFor<xsl:value-of select="@entityName" />(
            final Integer id) {
        EntityManager em = DBContext.getEntityManager();
        TypedQuery&lt;<xsl:value-of select="$entityName" />&gt; q = em.createNamedQuery(
                <xsl:value-of select="$entityName" />.GET_CURRENT_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />,
                <xsl:value-of select="$entityName" />.class).
                setParameter(
                        <xsl:value-of select="$entityName" />.GET_<xsl:value-of select="upper-case($entityName)" />_LIST_FOR_<xsl:value-of select="upper-case(@entityName)" />_<xsl:value-of select="upper-case(@keyColumn)" />,
                        id);
        q = TemporalUtils.setDatetimeConstantParameters(q);
        List&lt;<xsl:value-of select="$entityName" />&gt; entityList = q.getResultList();
        em.close();
        return entityList;
    }

</xsl:when>
</xsl:choose>
</xsl:template>


<!-- Support extra queries. -->

  <!-- Generate NamedQuery definition for q query to get a list of
       entities that are looked up by a "foreign key"-type key.
       For insertion into the @NamedQueries section of the
       entity class.
  -->
  <xsl:template match="extraQuery" mode="annotation">
    <xsl:param name="entityName" />,
    @NamedQuery(
            name = <xsl:value-of select="$entityName" />.
                <xsl:value-of select="@name" />,
            query = <xsl:value-of select="$entityName" />.
                <xsl:value-of select="@name" />_QUERY)</xsl:template>

  <!-- Generate constant definitions for a query to get a list of
       entities that are looked up by a "foreign key"-type key.  For
       insertion into the body of the entity class.
  -->
  <xsl:template match="extraQuery" mode="constants">
    <xsl:variable name="queryNameCamelCase">
      <xsl:call-template name="CamelCaseNotFirst">
        <xsl:with-param name="text" select="lower-case(@name)" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="queryTextNormalized"
                  select="local:normalize-list(tokenize(queryText, '\r?\n'))" />    /** Name of <xsl:value-of select="$queryNameCamelCase" /> query. */
    public static final String <xsl:value-of select="@name" /> =
            "<xsl:value-of select="$queryNameCamelCase" />";
<xsl:apply-templates select="extraQueryParameter" mode="constants">
  <xsl:with-param name="queryName" select="@name" />
  <xsl:with-param name="queryNameCamelCase" select="$queryNameCamelCase" />
</xsl:apply-templates>    /** Query of <xsl:value-of select="$queryNameCamelCase" /> query. */
    protected static final String <xsl:value-of select="@name" />_QUERY =
            <xsl:for-each select="$queryTextNormalized[. != '']">
              <xsl:if test="position() > 1">
            + </xsl:if>"<xsl:if
              test="position() > 1"><xsl:text> </xsl:text></xsl:if><xsl:value-of select="."/>"</xsl:for-each>;

</xsl:template>

  <xsl:template match="extraQueryParameter" mode="constants">
    <xsl:param name="queryName" />
    <xsl:param name="queryNameCamelCase" />    /** Name of <xsl:value-of select="$queryNameCamelCase" /> query's
     * <xsl:value-of select="@name" /> parameter. */
    public static final String
    <xsl:value-of select="$queryName" />_<xsl:value-of select="upper-case(@name)" /> =
            "<xsl:value-of select="@name" />";
</xsl:template>

  <!-- Generate method body for an extra query.
       For insertion into the DAO class.
  -->
  <xsl:template match="extraQuery" mode="method">
    <xsl:value-of select="method" />
  </xsl:template>


<!-- Columns. -->

  <!-- Generate the definition of one database column,
     as a field, and as getter and setter methods.
  -->

  <xsl:template match="dcl:column">
    <xsl:variable name="fieldType">
      <xsl:call-template name="FieldType">
        <xsl:with-param name="dbtype" select="@type" />
        <xsl:with-param name="tableName" select="../lower-case(@tableName)" />
        <xsl:with-param name="fieldName" select="lower-case(@name)" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="isEnumerated">
      <xsl:call-template name="IsEnumerated">
        <xsl:with-param name="tableName" select="../lower-case(@tableName)" />
        <xsl:with-param name="fieldName" select="lower-case(@name)" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="extraAnnotations">
      <xsl:call-template name="ExtraAnnotations">
        <xsl:with-param name="column" select="." />
        <xsl:with-param name="isEnumerated" select="$isEnumerated=true()" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="extraColumnAnnotation">
      <xsl:call-template name="ExtraColumnAnnotation">
        <xsl:with-param name="column" select="." />
        <xsl:with-param name="dbtype" select="@type" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="fieldName">
      <xsl:call-template name="CamelCaseNotFirst">
        <xsl:with-param name="text" select="lower-case(@name)" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="methodName">
      <xsl:call-template name="CamelCase">
        <xsl:with-param name="text" select="lower-case(@name)" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="formalParameterName">
      <xsl:call-template name="FormalParameterName">
        <xsl:with-param name="text" select="lower-case(@name)" />
      </xsl:call-template>
    </xsl:variable>
    <!-- Javadoc for the field. If the column has
         a remarks attribute, include that. -->
    <xsl:text>    /** </xsl:text>
    <xsl:value-of select="$fieldName" />
    <xsl:text>.</xsl:text>
    <xsl:choose>
      <xsl:when test="@remarks">
        <xsl:text>
     * </xsl:text>
        <xsl:value-of select="@remarks" />
        <xsl:text>
     */</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text> */</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    private <xsl:value-of select="$fieldType" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="$fieldName" />
    <xsl:text>;

    /** Get the value of </xsl:text><xsl:value-of select="$fieldName" /><xsl:text>.
     * @return The value of </xsl:text><xsl:value-of select="$fieldName" /><xsl:text>.
     */
</xsl:text>
<xsl:value-of select="$extraAnnotations" />
<xsl:text>    @Column(name = "</xsl:text><xsl:value-of select="lower-case(@name)" />"<xsl:value-of select="$extraColumnAnnotation" />, nullable = false)
    public <xsl:value-of select="$fieldType" /> get<xsl:value-of select="$methodName" />() {
        return <xsl:value-of select="$fieldName" />;
    }

    /** Set the value of <xsl:value-of select="$fieldName" />.
     * @param <xsl:value-of select="$formalParameterName" /> The value of
     *     <xsl:value-of select="$fieldName" /> to set.
     */
    public void set<xsl:value-of select="$methodName" />(
        final <xsl:value-of select="$fieldType" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="$formalParameterName" />) {
        <xsl:value-of select="$fieldName" /> = <xsl:value-of select="$formalParameterName" />
        <xsl:text>;
    }

</xsl:text>
</xsl:template>

  <!-- The following templates based on the accepted answer at
       http://stackoverflow.com/questions/13122545/convert-first-character-of-each-word-to-upper-case
       ... but using XSLT 2.0's lower-case()
       and upper-case() functions.
       -->

  <!-- Apply camelcasing to $text, but not to the very first word.
       vocabulary_id -> vocabularyId. -->
  <xsl:template name="CamelCaseNotFirst">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains($text,'_')">
        <xsl:value-of select="substring-before($text,'_')"/>
        <xsl:call-template name="CamelCase">
          <xsl:with-param name="text" select="substring-after($text,'_')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Apply camelcasing to $text, to every word.
       vocabulary_id -> VocabularyId. -->
  <xsl:template name="CamelCase">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains($text,'_')">
        <xsl:call-template name="CamelCaseWord">
          <xsl:with-param name="text" select="substring-before($text,'_')"/>
        </xsl:call-template>
        <xsl:call-template name="CamelCase">
          <xsl:with-param name="text" select="substring-after($text,'_')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="CamelCaseWord">
          <xsl:with-param name="text" select="$text"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Apply camelcasing to just one word. -->
  <xsl:template name="CamelCaseWord">
    <xsl:param name="text"/>
    <xsl:value-of select="upper-case(substring($text,1,1))" /><xsl:value-of select="lower-case(substring($text,2,string-length($text)-1))" />
  </xsl:template>

  <!-- Apply capitalization to just the beginning of one word. -->
  <xsl:template name="CapitalizeWord">
    <xsl:param name="text"/>
    <xsl:value-of select="upper-case(substring($text,1,1))" /><xsl:value-of select="substring($text,2,string-length($text)-1)" />
  </xsl:template>

  <!-- Generate formal parameter name, by prepending either
       "an" or "a", based on whether or not $text begins with
       a vowel. Parameter $text should have underscores
       to separate words; the result is camel-cased.
       start_date -> aStartDate, id -> anId
       -->
  <xsl:template name="FormalParameterName">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains('aeiouAEIOU',substring($text,1,1))">
        <xsl:text>an</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>a</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="CamelCase">
      <xsl:with-param name="text" select="$text"/>
    </xsl:call-template>
  </xsl:template>

  <!-- Generate formal parameter name, by prepending either
       "an" or "a", based on whether or not $text begins with
       a vowel. Parameter $text should be an entity type.
       Version -> aVersion, AccessPoint -> anAccessPoint
       -->
  <xsl:template name="FormalParameterNameForEntity">
    <xsl:param name="text"/>
    <xsl:choose>
      <xsl:when test="contains('aeiouAEIOU',substring($text,1,1))">
        <xsl:text>an</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>a</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="$text" />
  </xsl:template>

  <!-- Generate the Java type of the field, using
       the $db-to-type mapping. Copes with
       SQL type parameters, i.e., VARCHAR(65).
  -->
  <xsl:template name="FieldType">
    <xsl:param name="dbtype"/>
    <xsl:param name="tableName"/>
    <xsl:param name="fieldName"/>
    <xsl:variable name="isEnumerated">
      <xsl:call-template name="IsEnumerated">
        <xsl:with-param name="tableName" select="../lower-case(@tableName)" />
        <xsl:with-param name="fieldName" select="lower-case(@name)" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <!-- Enumerated type specified in entity-mapping. -->
      <xsl:when test="$isEnumerated=true()">
        <xsl:value-of
            select="key('db-to-entity', $tableName,
                          $db-entity-mapping)/column[@db=$fieldName]/@enum" />
      </xsl:when>
      <!-- Matches VARCHAR(65) -->
      <xsl:when test="contains(upper-case($dbtype),'(')">
        <xsl:variable name="fieldTypePrefix"
                      select="substring-before(upper-case($dbtype),'(')" />
        <xsl:value-of
            select="key('db-to-type', $fieldTypePrefix,
                    $db-type-mapping)/@fieldType" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of
            select="key('db-to-type', upper-case($dbtype),
                    $db-type-mapping)/@fieldType" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Determine if a field is to be of an enumerated type.
  -->
  <xsl:template name="IsEnumerated">
    <xsl:param name="tableName"/>
    <xsl:param name="fieldName"/>
    <xsl:choose>
      <!-- Enumerated type specified in entity-mapping. -->
      <xsl:when test="key('db-to-entity', $tableName,
                      $db-entity-mapping)/column[@db=$fieldName]"
                >true</xsl:when>
      <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Generate any extra annotations needed.
       For primary key columns, add
       a @GeneratedValue annotation.
  -->
  <xsl:template name="ExtraAnnotations">
    <xsl:param name="column"/>
    <xsl:param name="isEnumerated"/>
    <xsl:choose>
      <xsl:when test="$column/dcl:constraints/@primaryKey">
        <xsl:text>    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
</xsl:text>
      </xsl:when>
      <xsl:when test="$isEnumerated=true()">
        <xsl:text>    @Enumerated(EnumType.STRING)
</xsl:text>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Generate any extra content to be added
       to the @Column annotation.
       In practice, that means (a) coping
       with size parameters in VARCHAR(...)
       columns; (b) adding unique = true for
       primary keys.
  -->
  <xsl:template name="ExtraColumnAnnotation">
    <xsl:param name="column"/>
    <xsl:param name="dbtype"/>
    <xsl:choose>
      <!-- If the mapping specifies an extraColumnAnnotation
           attribute for this type. -->
      <xsl:when test="key('db-to-type', $dbtype,
                      $db-type-mapping)/@extraColumnAnnotation">
        <xsl:value-of
            select="key('db-to-type', $dbtype,
                      $db-type-mapping)/@extraColumnAnnotation" />
      </xsl:when>
      <!-- Matches VARCHAR(65) -->
      <xsl:when test="matches($dbtype, '^VARCHAR\(', 'i')">
        <!-- Extract the bit between parentheses -->
        <xsl:variable name="fieldTypeSuffix"
                      select="substring-before(
                              substring-after(
                              $dbtype,'('),
                              ')')" />
        <xsl:text>, length = </xsl:text>
        <xsl:value-of select="$fieldTypeSuffix" />
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$column/dcl:constraints/@primaryKey">
        <xsl:text>, unique = true</xsl:text>
      </xsl:when>
      <xsl:otherwise>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Function to normalize spaces in every element of a sequence. -->

  <xsl:function name="local:normalize-list">
    <xsl:param name="list" />
    <xsl:for-each select="$list">
      <xsl:sequence select="normalize-space(.)" />
    </xsl:for-each>
  </xsl:function>

</xsl:transform>
