package org.litebridge.db.oracle.engine;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.engine.ExecutionEngineReturnedKeysNamed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OracleExecutionEngine extends ExecutionEngineReturnedKeysNamed {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleExecutionEngine.class);

    public OracleExecutionEngine(final TypeConverter typeConverter, final AliasTransformer aliasTransformer) {
        super(typeConverter, aliasTransformer, DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);
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
    protected List<Map<ColumnMetaData, Object>> extractGeneratedKeysBatch(final List<ColumnMetaData> generatedPrimaryKeys, final int rows, final PreparedStatement preparedStatement) throws SQLException {
        final List<Map<ColumnMetaData, Object>> generatedKeysList = new ArrayList<>(rows);

        try (final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys()) {
            final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(generatedPrimaryKeys.size());
            int row = 0;

            while (generatedKeysResultSet.next()) {
                // Oracle doesn't support ResultSet.getObject() for batch operations
                for (int i = 0; i < generatedPrimaryKeys.size(); i++) {
                    final ColumnMetaData pkColumn = generatedPrimaryKeys.get(i);
                    final Object generatedId = generatedKeysResultSet.getObject(i + 1);
                    getLogger().debug("Generated ID for row {}, column '{}': {}", row, pkColumn.name(), generatedId);
                    generatedKeys.put(pkColumn, generatedId);
                }

                generatedKeysList.add(generatedKeys);
            }

            return generatedKeysList;
        }
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
