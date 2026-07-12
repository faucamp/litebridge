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
    private final @Nullable String catalog;
    /**
     * Database schema name
     */
    private final @Nullable String schema;

    /**
     * Constructs a new {@code Table} with catalog, schema, and name.
     *
     * @param catalog the catalog name
     * @param schema  the schema name
     * @param name    the table name
     */
    public Table(final @Nullable String catalog, final @Nullable String schema, final String name) {
        this(catalog, schema, name, null);
    }

    /**
     * Constructs a new {@code Table} with catalog, schema, name, and alias.
     *
     * @param catalog the catalog name
     * @param schema  the schema name
     * @param name    the table name
     * @param alias   the table alias
     */
    public Table(final @Nullable String catalog, final @Nullable String schema, final String name, final @Nullable String alias) {
        super(name, alias);

        if (!StringUtils.isBlank(catalog)) {
            this.catalog = catalog;
        } else {
            this.catalog = null;
        }

        if (!StringUtils.isBlank(schema)) {
            this.schema = schema;
        } else {
            this.schema = null;
        }
    }

    /**
     * Constructs a new {@code Table} with name and alias.
     *
     * @param name  the table name
     * @param alias the table alias
     */
    public Table(final String name, final @Nullable String alias) {
        this(StringUtils.splitArray(name, '.', 3, true), alias);
    }

    /**
     * Constructs a new {@code Table} with a name.
     *
     * @param name the table name
     */
    public Table(final String name) {
        this(name, null);
    }

    private Table(final String[] catalogSchemaTable, final @Nullable String alias) {
        this(catalogSchemaTable[0], catalogSchemaTable[1], catalogSchemaTable[2], alias);
    }

    /**
     * Constructs a new {@code Table} as a copy of another table.
     *
     * @param other the table to copy
     */
    @SuppressWarnings("IncompleteCopyConstructor")
    public Table(final Table other) {
        this(other.catalog(), other.schema(), other.name(), other.alias());
    }

    /**
     * Returns the catalog name of the table.
     *
     * @return the catalog name, or {@code null} if not specified
     */
    public @Nullable String catalog() {
        return catalog;
    }

    /**
     * Returns the schema name of the table.
     *
     * @return the schema name, or {@code null} if not specified
     */
    public @Nullable String schema() {
        return schema;
    }

    /**
     * Returns the qualified name of the table (schema.name).
     *
     * @return the qualified table name
     */
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
