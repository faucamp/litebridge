package org.litebridgedb.orm.expression.select;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.tracking.FieldAccessor;

/**
 * Expression that selects a DTO field.
 *
 * @param field  The field accessor for the field.
 * @param column The database column associated with the field.
 */
public record SelectFieldSpec(FieldAccessor field, Column column) implements ColumnExpressionSpec {
}
