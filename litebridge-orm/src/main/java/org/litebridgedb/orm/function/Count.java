package org.litebridgedb.orm.function;

/**
 * {@code COUNT()}: Selects the count of rows matching the query.
 */
public record Count() implements TypeOverrideExpression<Long> {

    @Override
    public Class<Long> returnType() {
        return Long.class;
    }
}
