/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import au.org.ands.vocabs.toolkit.db.DBContext;

/**
 * Related model class.
 */
@PersistenceUnit(unitName = DBContext.UNIT_NAME)
@Entity
@Table(name = "related")
public class Related {

    /** id. */
    private Integer id;
    /** type. */
    private String type;
    /** relation. */
    private String relation;
    /** title. */
    private String title;
    /** slug. */
    private String slug;
    /** data. */
    private String data;

    /** Get the id.
     * @return The id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return id;
    }

    /** Set the id.
     * @param anId the id
     */
    public void setId(final Integer anId) {
        id = anId;
    }

    /** Get the type.
     * @return The type
     */
    @Column(name = "type", length = 45)
    public String getType() {
        return type;
    }

    /** Set the type.
     * @param aType the type
     */
    public void setType(final String aType) {
        type = aType;
    }

    /** Get the relation.
     * @return The relation
     */
    @Column(name = "relation", length = 45)
    public String getRelation() {
        return relation;
    }

    /** Set the relation.
     * @param aRelation the relation
     */
    public void setRelation(final String aRelation) {
        relation = aRelation;
    }

    /** Get the title.
     * @return The title
     */
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    /** Set the title.
     * @param aTitle the title
     */
    public void setTitle(final String aTitle) {
        title = aTitle;
    }

    /** Get the slug.
     * @return The slug
     */
    @Column(name = "slug")
    public String getSlug() {
        return this.slug;
    }

    /** Set the slug.
     * @param aSlug the slug
     */
    public void setSlug(final String aSlug) {
        slug = aSlug;
    }

    /** Get the data.
     * @return The data
     */
    @Column(name = "data", length = 65535)
    public String getData() {
        return data;
    }

    /** Set the data.
     * @param aData the data
     */
    public void setData(final String aData) {
        data = aData;
    }

}
