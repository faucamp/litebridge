package org.litebridge.orm.persistence.alias;

import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.orm.persistence.OrmTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DefaultAliasGenerator implements AliasGenerator {

    private final AliasTransformer aliasTransformer;
    /**
     * Map of name -> alias base string
     */
    private final Map<String, String> aliasMap = new HashMap<>();
    /**
     * Map of alias base string -> count (number of times used)
     */
    private final Map<String, Integer> aliasCount = new HashMap<>();

    public DefaultAliasGenerator(final AliasTransformer aliasTransformer) {
        this.aliasTransformer = aliasTransformer;
    }

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
        final String alias = Objects.requireNonNull(aliasMap.computeIfAbsent(name, v -> aliasTransformer.transformAlias(StringUtils.abbreviate(v))));
        final int count = aliasCount.compute(alias, (k, v) -> v == null ? 0 : v + 1);

        if (count >= 1) {
            return alias + count;
        } else {
            return alias;
        }
    }
}
