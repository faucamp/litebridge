package org.litebridge.orm.api.spec;

public interface FieldColumnSpec {
    FieldSpec field();
    ColumnMapping column();

    default ColumnSpec columnSpec() {
        return (ColumnSpec) column();
    }
}
