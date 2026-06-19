package org.litebridgedb.orm.expression;

/**
 * Marker interface for select query expressions.
 */
public sealed interface Expression permits ColumnExpression, ProtoExpression, TypeOverrideExpression {
}
