package org.litebridge.orm.persistence.alias;

import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * DefaultAliasGenerator is an implementation of {@link AliasGenerator} used for generating unique aliases
 * for tables and columns in a database schema. It maintains internal mappings to ensure alias uniqueness
 * across multiple calls.
 * <p>
 * This class uses an {@link AliasTransformer} to apply specific transformation rules to the base alias strings.
 * The aliases are generated using a combination of base string transformations and integer suffixes for conflicts.
 * <p>
 * Thread-safety: This class is not thread-safe and should be used in single-threaded contexts unless externally synchronized.
 */
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


    public void clear() {
        aliasMap.clear();
        aliasCount.clear();
    }
    /**
     * Constructs a {@code DefaultAliasGenerator} with the specified {@link AliasTransformer}.
     *
     * @param aliasTransformer the transformer to use for creating base aliases
     */
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
    public Table aliasTable(final Table table) {
        if (table.alias() != null) {
            return table;
        }

        final String tableAlias = newAlias(table.name());
        return table.as(tableAlias);
    }

    @Override
    public Column aliasColumn(final Table ormTable, final ColumnMetaData columnMetaData) {
        // Create a new alias
        final String columnAlias = ormTable.alias() + newAlias(columnMetaData.name());
        return new Column(ormTable, columnMetaData.name(), columnAlias);
    }

    @Override
    public Column aliasColumn(final Table aliasedTable, final Column column) {
        if (column.alias() != null) {
            return column;
        }

        // Create a new alias
        final String columnAlias = aliasedTable.alias() + newAlias(column.name());
        column.setAlias(columnAlias);
        column.setTable(aliasedTable);
        return column;
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
