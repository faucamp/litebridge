package org.litebridgedb.orm.api.register;

public class RegistrationManyToManyJoinColumnStep {

    private final String joinTable;

    RegistrationManyToManyJoinColumnStep(final String joinTable) {
        this.joinTable = joinTable;
    }

    public RegistrationManyToManyInverseJoinColumnStep joinColumn(final String column) {
        return new RegistrationManyToManyInverseJoinColumnStep(joinTable, column);
    }
}