package org.litebridgedb.db.spi.tx;

/**
 * Provides control over the transaction lifecycle, including committing and rolling back transactions.
 * This interface extends {@link ConnectionProvider} to offer transaction-specific operations.
 * <p>
 * Implementations of this interface are expected to manage the transaction lifecycle, ensuring
 * that changes are either committed or rolled back appropriately. They also provide a mechanism
 * to check the status of the transaction for the current thread.
 */
public interface TransactionControl extends ConnectionProvider {

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
    void commit() throws TransactionException, IllegalStateException;

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
    void rollback() throws TransactionException, IllegalStateException;

    /**
     * Checks if there is an active transaction for the current thread.
     * <p>
     * This method determines whether a transaction has been started
     * and is not yet committed or rolled back.
     *
     * @return {@code true} if a thread-bound transaction is currently active, {@code false} otherwise
     */
    boolean isTransactionActive();

    /**
     * Checks if the current transaction is marked as rollback-only.
     * This indicates that the transaction cannot be successfully committed
     * and must be rolled back.
     *
     * @return {@code true} if the transaction is marked for rollback, {@code false} otherwise
     */
    boolean isRollbackOnly();
}
