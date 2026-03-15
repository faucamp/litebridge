package org.litebridge.orm.api.tx;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionControl;

public final class IsolationClause {

    private final TransactionContextTerminal transactionContextTerminal;

    public IsolationClause(final Isolation level, final TransactionContextTerminal transactionContextTerminal) {
        this.transactionContextTerminal = transactionContextTerminal;
        this.transactionContextTerminal.setIsolation(level);
    }

    public TransactionContextTerminal readOnly() {
        transactionContextTerminal.setReadOnly();
        return transactionContextTerminal;
    }

    public TransactionControl begin() {
        return transactionContextTerminal.begin();
    }
}
