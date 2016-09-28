/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.db.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** Converter between {@link java.sql.Timestamp}
 *  and {@link java.time.LocalDateTime} values.
 *  Automatically applied by JPA.
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements
    AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(
            final LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return Timestamp.valueOf(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(
            final Timestamp dbData) {
        if (dbData == null) {
            return null;
        }
        return dbData.toLocalDateTime();
    }
}
