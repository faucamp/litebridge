package org.litebridgedb.orm.function;

/**
 * Marker interface for select query expressions.
 */
public sealed interface Expression permits Count, ProtoSelectColumn, SelectColumn, SelectField {
}
