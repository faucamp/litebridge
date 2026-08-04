package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectSpec;

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
     * @param node the AST terminating node
     */
    public void put(final QueryNode node, final CachedOperation cachedOperation) {
        cache.put(node.hashCode(), cachedOperation);
    }

    /**
     * Stores an execution plan in the cache.
     *
     * @param nodeHash the AST terminating node hash
     */
    public void put(final int nodeHash, final CachedOperation cachedOperation) {
        cache.put(nodeHash, cachedOperation);
    }

    public record CachedOperation(String sql,
                                  List<Integer> bindValueSqlTypes,
                                  @Nullable TypeConversionMetaData typeConversionMetaData,
                                  @Nullable UpdateMetaData updateMetaData,
                                  @Nullable SelectSpec selectSpec) {

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
