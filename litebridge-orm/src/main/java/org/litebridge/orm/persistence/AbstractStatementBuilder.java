package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.UpdateStatement;

public abstract sealed class AbstractStatementBuilder<US extends UpdateStatement> implements StatementBuilder<US>
        permits InsertBuilder, UpdateBuilder, DeleteBuilder {

    protected final OrmTable ormTable;
    private final StatementChain statementChain = new StatementChain();

    protected AbstractStatementBuilder(final OrmTable ormTable) {
        this.ormTable = ormTable;
    }

    public StatementChain statementChain() {
        return statementChain;
    }

    public abstract US build();
}
