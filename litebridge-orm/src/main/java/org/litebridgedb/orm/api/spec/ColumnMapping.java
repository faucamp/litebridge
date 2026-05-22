package org.litebridgedb.orm.api.spec;

public sealed interface ColumnMapping permits ColumnSpec, OneToMany, ManyToMany {

    static OneToMany oneToMany(final FieldSpec mappedByField) {
        return new OneToMany(mappedByField);
    }

    static ManyToMany manyToMany(final String joinTable, final String joinColumn, final String inverseJoinColumn) {
        return new ManyToMany(joinTable, joinColumn, inverseJoinColumn);
    }
}
