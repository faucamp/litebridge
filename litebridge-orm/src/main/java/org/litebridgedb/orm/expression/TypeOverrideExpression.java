package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.CountSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;
import org.litebridgedb.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridgedb.orm.expression.function.java.ConvertSpec;

/**
 * A query expression that overrides the type of the result.
 */
public sealed interface TypeOverrideExpression<T> extends Expression permits NumberTONestableExpression, ProtoNestableTOExpr, StringTONestableExpression, AvgSpec, CountSpec, MaxSpec, MinSpec, CurrentTimestampSpec, ConvertSpec {

    /**
     * Gets the return type override of the query result.
     *
     * @return the type of the result.
     */
    Class<T> returnType();
}
