package org.litebridgedb.orm.function;

/**
 * Marker interface for select query expressions.
 */
public sealed interface Expression permits ProtoSelectColumn, SelectColumn, SelectField, TypeOverrideExpression {
}
