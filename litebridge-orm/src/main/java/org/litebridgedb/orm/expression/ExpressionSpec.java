package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.intent.ExpressionSpecArray;
import org.litebridgedb.orm.expression.select.SubselectSpec;

/**
 * Marker interface for select query expressions.
 */
public sealed interface ExpressionSpec permits ColumnExpressionSpec, ProtoExpressionSpec, TypeOverrideExpressionSpec, ExpressionSpecArray, SubselectSpec {
}
