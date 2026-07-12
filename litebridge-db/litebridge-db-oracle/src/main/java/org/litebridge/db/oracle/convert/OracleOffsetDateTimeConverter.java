package org.litebridge.db.oracle.convert;

import oracle.jdbc.OracleTypes;
import oracle.sql.TIMESTAMPTZ;
import org.jspecify.annotations.Nullable;
import org.litebridge.convert.converter.OffsetDateTimeConverter;

import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;

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
