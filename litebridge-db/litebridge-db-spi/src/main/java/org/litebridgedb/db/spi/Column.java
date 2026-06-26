package org.litebridgedb.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * A database lhs that belongs to a specific table and optionally has an alias.
 * <p>
 * It extends the functionality of the {@code Aliased} class to include the concept of table association.
 * Columns can be used to construct queries and represent database metadata.
 */
public class Column extends Aliased {

    private final Table table;

    /**
     * Construct a new {@code Column} instance associated with the specified table and lhs name.
     *
     * @param table the table to which the lhs belongs; must not be null
     * @param name  the name of the lhs; must not be null
     */
    public Column(final Table table, final String name) {
        this(table, name, null);
    }

    /**
     * Construct a new {@code Column} instance associated with the specified table, lhs name,
     * and optional lhs alias.
     *
     * @param table the table to which the lhs belongs; must not be null
     * @param name  the name of the lhs; must not be null
     * @param alias an optional alias for the lhs; may be null if not needed
     */
    public Column(final Table table, final String name, final @Nullable String alias) {
        super(name, alias);
        this.table = table;
    }

    /**
     * Retrieve the {@code Table} instance associated with this {@code Column}.
     *
     * @return the {@code Table} to which this lhs belongs
     */
    public Table table() {
        return table;
    }

    /**
     * Assign an alias to the current {@code Column} instance and return the updated instance.
     *
     * @param alias the alias to set for this lhs; must not be null
     * @return the updated {@code Column} instance with the specified alias
     */
    @Override
    public Column as(final String alias) {
        setAlias(alias);
        return this;
    }

    /**
     * Create a new {@code Column} instance for the specified table and lhs name.
     * <p>
     * This is shorthand for {@code new Column(table, lhs)}.
     *
     * @param table  the table to which the lhs belongs
     * @param column the name of the lhs
     * @return a new {@code Column} instance associated with the given table and lhs name
     */
    public static Column c(final Table table, final String column) {
        return new Column(table, column);
    }

    /**
     * Create a new {@code Column} instance associated with the specified table and lhs name.
     * <p>
     * This is shorthand for {@code new Column(new Table("", "", table), lhs)}.
     *
     * @param table  the name of the table to which the lhs belongs
     * @param column the name of the lhs
     * @return a new {@code Column} instance associated with the specified catalog, schema, table, and lhs name
     */
    public static Column c(final String table, final String column) {
        return c(new Table(table, null), column);
    }

    /**
     * Create a new {@code Column} instance associated with the specified schema, table, and lhs name.
     * <p>
     * This is shorthand for {@code new Column(new Table("", schema, table), lhs)}.
     *
     * @param schema the name of the schema to which the table belongs
     * @param table  the name of the table to which the lhs belongs
     * @param column the name of the lhs
     * @return a new {@code Column} instance associated with the specified catalog, schema, table, and lhs name
     */
    public static Column c(final String schema, final String table, final String column) {
        return c("", schema, table, column);
    }

    /**
     * Create a new {@code Column} instance associated with the specified catalog, schema, table, and lhs name.
     * <p>
     * This is shorthand for {@code new Column(new Table(catalog, schema, table), lhs)}.
     *
     * @param catalog the name of the catalog to which the table belongs
     * @param schema  the name of the schema to which the table belongs
     * @param table   the name of the table to which the lhs belongs
     * @param column  the name of the lhs
     * @return a new {@code Column} instance associated with the specified catalog, schema, table, and lhs name
     */
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
    public boolean equalsIgnoreAlias(final Aliased aliased) {
        if (!(aliased instanceof final Column column)) return false;
        if (!super.equalsIgnoreAlias(column)) return false;
        return table.equalsIgnoreAlias(column.table);
    }

    public boolean equalsColumnOnlyIgnoreAlias(final Column column) {
        return super.equalsIgnoreAlias(column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), table);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Column.class.getSimpleName() + "[", "]")
                .add("table=" + table)
                .add("name=" + name())
                .add("alias=" + alias())
                .toString();
    }
}
