package org.litebridgedb.orm.expression;

/**
 * Marker interface for select query expressions.
 */
public sealed interface ExpressionSpec permits ColumnExpressionSpec, ProtoExpressionSpec, TypeOverrideExpressionSpec {
}
