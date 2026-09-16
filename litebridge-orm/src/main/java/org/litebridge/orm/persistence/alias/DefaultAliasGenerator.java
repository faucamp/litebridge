package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.AliasTransformer;

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

//    private final AliasTransformer aliasTransformer;
    /**
     * Map of name -> alias base string
     */
    private final Map<String, String> aliasMap = new HashMap<>();
    /**
     * Map of alias base string -> count (number of times used)
     */
    private final Map<String, Integer> aliasCount = new HashMap<>();
    /**
     * Tables that have been aliased for the current operation; the value is the alias.
     */
    private final Map<Table, String> tableAliasMap = new HashMap<>();
    /**
     * Columns that have been aliased for the current operation; the value is the alias.
     */
    private final Map<Column, String> columnAliasMap = new HashMap<>();

    /**
     * Clears internal alias maps and usage counts.
     */
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
//        this.aliasTransformer = aliasTransformer;
    }

    @Override
    public @Nullable Column column(final String alias) {
        return columnAliasMap.entrySet().stream()
                .filter(e -> e.getValue().equals(alias))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public @Nullable String columnAlias(final Column column) {
        return columnAliasMap.get(column);
    }

    @Override
    public @Nullable String tableAlias(final Table table) {
        return tableAliasMap.get(table);
    }

    @Override
    public String newTableAlias(final Table table) {
        return tableAliasMap.computeIfAbsent(table, t -> newAlias(t.name()));
    }

    @Override
    public String newColumnAlias(final Column column) {
        return columnAliasMap.computeIfAbsent(column, c -> newAlias(c.qualifiedName()));
    }

    @Override
    public void setColumnAlias(final Column column, final String alias) {
        columnAliasMap.put(column, alias);
        aliasMap.put(alias, alias);
    }

    @Override
    public String newAlias(final String name) {
        //TODO: cleanup
//        final String alias = Objects.requireNonNull(aliasMap.computeIfAbsent(name, v -> aliasTransformer.transformAlias(StringUtils.abbreviate(v))));
        final String alias = Objects.requireNonNull(aliasMap.computeIfAbsent(name, StringUtils::abbreviate));
        final int count = aliasCount.compute(alias, (k, v) -> v == null ? 0 : v + 1);

        if (count >= 1) {
            return alias + count;
        } else {
            return alias;
        }
    }
}
