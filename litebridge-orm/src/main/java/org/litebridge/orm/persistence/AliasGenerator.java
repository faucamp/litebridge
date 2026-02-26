package org.litebridge.orm.persistence;

import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;

import java.util.HashMap;
import java.util.Map;

public final class AliasGenerator {

    /**
     * Map of name -> alias base string
     */
    private final Map<String, String> aliasMap = new HashMap<>();
    /**
     * Map of alias base string -> count (number of times used)
     */
    private final Map<String, Integer> aliasCount = new HashMap<>();

    public Table aliasTable(final OrmTable table) {
        final TableMetaData tableMetaData = table.getMetaData();
        final String tableAlias = newAlias(tableMetaData.name());
        return new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), tableAlias);
    }

    public Column aliasColumn(final Table table, final Column column) {
        if (column.alias() != null) {
            return column;
        }

        // Create a new alias
        final String columnAlias = table.alias() + newAlias(column.name());
        return new Column(table, column.name(), columnAlias);
    }

    private String newAlias(final String name) {
        final String alias = aliasMap.computeIfAbsent(name, StringUtils::abbreviate);
        final int count = aliasCount.compute(alias, (k, v) -> v == null ? 0 : v + 1);

        if (count >= 1) {
            return alias + count;
        } else {
            return alias;
        }
    }
}
