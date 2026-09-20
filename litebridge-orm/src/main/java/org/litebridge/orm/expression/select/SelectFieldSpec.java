package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.tracking.FieldAccessor;

/**
 * Expression spec that selects a DTO field.
 */
@Deprecated(forRemoval = true)
public final class SelectFieldSpec extends SelectColumnSpec {

    private final FieldAccessor field;

    /**
     * Creates a new expression spec that selects a DTO field.
     *
     * @param field  The field accessor for the field.
     * @param column The database column associated with the field.
     */
    public SelectFieldSpec(FieldAccessor field, Column column) {
        super(column);
        this.field = field;
    }

    public SelectFieldSpec(FieldAccessor field, Column column, final @Nullable String alias, final @Nullable String tableAlias) {
        super(column);
        this.field = field;
        this.alias = alias;
        this.tableAlias = tableAlias;
    }

    /**
     * Returns the field accessor for the DTO field.
     *
     * @return the field accessor
     */
    public FieldAccessor field() {
        return field;
    }
}
