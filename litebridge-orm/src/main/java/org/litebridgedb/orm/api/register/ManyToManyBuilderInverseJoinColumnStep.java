package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.ManyToMany;

public final class ManyToManyBuilderInverseJoinColumnStep {

    private final String joinTable;
    private final String joinColumn;

    ManyToManyBuilderInverseJoinColumnStep(final String joinTable, final String joinColumn) {
        this.joinTable = joinTable;
        this.joinColumn = joinColumn;
    }

    public ManyToMany inverseJoinColumn(final String column) {
        return new ManyToMany(joinTable, joinColumn, column);
    }
}