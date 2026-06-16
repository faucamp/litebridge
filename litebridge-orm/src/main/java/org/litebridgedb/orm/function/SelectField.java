package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.tracking.FieldAccessor;

/**
 * Expression that selects a DTO field.
 *
 * @param field  The field accessor for the field.
 * @param column The database column associated with the field.
 */
public record SelectField(FieldAccessor field, Column column) implements ColumnExpression {
}
