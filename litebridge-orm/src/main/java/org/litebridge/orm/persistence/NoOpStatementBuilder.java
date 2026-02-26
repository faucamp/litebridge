package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.Update;

public final class NoOpStatementBuilder implements StatementBuilder<Update> {

    @Override
    public StatementChain statementChain() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Update build() {
        throw new UnsupportedOperationException();
    }
}
