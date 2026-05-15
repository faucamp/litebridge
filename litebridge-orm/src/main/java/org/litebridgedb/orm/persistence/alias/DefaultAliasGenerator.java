package org.litebridgedb.orm.persistence.alias;

import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.persistence.OrmTable;

import java.util.HashMap;
import java.util.Map;

public final class DefaultAliasGenerator implements AliasGenerator {

    /**
     * Map of name -> alias base string
     */
    private final Map<String, String> aliasMap = new HashMap<>();
    /**
     * Map of alias base string -> count (number of times used)
     */
    private final Map<String, Integer> aliasCount = new HashMap<>();

    @Override
    public Table aliasTable(final OrmTable ormTable) {
        final TableMetaData tableMetaData = ormTable.getMetaData();
        final String tableAlias = newAlias(tableMetaData.name());
        return new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), tableAlias);
    }

    @Override
    public Column aliasColumn(final Table ormTable, final ColumnMetaData columnMetaData) {
        // Create a new alias
        final String columnAlias = ormTable.alias() + newAlias(columnMetaData.name());
        return new Column(ormTable, columnMetaData.name(), columnAlias);
    }

    private String newAlias(final String name) {
        final String alias = aliasMap.computeIfAbsent(name, StringUtils::abbreviate).toUpperCase();
        final int count = aliasCount.compute(alias, (k, v) -> v == null ? 0 : v + 1);

        if (count >= 1) {
            return alias + count;
        } else {
            return alias;
        }
    }
}
