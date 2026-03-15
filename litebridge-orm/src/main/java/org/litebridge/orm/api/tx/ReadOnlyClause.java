package org.litebridge.orm.api.tx;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionControl;

public final class ReadOnlyClause {

    private final TransactionContextTerminal transactionContextTerminal;

    public ReadOnlyClause(final TransactionContextTerminal transactionContextTerminal) {
        this.transactionContextTerminal = transactionContextTerminal;
    }

    public TransactionContextTerminal isolation(Isolation level) {
        transactionContextTerminal.setIsolation(level);
        return transactionContextTerminal;
    }

    public TransactionControl begin() {
        return transactionContextTerminal.begin();
    }
}
