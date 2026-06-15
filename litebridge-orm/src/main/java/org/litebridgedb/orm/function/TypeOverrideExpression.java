package org.litebridgedb.orm.function;

/**
 * A query expression that overrides the type of the result.
 */
public sealed interface TypeOverrideExpression<T> extends Expression permits Count {

    Class<T> type();
}
