/** See the file "LICENSE" for the full license governing this code. */
package au.org.ands.vocabs.toolkit.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Log model class.
 */
@Entity
@Table(name = Log.TABLE_NAME)
public class Log {

    /** The name of the underlying database table.
     * Use this in the class's {@code @Table} annotation. */
    public static final String TABLE_NAME = "log";

    /** id. */
    private Integer id;
    /** class. */
    private String classColumn;
    /** type. */
    private String type;
    /** message. */
    private String message;

    /** Get the id.
     * @return The id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    /** Set the id.
     * @param anId the id
     */
    public void setId(final Integer anId) {
        id = anId;
    }

    /** Get the class.
     * @return The class
     */
    @Column(name = "class", length = 45)
    public String getClassColumn() {
        return classColumn;
    }

    /** Set the class.
     * @param aClass the class
     */
    public void setClassColumn(final String aClass) {
        classColumn = aClass;
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

    /** Get the message.
     * @return The message
     */
    @Column(name = "message", length = 65535)
    public String getMessage() {
        return message;
    }

    /** Set the message.
     * @param aMessage the message
     */
    public void setMessage(final String aMessage) {
        message = aMessage;
    }

}
