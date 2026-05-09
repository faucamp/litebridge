package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;

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

    public Table(final String name, final @Nullable String alias) {
        this(StringUtils.splitArray(name, '.', 3, true), alias);
    }

    private Table(final String[] catalogSchemaTable, final @Nullable String alias) {
        this(catalogSchemaTable[0], catalogSchemaTable[1], catalogSchemaTable[2], alias);
    }

    @SuppressWarnings("IncompleteCopyConstructor")
    public Table(final Table other) {
        this(other.catalog(), other.schema(), other.name(), other.alias());
    }

    public String catalog() {
        return catalog;
    }

    public String schema() {
        return schema;
    }

    public String qualifiedName() {
        return schema + "." + name();
    }

    @Override
    public Table as(final String alias) {
        return (Table) super.as(alias);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Table table)) return false;
        return equalsIgnoreAlias(table) && Objects.equals(alias(), table.alias());
    }

    @Override
    public boolean equalsIgnoreAlias(final Aliased o) {
        if (!(o instanceof final Table table)) return false;
        return Objects.equals(catalog, table.catalog) && Objects.equals(schema, table.schema) && Objects.equals(name(), table.name());
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
                .add("alias='" + alias() + "'")
                .toString();
    }
}
