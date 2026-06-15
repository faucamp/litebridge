package org.litebridgedb.orm.function;

public sealed interface Expression permits Count, ProtoSelectColumn, SelectColumn, SelectField {
}
