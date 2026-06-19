package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.TypeOverrideExpression;

/**
 * {@code COUNT()}: Selects the count of rows matching the query.
 */
public record CountSpec() implements TypeOverrideExpression<Long> {

    @Override
    public Class<Long> returnType() {
        return Long.class;
    }
}
