package org.litebridge.db.spi.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.query.UpdateMetaData;

import java.util.Collections;
import java.util.List;

/**
 * A prepared SQL statement along with its associated bind values.
 * <p>
 * This record encapsulates the SQL query string and the list of values to be
 * bound to the query parameters.
 * <p>
 * Instances of this record are immutable and can be used to safely pass
 * SQL queries and their bindings within the application.
 *
 * @param sql                    The SQL query string that may contain placeholders for
 *                               parameterized values.
 * @param bindValues             The list of bind values corresponding to the placeholders
 *                               in the SQL query. Each value can be nullable, represented
 *                               by the {@link BindValue} type.
 * @param typeConversionMetaData The metadata for type conversion, which may be null.
 */
public record PreparedSql(String sql,
                          List<@Nullable BindValue> bindValues,
                          @Nullable TypeConversionMetaData typeConversionMetaData,
                          @Nullable UpdateMetaData updateMetaData) {

    /**
     * Constructs a new instance of {@code PreparedSql} with the provided SQL
     * query string and list of bind values.
     *
     * @param sql The SQL query string.
     */
    public PreparedSql(String sql,
                       List<@Nullable BindValue> bindValues) {
        this(sql, bindValues, null, null);
    }

    /**
     * Constructs a new instance of {@code PreparedSql} with the provided SQL
     * query string and an empty list of bind values.
     *
     * @param sql The SQL query string.
     */
    public PreparedSql(final String sql) {
        this(sql, Collections.emptyList(), null, null);
    }
}