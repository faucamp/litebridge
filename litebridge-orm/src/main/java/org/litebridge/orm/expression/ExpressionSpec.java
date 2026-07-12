package org.litebridge.orm.expression;

import org.litebridge.orm.expression.intent.ExpressionSpecArray;
import org.litebridge.orm.expression.select.SubselectSpec;
import org.litebridge.orm.meta.QueryField;

/**
 * Marker interface for select query expressions.
 */
public sealed interface ExpressionSpec permits ColumnExpressionSpec, ProtoExpressionSpec, TypeOverrideExpressionSpec, ExpressionSpecArray, SubselectSpec, QueryField {
}
