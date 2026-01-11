package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class Column extends Aliased {

    private final Table table;

    public Column(final Table table, final String name) {
        this(table, name, null);
    }

    public Column(final Table table, final String name, final @Nullable String alias) {
        super(name, alias);
        this.table = table;
    }

    public Table table() {
        return table;
    }

    @Override
    public Column as(final String alias) {
        setAlias(alias);
        return this;
    }

    public static Column c(final Table table, final String column) {
        return new Column(table, column);
    }

    public static Column c(final String table, final String column) {
        return c("", "", table, column);
    }

    public static Column c(final String schema, final String table, final String column) {
        return c("", schema, table, column);
    }

    public static Column c(final String catalog, final String schema, final String table, final String column) {
        return new Column(new Table(catalog, schema, table), column);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Column column)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(table, column.table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), table);
    }
}
