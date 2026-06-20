package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.intent.ConvertIntent;

public sealed interface TypeOverride<T> permits TypeOverrideExpressionSpec, ConvertIntent {

    /**
     * Gets the return type override of the query result.
     *
     * @return the type of the result.
     */
    Class<T> returnType();
}
