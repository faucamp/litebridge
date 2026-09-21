package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.compiler.ContextStack;

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

    /**
     * Map of name -> alias base string
     */
    private final Map<String, String> aliasMap = new HashMap<>();

    /**
     * Map of alias base string -> count (number of times used)
     */
    private final Map<String, Integer> aliasCount = new HashMap<>();

    /**
     * Scope stack (used for subqueries)
     */
    private final ScopeContextStack scope = new ScopeContextStack();

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
        return scope.current().columnAliasMap.entrySet().stream()
                .filter(e -> e.getValue().equals(alias))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public @Nullable String columnAlias(final Column column) {
        return scope.current().columnAliasMap.get(column);
    }

    @Override
    public @Nullable String tableAlias(final Table table) {
        return scope.current().tableAliasMap.get(table);
    }

    @Override
    public String newTableAlias(final Table table) {
        return scope.current().tableAliasMap.computeIfAbsent(table, t -> newAlias(t.name()));
    }

    @Override
    public String newColumnAlias(final Column column) {
        return scope.current().columnAliasMap.computeIfAbsent(column, c -> newAlias(c.qualifiedName()));
    }

    @Override
    public void setColumnAlias(final Column column, final String alias) {
        scope.current().columnAliasMap.put(column, alias);
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

    @Override
    public void pushScope() {
        scope.push(LogicOperator.NOOP);
    }

    @Override
    public void popScope() {
        scope.pop();
    }

    /**
     * @param tableAliasMap  Tables that have been aliased for the current operation; the value is the alias.
     * @param columnAliasMap Columns that have been aliased for the current operation; the value is the alias.
     */
    private record Scope(Map<Table, String> tableAliasMap, Map<Column, String> columnAliasMap) {

        public Scope() {
            this(new HashMap<>(), new HashMap<>());
        }
    }

    private class ScopeContextStack extends ContextStack<Scope> {

        @Override
        protected Scope newRootInstance() {
            return new Scope();
        }

        @Override
        protected Scope newSubInstance(final LogicOperator logicOperator) {
            return new Scope();
        }
    }
}
