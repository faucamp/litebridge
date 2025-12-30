package org.litebridge.orm.api.spec;

public sealed interface ColumnSpecBuilder<SELF extends ColumnSpecBuilder<SELF>> extends ColumnSpec
        permits AbstractColumnSpecBuilder {

    SELF autoIncrement(boolean autoIncrement);

    SELF sequence(String sequence);

    SELF joinOn(String column);

    SELF joinUsing();

    /**
     * Creates a new {@code ColumnSpecBuilder} configured with the specified column name
     *
     * @param column the name of the column; must not be null or empty
     * @return this {@code ColumnSpecBuilder} for further chaining
     */
    static ColumnSpecBuilder<ColumnSpecBuilderImpl> c(final String column) {
        return new ColumnSpecBuilderImpl(column);
    }
}
