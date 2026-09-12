package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches SQL execution plans based on the structural fingerprint of a {@link Select} operation.
 */
public final class QueryPlanCache {

    private final Map<Integer, CachedOperation> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached execution plan for the given select operation.
     *
     * @param node the AST terminating node
     * @return the cached prepared SQL, or {@code null} if not found
     */
    public @Nullable CachedOperation get(final QueryNode node) {
        return cache.get(node.hashCode());
    }

    /**
     * Retrieves a cached execution plan for the given select operation.
     *
     * @param nodeHash the AST terminating node hash
     * @return the cached prepared SQL, or {@code null} if not found
     */
    public @Nullable CachedOperation get(final int nodeHash) {
        return cache.get(nodeHash);
    }

    /**
     * Stores an execution plan in the cache.
     *
     * @param node            the AST terminating node
     * @param cachedOperation the execution plan to cache
     */
    public void put(final QueryNode node, final CachedOperation cachedOperation) {
        cache.put(node.hashCode(), cachedOperation);
    }

    /**
     * Stores an execution plan in the cache.
     *
     * @param nodeHash        the AST terminating node hash
     * @param cachedOperation the execution plan to cache
     */
    public void put(final int nodeHash, final CachedOperation cachedOperation) {
        cache.put(nodeHash, cachedOperation);
    }

    /**
     * Returns the current cache size.
     *
     * @return the current cache size.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Cached operation representing compiled SQL, bind value types, and conversion metadata.
     *
     * @param sql                    the generated SQL string
     * @param bindValueSqlTypes      the SQL types for bind values
     * @param typeConversionMetaData type conversion metadata, or {@code null}
     * @param updateMetaData         update metadata, or {@code null}
     */
    public record CachedOperation(String sql,
                                  List<Integer> bindValueSqlTypes,
                                  @Nullable TypeConversionMetaData typeConversionMetaData,
                                  @Nullable UpdateMetaData updateMetaData) {

        /**
         * Creates a {@link PreparedSql} instance populated with the supplied runtime bind values.
         *
         * @param rawBindValues the runtime bind values matching the required SQL types
         * @return the prepared SQL object ready for execution
         */
        public PreparedSql preparedSql(final List<@Nullable Object> rawBindValues) {
            if (rawBindValues.size() != bindValueSqlTypes.size()) {
                throw new IllegalArgumentException("Number of bind values does not match number of bind value SQL types; expected " + bindValueSqlTypes().size() + ", got " + rawBindValues.size());
            }

            final List<BindValue> bindValues = new ArrayList<>(bindValueSqlTypes.size());

            for (int i = 0; i < bindValueSqlTypes.size(); i++) {
                bindValues.add(new BindValue(rawBindValues.get(i), bindValueSqlTypes.get(i)));
            }

            return new PreparedSql(sql, bindValues, typeConversionMetaData, updateMetaData);
        }
    }
}
