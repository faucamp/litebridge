package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class DtoAliasRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtoAliasRegistry.class);

    /**
     * Map of name -> alias base string
     */
    private final Map<String, String> aliasMap = new HashMap<>();
    /**
     * Map of alias base string -> count (number of times used)
     */
    private final Map<String, Integer> aliasCount = new HashMap<>();
    /**
     * Map of table -> alias
     */
    private final Map<Table, String> tableAliases = new HashMap<>();
    /**
     * Map of table alias -> column -> alias
     */
    private final Map<String, Map<Column, String>> columnAliases = new HashMap<>();

    public String alias(final Table table) {
        return tableAliases.computeIfAbsent(table, cls -> createNewAlias(table.name()));
    }

    public @Nullable String aliasOrNull(final Table table) {
        return tableAliases.get(table);
    }

    public String alias(final String tableAlias, final Column column) {
        return columnAliases.computeIfAbsent(tableAlias, k -> new HashMap<>())
                .computeIfAbsent(column, col -> tableAlias + createNewAlias(col.name()));
    }

    public String aliasOrNull(final String tableAlias, final Column column) {
        final String alias = columnAliases.containsKey(tableAlias) ? columnAliases.get(tableAlias).get(column) : null;

        if (alias == null) {
            LOGGER.error("Column alias not found for table alias: {}, column: {}", tableAlias, column.name());
            throw new IllegalStateException("Column alias not found for table: " + tableAlias + ", column: " + column.name());
        }

        return alias;
    }

    public boolean belongsTo(final String tableAlias, final Column column) {
        return columnAliases.containsKey(tableAlias) && columnAliases.get(tableAlias).containsKey(column);
    }

    private String createNewAlias(final String name) {
        final String alias = aliasMap.computeIfAbsent(name, StringUtils::abbreviate);
        final int count = aliasCount.getOrDefault(alias, 0) + 1;
        aliasCount.put(alias, count);
        return alias + count;
    }
}
