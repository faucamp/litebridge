package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * Map of table -> aliases
     */
    private final Map<Table, List<String>> tableAliases = new HashMap<>();
    /**
     * Map of table alias -> column -> alias
     */
    private final Map<String, Map<Column, String>> columnAliases = new HashMap<>();

    public String newAlias(final Table table) {
        final String tableAlias = newAlias(table.name());
        tableAliases.computeIfAbsent(table, cls -> new ArrayList<>())
                .add(tableAlias);
        return tableAlias;
    }

    public String alias(final String tableAlias, final Column column) {
        return columnAliases.computeIfAbsent(tableAlias, k -> new HashMap<>())
                .computeIfAbsent(column, col -> tableAlias + newAlias(col.name()));
    }

    public @Nullable String aliasOrNull(final Table table, final int index) {
        final List<String> aliases = tableAliases.get(table);

        if (aliases == null || aliases.size() <= index) {
            return null;
        }

        return aliases.get(index);
    }

    public @Nullable String aliasOrNull(final String tableAlias, final Column column) {
        return columnAliases.containsKey(tableAlias) ? columnAliases.get(tableAlias).get(column) : null;
    }

    public boolean belongsTo(final String tableAlias, final Column column) {
        if (columnAliases.containsKey(tableAlias)) {
            final String columnAlias = columnAliases.get(tableAlias).get(column);

            if (columnAlias != null) {
                return column.alias() == null || columnAlias.equalsIgnoreCase(column.alias());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private String newAlias(final String name) {
        final String alias = aliasMap.computeIfAbsent(name, StringUtils::abbreviate);
        final int count = aliasCount.getOrDefault(alias, 0) + 1;
        aliasCount.put(alias, count);
        return alias + count;
    }
}
