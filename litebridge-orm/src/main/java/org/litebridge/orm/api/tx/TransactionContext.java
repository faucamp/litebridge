package org.litebridge.orm.api.tx;

import org.litebridge.db.spi.tx.Isolation;
import org.litebridge.db.spi.tx.TransactionException;
import org.litebridge.db.spi.tx.TransactionManager;

public final class TransactionContext extends TransactionContextTerminal {

    public TransactionContext(TransactionManager transactionManager) {
        super(transactionManager);
    }

    /**
     * Indicates that the transaction should be read-only.
     * <p>
     * Read-only transactions are used when the transaction
     * is intended to only read data and not modify it.
     *
     * @return Available clauses for transaction configuration chaining
     */
    public ReadOnlyClause readOnly() {
        return new ReadOnlyClause(this);
    }

    /**
     * Sets the transaction isolation level for the current thread.
     * <p>
     * The isolation level determines the degree of isolation provided
     * for transactions, affecting how changes made by one transaction
     * are visible to other transactions.
     *
     * @return Available clauses for transaction configuration chaining
     */
    public IsolationClause isolation(final Isolation level) {
        return new IsolationClause(level, this);
    }

    /**
     * Commits the current transaction.
     * <p>
     * This method finalises all operations performed during the transaction
     * and applies any changes to the underlying database. After a successful
     * commit, the transaction is no longer active.
     *
     * @throws IllegalStateException if there is no active transaction to commit
     * @throws TransactionException  if an error occurs during the commit process
     */
    public void commit() {
        transactionManager.commit();
    }

    /**
     * Rolls back the current transaction.
     * <p>
     * This method undoes all changes made during the active transaction.
     * It reverts the state of the underlying data store to the point
     * before the transaction began. After a successful rollback, the
     * transaction is no longer active.
     * <p>
     * It should be called in cases where the transaction cannot be
     * successfully completed, such as when an error occurs during the
     * operations performed within the transaction.
     *
     * @throws IllegalStateException if there is no active transaction to roll back
     * @throws TransactionException  if an error occurs during the rollback process
     */
    public void rollback() {
        transactionManager.rollback();
    }
}
