package org.litebridge.orm.expression;

import org.litebridge.orm.expression.function.aggregate.AvgSpec;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.function.aggregate.MaxSpec;
import org.litebridge.orm.expression.function.aggregate.MinSpec;
import org.litebridge.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridge.orm.expression.intent.ConvertSpec;

/**
 * A query expression that overrides the type of the result.
 *
 * @param <T> The type override of the expression result.
 */
public sealed interface TypeOverrideExpressionSpec<T> extends ExpressionSpec, TypeOverride<T> permits NumberTODelegateExpressionSpec, ProtoNestableTOExpr, StringTODelegateExpressionSpec, AvgSpec, CountSpec, MaxSpec, MinSpec, CurrentTimestampSpec, ConvertSpec {

}
