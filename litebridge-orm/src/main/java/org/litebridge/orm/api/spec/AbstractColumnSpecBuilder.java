package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.BooleanUtils;
import org.litebridge.commons.ObjectUtils;

public abstract sealed class AbstractColumnSpecBuilder<SELF extends AbstractColumnSpecBuilder<SELF>>
        implements ColumnSpec, ColumnSpecBuilder<SELF>
        permits ColumnSpecBuilderImpl, FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder {

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
    @Nullable
    private Boolean autoIncrement;
    /**
     * Name of the sequence used to generate values for this column
     */
    @Nullable
    private String sequence;

    /**
     * Field name of the nested DTO to join on
     */
    @Nullable
    private String joinColumn;

    AbstractColumnSpecBuilder(final String name) {
        this.name = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SELF autoIncrement(final boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return (SELF) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SELF sequence(final String sequence) {
        ObjectUtils.requireNull(this.sequence, () -> new IllegalArgumentException("Sequence already specified for column '" + name + "'"));
        this.sequence = ObjectUtils.requireNonNull(sequence, () -> new IllegalArgumentException("No sequence specified for column '" + name + "'"));
        return (SELF) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SELF joinOn(final String column) {
        this.joinColumn = column;
        return (SELF) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SELF joinUsing() {
        this.joinColumn = name;
        return (SELF) this;
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
            columnSpec = new ColumnSpecImpl(name, BooleanUtils.toBoolean(autoIncrement), sequence, joinColumn);
        }

        return columnSpec;
    }
}
