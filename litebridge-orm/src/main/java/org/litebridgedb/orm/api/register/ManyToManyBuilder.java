package org.litebridgedb.orm.api.register;

public class ManyToManyBuilder {

    public ManyToManyBuilderJoinColumnStep joinTable(final String table) {
        return new ManyToManyBuilderJoinColumnStep(table);
    }
}
