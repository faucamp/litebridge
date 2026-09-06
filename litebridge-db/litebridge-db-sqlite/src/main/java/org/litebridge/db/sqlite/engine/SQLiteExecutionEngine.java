package org.litebridge.db.sqlite.engine;


import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.engine.ExecutionEngineReturnedKeysAuto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class SQLiteExecutionEngine extends ExecutionEngineReturnedKeysAuto {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteExecutionEngine.class);

    public SQLiteExecutionEngine(final TypeConverter typeConverter, final AliasTransformer aliasTransformer) {
        super(typeConverter, aliasTransformer);
    }

    @Override
    protected Map<ColumnMetaData, Object> extractGeneratedKeys(final List<ColumnMetaData> generatedPrimaryKeys, final PreparedStatement preparedStatement) throws SQLException {
        final Map<ColumnMetaData, Object> generatedKeys = new HashMap<>(generatedPrimaryKeys.size());
        try (final ResultSet generatedKeysResultSet = preparedStatement.getGeneratedKeys()) {
            if (generatedKeysResultSet.next()) {
                // SQLite usually returns one generated key (rowid)
                int generatedKeyIndex = 1;
                for (ColumnMetaData pkColumn : generatedPrimaryKeys) {
                    final Object generatedId = generatedKeysResultSet.getObject(generatedKeyIndex++);
                    getLogger().debug("Generated ID for column '{}': {}", pkColumn.name(), generatedId);
                    generatedKeys.put(pkColumn, generatedId);
                }
            }
        }
        return generatedKeys;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
