package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;

public final class FieldColumnSpecBuilder {

    private final FieldSpec fieldSpec;
    @Nullable
    private EmbeddedColumnSpecBuilder columnSpecBuilder;

    FieldColumnSpecBuilder(final FieldSpec fieldSpec) {
        this.fieldSpec = fieldSpec;
    }

    public EmbeddedColumnSpecBuilder c(final String column) {
        return column(column);
    }

    public EmbeddedColumnSpecBuilder column(final String column) {
        this.columnSpecBuilder = new EmbeddedColumnSpecBuilder(column);
        return columnSpecBuilder;
    }

    public FieldColumnSpec build() {
        if (columnSpecBuilder == null) {
            throw new IllegalStateException("Column not specified");
        }

        return new FieldColumnSpecImpl(fieldSpec, columnSpecBuilder.build());
    }

    public final class EmbeddedColumnSpecBuilder
            extends AbstractColumnSpecBuilder<EmbeddedColumnSpecBuilder>
            implements FieldColumnSpec {

        private EmbeddedColumnSpecBuilder(final String name) {
            super(name);
        }

        @Override
        public FieldSpec field() {
            return FieldColumnSpecBuilder.this.fieldSpec;
        }

        @Override
        public ColumnMapping column() {
            return build();
        }
    }
}
