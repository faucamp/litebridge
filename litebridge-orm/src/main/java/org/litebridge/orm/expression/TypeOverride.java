package org.litebridge.orm.expression;

import org.litebridge.orm.expression.intent.ConvertIntent;

/**
 * Indicates that a query result should be converted to a different type.
 *
 * @param <T> the type of the result to convert the result to.
 */
public sealed interface TypeOverride<T> permits TypeOverrideExpressionSpec, ConvertIntent {

    /**
     * Gets the return type override of the query result.
     *
     * @return the type of the result.
     */
    Class<T> returnType();
}
