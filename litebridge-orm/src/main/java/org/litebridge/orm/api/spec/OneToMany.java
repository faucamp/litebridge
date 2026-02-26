package org.litebridge.orm.api.spec;

public record OneToMany(FieldSpec mappedByField) implements ColumnMapping {
}
