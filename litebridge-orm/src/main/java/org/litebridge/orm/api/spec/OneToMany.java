package org.litebridge.orm.api.spec;

public record OneToMany(String mappedByField) implements ColumnMapping {
}
