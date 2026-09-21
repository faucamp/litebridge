package org.litebridge.orm.persistence.alias;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;

/**
 * An {@link AliasGenerator} implementation that does not generate any aliases,
 * returning the original table and column names instead.
 */
public final class NoOpAliasGenerator implements AliasGenerator {

    @Override
    public @Nullable Column column(final String alias) {
        return null;
    }

    @Override
    public @Nullable String columnAlias(final Column column) {
        return null;
    }

    @Override
    public @Nullable String tableAlias(final Table table) {
        return null;
    }

    @Override
    public String newTableAlias(final Table table) {
        return table.name();
    }

    @Override
    public String newColumnAlias(final Column column) {
        return column.name();
    }

    @Override
    public void setColumnAlias(final Column column, final String alias) {
        /* Ignored */
    }

    @Override
    public String newAlias(final String name) {
        return name;
    }

    @Override
    public void pushScope() {
        /* Ignored */
    }

    @Override
    public void popScope() {
        /* Ignored */
    }
}
