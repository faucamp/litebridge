package org.litebridgedb.orm.api.spec;

public record FieldColumnSpecImpl(
        FieldSpec field,
        ColumnMapping column) implements FieldColumnSpec {
}
