package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.intent.ConvertIntent;

/**
 * A query expression modifier that changes the result of the query.
 * <p>
 * These "expressions" are not translated to SQL; rather, they change the
 * behaviour of the query on the ORM side. They are intermediate objects to control
 * the flow of the fluent query API.
 */
public sealed interface ExpressionModifier permits ConvertIntent {

    /**
     * Get an {@link ExpressionSpec} instance that represents this modifier.
     *
     * @return an {@link ExpressionSpec} representing this modifier.
     */
    ExpressionSpec toExpression();
}
