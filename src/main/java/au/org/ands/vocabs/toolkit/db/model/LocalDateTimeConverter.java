/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.db.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** Converter between {@link java.sql.Timestamp}
 *  and {@link java.time.LocalDateTime} values that are in UTC.
 *  Automatically applied by JPA.
 *  NB: this currently requires Hibernate, and some Hibernate-specific
 *  configuration:
 *  {@code hibernate.jdbc.time_zone=UTC}
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
        return Timestamp.from(attribute.toInstant(ZoneOffset.UTC));
    }

    @Override
    public LocalDateTime convertToEntityAttribute(
            final Timestamp dbData) {
        if (dbData == null) {
            return null;
        }
        return LocalDateTime.ofInstant(dbData.toInstant(), ZoneOffset.UTC);
    }
}
