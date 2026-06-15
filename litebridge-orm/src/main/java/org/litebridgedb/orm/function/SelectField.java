package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.tracking.FieldAccessor;

public final class SelectField implements Expression {

    private final String fieldName;
    private @Nullable FieldAccessor fieldAccessor;
    private @Nullable Column column;

    public SelectField(String fieldName) {
        this.fieldName = fieldName;
    }

    public SelectField(final FieldAccessor fieldAccessor, final Column column) {
        this.fieldName = fieldAccessor.name();
        this.fieldAccessor = fieldAccessor;
        this.column = column;
    }

    public String fieldName() {
        return fieldName;
    }

    public @Nullable FieldAccessor getFieldAccessor() {
        return fieldAccessor;
    }

    public void setFieldAccessor(@Nullable final FieldAccessor fieldAccessor) {
        this.fieldAccessor = fieldAccessor;
    }

    public @Nullable Column getColumn() {
        return column;
    }

    public void setColumn(@Nullable final Column column) {
        this.column = column;
    }
}
