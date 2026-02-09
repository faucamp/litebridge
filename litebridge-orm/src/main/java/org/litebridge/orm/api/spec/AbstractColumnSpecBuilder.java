package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.BooleanUtils;
import org.litebridge.commons.ObjectUtils;

public abstract sealed class AbstractColumnSpecBuilder<SELF extends AbstractColumnSpecBuilder<SELF>>
        implements ColumnSpecBuilder<SELF>
        permits ColumnSpecBuilderImpl, FieldColumnSpecBuilder.EmbeddedColumnSpecBuilder {

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
    /**
     * In-line mapped table that is
     */
    @Nullable
    private TableMapping mappedTable;

    AbstractColumnSpecBuilder(final String name) {
        this.name = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ColumnSpecAutoIncrementTerminal<SELF> autoIncrement() {
        this.autoIncrement = true;
        return new ColumnSpecAutoIncrementTerminal<>((SELF) this);
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

    @SuppressWarnings("unchecked")
    public SELF withMappedTable(final Class<?> dtoClass, final TableSpec mappedTable) {
        this.mappedTable = new TableMapping(dtoClass, mappedTable);
        return (SELF) this;
    }

    public ColumnSpec build() {
        return new ColumnSpec(name, BooleanUtils.toBoolean(autoIncrement), sequence, joinColumn, mappedTable);
    }

    void setSequence(final String sequence) {
        ObjectUtils.requireNull(this.sequence, () -> new IllegalArgumentException("Sequence already specified for column '" + name + "'"));
        this.sequence = ObjectUtils.requireNonNull(sequence, () -> new IllegalArgumentException("No sequence specified for column '" + name + "'"));
    }
}
