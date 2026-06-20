package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

/**
 * {@code COUNT()}: Selects the count of rows matching the query.
 */
public record CountSpec() implements TypeOverrideExpressionSpec<Long> {

    @Override
    public Class<Long> returnType() {
        return Long.class;
    }
}
