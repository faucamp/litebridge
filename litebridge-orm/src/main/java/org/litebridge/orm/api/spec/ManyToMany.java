package org.litebridge.orm.api.spec;

public record ManyToMany(String joinTable, String joinColumn, String inverseJoinColumn) implements ColumnMapping {
}
