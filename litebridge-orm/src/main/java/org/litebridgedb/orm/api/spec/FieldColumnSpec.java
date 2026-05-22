package org.litebridgedb.orm.api.spec;

public record FieldColumnSpec(FieldSpec field, ColumnMapping column) {

    public ColumnSpec columnSpec() {
        return (ColumnSpec) column();
    }
}
