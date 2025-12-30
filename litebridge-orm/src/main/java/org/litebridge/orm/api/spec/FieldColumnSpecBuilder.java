package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

public final class FieldColumnSpecBuilder implements FieldColumnSpec {

    private final FieldSpecBuilder<EmbeddedColumnSpecBuilder> fieldSpecBuilder;
    @Nullable
    private EmbeddedColumnSpecBuilder columnSpecBuilder;

    FieldColumnSpecBuilder(final String field) {
        this.fieldSpecBuilder = new FieldSpecBuilderImpl(field);
    }

    FieldColumnSpecBuilder(final FieldSpecBuilder<EmbeddedColumnSpecBuilder> fieldSpecBuilder) {
        this.fieldSpecBuilder = fieldSpecBuilder;
    }

    public EmbeddedColumnSpecBuilder column(final String column) {
        this.columnSpecBuilder = new EmbeddedColumnSpecBuilder(column);
        return columnSpecBuilder;
    }

    @Override
    public FieldSpec field() {
        return fieldSpecBuilder;
    }

    @Override
    public ColumnSpec column() {
        return ObjectUtils.requireNonNull(columnSpecBuilder, "Column spec not set");
    }

    public final class EmbeddedColumnSpecBuilder
            extends AbstractColumnSpecBuilder<EmbeddedColumnSpecBuilder>
            implements FieldColumnSpec {

        private EmbeddedColumnSpecBuilder(final String name) {
            super(name);
        }

        @Override
        public FieldSpec field() {
            return FieldColumnSpecBuilder.this.field();
        }

        @Override
        public ColumnSpec column() {
            return FieldColumnSpecBuilder.this.column();
        }
    }
}
