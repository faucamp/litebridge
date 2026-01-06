package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.UpdateStatement;

abstract sealed class AbstractStatementBuilder<US extends UpdateStatement>
        permits InsertBuilder, UpdateBuilder {

    protected final Table table;
    private final StatementChain statementChain = new StatementChain();

    protected AbstractStatementBuilder(final Table table) {
        this.table = table;
    }

    public StatementChain statementChain() {
        return statementChain;
    }

    public abstract US build();
}
