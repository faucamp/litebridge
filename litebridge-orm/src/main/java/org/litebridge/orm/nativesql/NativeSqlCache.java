package org.litebridge.orm.nativesql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NativeSqlCache {

    private final Map<String, ParsedSql> cache = new ConcurrentHashMap<>();

    public ParsedSql getCachedSql(final String rawSql) {
        return cache.computeIfAbsent(rawSql, SqlParser::parseSql);
    }
}
