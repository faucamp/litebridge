package org.litebridge.orm.api.register;

import org.litebridge.orm.api.spec.ManyToMany;

public final class RegistrationManyToManyInverseJoinColumnStep {

    private final String joinTable;
    private final String joinColumn;

    RegistrationManyToManyInverseJoinColumnStep(final String joinTable, final String joinColumn) {
        this.joinTable = joinTable;
        this.joinColumn = joinColumn;
    }

    public ManyToMany inverseJoinColumn(final String column) {
        return new ManyToMany(joinTable, joinColumn, column);
    }
}