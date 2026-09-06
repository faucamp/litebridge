package org.litebridge.orm.nativesql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a parsed SQL statement with positional parameters and their corresponding names.
 * <p>
 * This class encapsulates a SQL statement where named parameters are replaced with
 * positional placeholders (e.g., "?") and provides a list of the original parameter names.
 * It is used for transforming SQL statements with named parameters into a format
 * that can be executed using standard JDBC APIs.
 *
 * @param sql            the SQL statement with named parameters replaced by positional placeholders
 * @param bindValueCount the number of bind values in the statement
 * @param bindValueNames a list of the original names of the bind parameters in the order
 *                       they appear in the statement
 */
public record ParsedSql(String sql, int bindValueCount, List<String> bindValueNames) {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsedSql.class);

    public PreparedSql prepareSql(final List<@Nullable Object> rawBindValues) {
        validateBindValueCount(rawBindValues.size());

        final List<BindValue> bindValues = rawBindValues.stream()
                .map(BindValue::new)
                .toList();

        return createPreparedSql(bindValues);
    }

    public PreparedSql prepareSql(final @Nullable Object[] rawBindValues) {
        validateBindValueCount(rawBindValues.length);
        final List<BindValue> bindValues = new ArrayList<>(rawBindValues.length);

        for (final Object rawBindValue : rawBindValues) {
            bindValues.add(new BindValue(rawBindValue));
        }

        return createPreparedSql(bindValues);
    }

    public PreparedSql prepareSql(final Map<String, @Nullable Object> bindParameters) {
        validateBindValueCount(bindParameters.size());

        if (bindValueNames.isEmpty()) {
            throw new IllegalArgumentException("No named bind parameters found in parsed SQL");
        }

        final List<BindValue> bindValues = bindValueNames.stream()
                .map(bindValueName -> new BindValue(bindParameters.get(bindValueName), 0))
                .toList();

        return createPreparedSql(bindValues);
    }

    private PreparedSql createPreparedSql(final List<BindValue> bindValues) {
        return new PreparedSql(sql, bindValues, null, null);
    }

    private void validateBindValueCount(final int receivedCount) {
        if (receivedCount != bindValueCount) {
            final String message = "Number of input bind values does not match number of parsed bind parameters; expected " + bindValueNames.size() + ", got " + receivedCount;
            LOGGER.error("{} for SQL: {}", message, sql);
            throw new IllegalArgumentException("Number of input bind values does not match number of parsed bind parameters; expected " + bindValueNames.size() + ", got " + receivedCount);
        }
    }
}
