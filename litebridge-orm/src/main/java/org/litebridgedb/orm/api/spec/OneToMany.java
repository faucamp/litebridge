package org.litebridgedb.orm.api.spec;

public record OneToMany(FieldSpec mappedByField) implements ColumnMapping {
}
