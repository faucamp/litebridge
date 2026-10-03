package org.litebridge.db.spi;

import java.util.StringJoiner;

/**
 * A database column that belongs to a specific table and optionally has an alias.
 * <p>
 * It extends the functionality of the {@code Aliased} class to include the concept of table association.
 * Columns can be used to construct queries and represent database metadata.
 *
 * @param name  Name of the database column.
 * @param table Table to which this column belongs.
 */
public record Column(String name, Table table) {

    private static final Table NO_TABLE = VirtualTable.anonymous();

    /**
     * Construct a new {@code Column} instance associated with the specified table and column name.
     *
     * @param table the table to which the column belongs; must not be null
     * @param name  the name of the column; must not be null
     */
    public Column(final Table table, final String name) {
        this(name, table);
    }

    /**
     * Construct a new {@code Column} instance without an associated {@link Table} instance.
     *
     * @param name the name of the column; must not be null
     */
    public Column(final String name) {
        this(NO_TABLE, name);
    }

    public boolean hasTable() {
        //noinspection ConstantValue
        return table != null && table != NO_TABLE;
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
    public String toString() {
        return new StringJoiner(", ", Column.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("table=" + table)
                .toString();
    }
}
