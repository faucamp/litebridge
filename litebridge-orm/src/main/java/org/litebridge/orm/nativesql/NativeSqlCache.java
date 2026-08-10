package org.litebridge.orm.nativesql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for parsed named parameter-based queries.
 */
public final class NativeSqlCache {

    private final Map<String, ParsedSql> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached parsed SQL statement for the given raw SQL string.
     * <p>
     * If the SQL statement is not found in the cache, it is parsed and added to the cache.
     *
     * @param rawSql The raw SQL string to retrieve or parse.
     * @return The parsed SQL statement.
     */
    public ParsedSql getCachedSql(final String rawSql) {
        return cache.computeIfAbsent(rawSql, SqlParser::parseSql);
    }
}
