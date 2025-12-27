package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

public final class ColumnSpecBuilder implements ColumnSpec {

    /**
     * Built ColumnSpec instance
     */
    @Nullable
    private ColumnSpecImpl columnSpec;

    /**
     * Database column name
     */
    private final String name;
    /**
     * Whether column is set to auto-increment
     */
    private boolean autoIncrement;
    /**
     * Name of the sequence used to generate values for this column
     */
    @Nullable
    private String sequence;

    /**
     * Field name of the nested DTO to join on
     */
    @Nullable
    private String joingColumn;

    private ColumnSpecBuilder(final String name) {
        this.name = name;
    }

    public ColumnSpecBuilder autoIncrement(final boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public ColumnSpecBuilder sequence(final String sequence) {
        this.sequence = ObjectUtils.requireNonNull(sequence, () -> new IllegalArgumentException("No sequence specified for column '" + name + "'"));
        return this;
    }

    public ColumnSpecBuilder joinOn(final String column) {
        this.joingColumn = column;
        return this;
    }

    public ColumnSpecBuilder joinUsing() {
        this.joingColumn = name;
        return this;
    }

    /**
     * Creates a new {@code ColumnSpecBuilder} configured with the specified column name
     *
     * @param column the name of the column; must not be null or empty
     * @return this {@code ColumnSpecBuilder} for further chaining
     */
    public static ColumnSpecBuilder c(final String column) {
        return new ColumnSpecBuilder(column);
    }

    @Override
    public String name() {
        return ensureColumnSpec().name();
    }

    @Override
    public boolean autoIncrement() {
        return ensureColumnSpec().autoIncrement();
    }

    @Override
    public @Nullable String sequence() {
        return ensureColumnSpec().sequence();
    }

    @Override
    public @Nullable String joinColumn() {
        return ensureColumnSpec().joinColumn();
    }

    private ColumnSpecImpl ensureColumnSpec() {
        if (columnSpec == null) {
            columnSpec = new ColumnSpecImpl(name, autoIncrement, sequence, joingColumn);
        }

        return columnSpec;
    }
}
