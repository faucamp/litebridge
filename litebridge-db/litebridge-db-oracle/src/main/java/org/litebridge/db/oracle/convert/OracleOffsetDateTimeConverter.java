package org.litebridge.db.oracle.convert;

import oracle.jdbc.OracleTypes;
import oracle.sql.TIMESTAMPTZ;
import org.jspecify.annotations.Nullable;
import org.litebridge.convert.converter.OffsetDateTimeConverter;

import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;

/**
 * A specialised converter for Oracle-specific {@link OffsetDateTime} SQL types.
 * <p>
 * This class extends {@link OffsetDateTimeConverter} to handle Oracle-specific
 * {@code TIMESTAMPTZ} SQL types alongside the standard {@code TIMESTAMP_WITH_TIMEZONE}.
 * It provides compatibility for Oracle's `TIMESTAMPTZ` type and ensures proper
 * conversion to {@link OffsetDateTime}.
 * <p>
 * Key functionality includes:
 * <ul>
 *   <li>Conversion of Oracle-specific {@link TIMESTAMPTZ} objects to {@link OffsetDateTime}.</li>
 *   <li>Fallback handling to {@link java.time.ZonedDateTime} when direct conversion
 * from {@link TIMESTAMPTZ} fails.</li>
 *   <li>Delegation to the parent class {@link OffsetDateTimeConverter} when the input
 * value type is unsupported by this implementation.</li>
 * </ul>
 * <p>
 * The converter supports the following SQL types:
 * <ul>
 *   <li>{@link Types#TIMESTAMP_WITH_TIMEZONE}</li>
 *   <li>Oracle-specific {@link OracleTypes#TIMESTAMPTZ}</li>
 * </ul>
 * <p>
 * Thread safety: The class is immutable and thread-safe.
 */
public final class OracleOffsetDateTimeConverter extends OffsetDateTimeConverter {

    private static final int[] SQL_TYPES = new int[]{Types.TIMESTAMP_WITH_TIMEZONE, OracleTypes.TIMESTAMPTZ};

    @Override
    public @Nullable OffsetDateTime convert(final @Nullable Object value) {
        if (value instanceof TIMESTAMPTZ timestamptz) {
            try {
                return timestamptz.toOffsetDateTime();
            } catch (final SQLException ex) {
                try {
                    return timestamptz.toZonedDateTime().toOffsetDateTime();
                } catch (final SQLException ex2) {
                    throw new IllegalArgumentException("Failed to convert TIMESTAMPZ to OffsetDateTime", ex2);
                }
            }
        } else {
            return super.convert(value);
        }
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
