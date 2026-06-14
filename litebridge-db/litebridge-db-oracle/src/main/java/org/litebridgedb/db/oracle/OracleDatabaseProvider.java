package org.litebridgedb.db.oracle;

import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.impl.AbstractDatabaseProvider;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Limit;
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
public final class OracleDatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleDatabaseProvider.class);

    public OracleDatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new OracleSequenceColumnValueGenerator(sequence);
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
    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new OracleColumnIdentifierGenerator();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
