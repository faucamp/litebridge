package org.litebridgedb.orm.api.spec;

public sealed interface ColumnMapping permits ColumnSpec, ColumnSpecBuilder, OneToMany, ManyToMany {

    /**
     * Shortcut for {@link #column(String)}.
     * Creates a new {@code ColumnSpecBuilder} configured with the specified column name.
     *
     * @param column the name of the column; must not be null or empty
     * @return this {@code ColumnSpecBuilder} for further chaining
     */
    static ColumnSpecBuilder<ColumnSpecBuilderImpl> c(final String column) {
        return column(column);
    }

    /**
     * Creates a new {@code ColumnSpecBuilder} configured with the specified column name.
     *
     * @param column the name of the column; must not be null or empty
     * @return this {@code ColumnSpecBuilder} for further chaining
     */
    static ColumnSpecBuilder<ColumnSpecBuilderImpl> column(final String column) {
        return new ColumnSpecBuilderImpl(column);
    }

    static OneToMany oneToMany(final FieldSpec mappedByField) {
        return new OneToMany(mappedByField);
    }

    static ManyToMany manyToMany(final String joinTable, final String joinColumn, final String inverseJoinColumn) {
        return new ManyToMany(joinTable, joinColumn, inverseJoinColumn);
    }
}
