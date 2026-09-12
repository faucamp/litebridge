package org.litebridge.db.oracle.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.engine.ExecutionEngineReturnedKeysNamed;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OracleExecutionEngine extends ExecutionEngineReturnedKeysNamed {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleExecutionEngine.class);
    private static final Map<String, int[]> PARAMETER_PERMUTATIONS = new ConcurrentHashMap<>();

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

    /**
     * Registers a parameter permutation array for the given SQL string.
     *
     * @param sql         the SQL query string
     * @param permutation the parameter permutation array
     */
    public static void registerParameterPermutation(final String sql, final int[] permutation) {
        PARAMETER_PERMUTATIONS.put(sql, permutation);
    }

    /**
     * Returns the parameter permutation array for the given SQL string, or {@code null} if none exists.
     *
     * @param sql the SQL query string
     * @return the parameter permutation array, or {@code null}
     */
    public static int @Nullable [] getParameterPermutation(final String sql) {
        return PARAMETER_PERMUTATIONS.get(sql);
    }

    /**
     * Clears all registered parameter permutations.
     */
    public static void clearParameterPermutations() {
        PARAMETER_PERMUTATIONS.clear();
    }

    @Override
    public UpdateResult executeUpdate(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
        // Reorder bind parameters for MERGE operations (Oracle syntax bind value ordering differs from the Litebridge SPI model)
        return super.executeUpdate(reorderBindValuesIfNecessary(preparedSql), connectionProvider);
    }

    PreparedSql reorderBindValuesIfNecessary(final PreparedSql preparedSql) {
        final int[] permutation = PARAMETER_PERMUTATIONS.get(preparedSql.sql());

        if (permutation == null) {
            return preparedSql;
        }

        final List<BindValue> original = preparedSql.bindValues();

        if (original.size() == permutation.length) {
            final List<BindValue> reordered = new ArrayList<>(permutation.length);

            for (final int index : permutation) {
                reordered.add(original.get(index));
            }

            return new PreparedSql(
                    preparedSql.sql(),
                    reordered,
                    preparedSql.typeConversionMetaData(),
                    preparedSql.updateMetaData()
            );
        }

        return preparedSql;
    }
}
