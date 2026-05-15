package org.litebridgedb.orm.api.register;

public class ManyToManyBuilder {

    public RegistrationManyToManyJoinColumnStep joinTable(final String table) {
        return new RegistrationManyToManyJoinColumnStep(table);
    }
}
