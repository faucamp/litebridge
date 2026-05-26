package org.litebridgedb.orm.api.register;

public class ManyToManyBuilderJoinColumnStep {

    private final String joinTable;

    ManyToManyBuilderJoinColumnStep(final String joinTable) {
        this.joinTable = joinTable;
    }

    public ManyToManyBuilderInverseJoinColumnStep joinColumn(final String column) {
        return new ManyToManyBuilderInverseJoinColumnStep(joinTable, column);
    }
}