package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.CountSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;
import org.litebridgedb.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridgedb.orm.expression.intent.ConvertSpec;

/**
 * A query expression that overrides the type of the result.
 *
 * @param <T> The type override of the expression result.
 */
public sealed interface TypeOverrideExpressionSpec<T> extends ExpressionSpec, TypeOverride<T> permits NumberTODelegateExpressionSpec, ProtoNestableTOExpr, StringTODelegateExpressionSpec, AvgSpec, CountSpec, MaxSpec, MinSpec, CurrentTimestampSpec, ConvertSpec {

}
