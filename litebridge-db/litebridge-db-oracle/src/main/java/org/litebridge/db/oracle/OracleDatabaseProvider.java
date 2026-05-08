package org.litebridge.db.oracle;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Oracle Database Provider for Litebridge.
 * <p>
 * {@code OracleDatabaseProvider} is a concrete implementation of {@link AbstractDatabaseProvider}
 * designed to facilitate interactions with an Oracle database.
 * <p>
 * It uses a {@link DefaultTypeConverter} for handling type conversions between
 * database values and Java data types.
 */
public class OracleDatabaseProvider extends AbstractDatabaseProvider {

    private static Logger LOGGER = LoggerFactory.getLogger(OracleDatabaseProvider.class);

    public OracleDatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    protected String createColumnIdentifier(final Column column, final boolean includeColumnAlias, final @Nullable Select select) {
        // If a JOIN USING is used in the select from/where/using clause, Oracle doesn't allow table qualifiers for the column
        if (select == null || CollectionUtils.isEmpty(select.joins())) {
            return super.createColumnIdentifier(column, includeColumnAlias, select);
        }

        boolean applyTableQualifier = true;

        for (Join join : select.joins()) {
            for (Condition condition : join.conditions()) {
                if (condition.operator() == Operator.USING && condition.column().equalsIgnoreAlias(column)) {
                    // Don't include table qualifiers
                    applyTableQualifier = false;
                    break;
                }
            }

            if (!applyTableQualifier) {
                break;
            }
        }

        if (applyTableQualifier) {
            return super.createColumnIdentifier(column, includeColumnAlias, select);
        }

        final StringBuilder columnSql = new StringBuilder(quoteIdentifier(column.name()));
        
        if (includeColumnAlias && !StringUtils.isBlank(column.alias())) {
            columnSql.append(' ').append(createAlias(quoteIdentifier(column.alias())));
        }

        return columnSql.toString();
    }

    @Override
    protected String createSequenceNextValueForDirectInsert(final String sequence) {
        return "%s.NEXTVAL".formatted(sequence);
    }

    @Override
    protected String createAlias(final String alias) {
        return alias;
    }

    @Override
    protected void appendLimitClause(final Limit limit, final StringBuilder sql) {
        limit.offset().ifPresent(offset -> sql.append(" OFFSET ").append(offset).append(" ROWS"));
        limit.limit().ifPresent(limitVal -> sql.append(" FETCH FIRST ").append(limitVal).append(" ROWS ONLY"));
    }

    @Override
    protected Map<ColumnMetaData, Object> extractGeneratedKeys(final TableMetaData tableMetaData, final PreparedStatement preparedStatement) throws SQLException {
        final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(tableMetaData.primaryKey().size());
        final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys();

        if (generatedKeysResultSet.next()) {
            int generatedKeyIndex = 1;

            for (ColumnMetaData pkColumn : tableMetaData.primaryKey()) {
                final Object generatedId = generatedKeysResultSet.getObject(generatedKeyIndex++);
                getLogger().debug("Generated ID for column '{}': {}", pkColumn.name(), generatedId);
                generatedKeys.put(pkColumn, generatedId);
            }
        }

        generatedKeysResultSet.close();
        return generatedKeys;
    }

    @Override
    protected @Nullable String transformAlias(final @Nullable String dbAlias) {
        if (dbAlias == null) {
            return null;
        } else {
            return dbAlias.toLowerCase();
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
