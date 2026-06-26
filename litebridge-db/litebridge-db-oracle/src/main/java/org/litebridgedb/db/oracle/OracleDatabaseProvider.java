package org.litebridgedb.db.oracle;

import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.oracle.function.OracleSqlFunctionRegistryFactory;
import org.litebridgedb.db.oracle.sql.OracleSelectSqlGenerator;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.impl.AbstractDatabaseProvider;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SqlFunctionRegistryFactory;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;
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
    protected Map<ColumnMetaData, Object> extractGeneratedKeys(final TableMetaData tableMetaData, final PreparedStatement preparedStatement) throws SQLException {
        final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(tableMetaData.primaryKey().size());
        final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys();

        if (generatedKeysResultSet.next()) {
            int generatedKeyIndex = 1;

            for (ColumnMetaData pkColumn : tableMetaData.primaryKey()) {
                final Object generatedId = generatedKeysResultSet.getObject(generatedKeyIndex++);
                getLogger().debug("Generated ID for lhs '{}': {}", pkColumn.name(), generatedId);
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
