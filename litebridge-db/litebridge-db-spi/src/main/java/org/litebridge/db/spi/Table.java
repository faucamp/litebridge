package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * A database table with an associated catalog, schema, name, and optional alias.
 * <p>
 * This class provides functionality for handling table-related metadata
 * and supports aliasing for the table name.
 */
public class Table extends Aliased {

    /**
     * Database catalog name
     */
    private final String catalog;
    /**
     * Database schema name
     */
    private final String schema;

    public Table(final String catalog, final String schema, final String name) {
        this(catalog, schema, name, null);
    }

    public Table(final String catalog, final String schema, final String name, final @Nullable String alias) {
        super(name, alias);
        this.catalog = catalog;
        this.schema = schema;
    }

    public String catalog() {
        return catalog;
    }

    public String schema() {
        return schema;
    }

    @Override
    public Table as(final String alias) {
        return (Table) super.as(alias);
    }

    public final boolean isTableMetaData() {
        return this instanceof TableMetaData;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Table table1)) return false;
        return Objects.equals(catalog, table1.catalog) && Objects.equals(schema, table1.schema) && Objects.equals(name(), table1.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, name());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Table.class.getSimpleName() + "[", "]")
                .add("catalog='" + catalog + "'")
                .add("schema='" + schema + "'")
                .add("name='" + name() + "'")
                .toString();
    }
}
