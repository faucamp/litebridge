package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.CountSpec;

/**
 * A query expression that overrides the type of the result.
 */
public sealed interface TypeOverrideExpression<T> extends Expression permits AvgSpec, CountSpec, ProtoNestableTOExpr, StringTONestableExpression {

    /**
     * Gets the return type override of the query result.
     *
     * @return the type of the result.
     */
    Class<T> returnType();
}
