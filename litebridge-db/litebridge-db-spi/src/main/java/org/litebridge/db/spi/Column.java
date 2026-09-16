package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * A database column that belongs to a specific table and optionally has an alias.
 * <p>
 * It extends the functionality of the {@code Aliased} class to include the concept of table association.
 * Columns can be used to construct queries and represent database metadata.
 */
public final class Column {

    private static final Table NO_TABLE = new Table("");

    /**
     * Target name
     */
    private final String name;

    private final Table table;

    /**
     * Construct a new {@code Column} instance associated with the specified table and column name.
     *
     * @param table the table to which the column belongs; must not be null
     * @param name  the name of the column; must not be null
     */
    public Column(final Table table, final String name) {
        this(table, name, null);
    }

    /**
     * Construct a new {@code Column} instance associated with the specified table, column name,
     * and optional column alias.
     *
     * @param table the table to which the column belongs; must not be null
     * @param name  the name of the column; must not be null
     * @param alias an optional alias for the column; may be null if not needed
     */
    public Column(final Table table, final String name, final @Nullable String alias) {
        this.name = name;
        this.table = table;
    }

    /**
     * Construct a new {@code Column} instance without an associated {@link Table} instance.
     *
     * @param name  the name of the column; must not be null
     * @param alias an optional alias for the column; may be null if not needed
     */
    public Column(final String name, final @Nullable String alias) {
        this(NO_TABLE, name);
    }

    /**
     * Retrieve the name of the aliased entity.
     *
     * @return the name of the aliased entity
     */
    public String name() {
        return name;
    }

    public boolean hasTable() {
        return table != NO_TABLE;
    }

    /**
     * Retrieve the {@code Table} instance associated with this {@code Column}.
     *
     * @return the {@code Table} to which this column belongs
     */
    public Table table() {
        return table;
    }

    /**
     * Set the alias for this entity and return the updated instance.
     *
     * @param alias the alias to assign to this entity; must not be null
     * @return the updated instance of {@code Aliased} with the specified alias set
     */
    public Column as(final String alias) {
//        setAlias(alias);
//        return this;
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Returns the qualified name of the column ("tableName.columnName").
     *
     * @return the qualified column name
     */
    public String qualifiedName() {
        if (table != NO_TABLE) {
            return table.name() + "." + name();
        } else {
            return name();
        }
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof final Column column
                && Objects.equals(name, column.name)
                && Objects.equals(table, column.table));
    }

    /**
     * Compares this column with another column, ignoring the alias and the table.
     * Only the column name is compared.
     *
     * @param column the column to compare with
     * @return {@code true} if the column names are equal; {@code false} otherwise
     */
    @Deprecated(forRemoval = true)
    public boolean equalsColumnOnlyIgnoreAlias(final Column column) {
        throw new UnsupportedOperationException("Deprecated");
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), table);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Column.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("table=" + table)
                .toString();
    }
}
