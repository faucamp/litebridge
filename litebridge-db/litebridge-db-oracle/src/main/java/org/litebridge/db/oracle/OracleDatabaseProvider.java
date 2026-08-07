package org.litebridge.db.oracle;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.oracle.function.OracleSqlFunctionRegistryFactory;
import org.litebridge.db.oracle.sql.OracleSelectSqlGenerator;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Constructs a new {@code OracleDatabaseProvider} using a default type converter.
     */
    public OracleDatabaseProvider() {
        super(new DefaultTypeConverter());
    }

    @Override
    public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new OracleSequenceColumnValueGenerator(sequence);
    }

    @Override
    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new OracleColumnIdentifierGenerator();
    }

    @Override
    protected SqlFunctionRegistryFactory createSqlFunctionRegistryFactory() {
        return new OracleSqlFunctionRegistryFactory(columnIdentifierGenerator.orThrow(), selectSqlGenerator.orThrow());
    }

    @Override
    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new OracleSelectSqlGenerator(typeConverter, columnIdentifierGenerator.orThrow(), this::ensureTableMetaData);
    }

    @Override
    protected Map<ColumnMetaData, Object> extractGeneratedKeys(final List<ColumnMetaData> generatedPrimaryKeys, final PreparedStatement preparedStatement) throws SQLException {
        final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(generatedPrimaryKeys.size());
        final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys();

        if (generatedKeysResultSet.next()) {
            int generatedKeyIndex = 1;

            for (ColumnMetaData pkColumn : generatedPrimaryKeys) {
                final Object generatedId = generatedKeysResultSet.getObject(generatedKeyIndex++);
                getLogger().debug("Generated ID for column '{}': {}", pkColumn.name(), generatedId);
                generatedKeys.put(pkColumn, generatedId);
            }
        }

        generatedKeysResultSet.close();
        return generatedKeys;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
