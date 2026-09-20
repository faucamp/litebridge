package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.query.SelectTarget;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * A database table with an associated catalog, schema, name, and optional alias.
 * <p>
 * This class provides functionality for handling table-related metadata
 * and supports aliasing for the table name.
 */
public sealed class Table implements SelectTarget permits VirtualTable {

    /**
     * Database catalog name
     */
    private final @Nullable String catalog;
    /**
     * Database schema name
     */
    private final @Nullable String schema;
    /**
     * Database table name
     */
    private final String name;

    /**
     * Constructs a new {@code Table} with catalog, schema, and name.
     *
     * @param catalog the catalog name
     * @param schema  the schema name
     * @param name    the table name
     */
    public Table(final @Nullable String catalog, final @Nullable String schema, final String name) {
        this.name = name;

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
     * Constructs a new {@code Table} with the specified fully qualified name.
     *
     * @param name the fully qualified table name, e.g. {@code "schema.table"} or {@code "catalog.schema.table"}
     */
    public Table(final String name) {
        this(StringUtils.splitArray(name, '.', 3, true));
    }

    private Table(final String[] catalogSchemaTable) {
        this(catalogSchemaTable[0], catalogSchemaTable[1], catalogSchemaTable[2]);
    }

    /**
     * Constructs a new {@code Table} as a copy of another table.
     *
     * @param other the table to copy
     */
    @SuppressWarnings("IncompleteCopyConstructor")
    public Table(final Table other) {
        this(other.catalog(), other.schema(), other.name());
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
        if (schema != null) {
            return schema + "." + name();
        } else {
            return name();
        }
    }

    /**
     * Set the alias for this entity and return the updated instance.
     *
     * @param alias the alias to assign to this entity; must not be null
     * @return the updated instance of {@code Aliased} with the specified alias set
     */
    @Deprecated(forRemoval = true)
    public Table as(final String alias) {
//        setAlias(alias);
//        return (Table) this;
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof final Table that
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.schema, that.schema)
                && Objects.equals(this.catalog, that.catalog));
    }

    @Deprecated(forRemoval = true)
    public boolean equalsIgnoreAlias(final Object o) {
//        if (!(o instanceof final Table table)) return false;
//        return Objects.equals(catalog, table.catalog) && Objects.equals(schema, table.schema) && Objects.equals(name(), table.name());
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Table.class.getSimpleName() + "[", "]")
                .add("catalog='" + catalog + "'")
                .add("schema='" + schema + "'")
                .add("name='" + name + "'")
                .toString();
    }

    /**
     * Retrieve the name of the aliased entity.
     *
     * @return the name of the aliased entity
     */
    public String name() {
        return name;
    }

    public boolean isVirtual() {
        return false;
    }
}
