package org.litebridge.db.spi.tx;

public interface TransactionManager extends TransactionControl {

    /**
     * Begins a new transaction for the current thread.
     * <p>
     * This method should be called before performing
     * operations that need to be executed within a transactional context.
     * It ensures that subsequent database interactions are part of the same transaction
     * until it is either committed or rolled back.
     * <p>
     * This method may be called multiple times (nested).
     *
     * @throws TransactionException if an error occurs during starting the transaction
     */
    void begin() throws TransactionException;

    void begin(boolean readOnly, Isolation isolation) throws TransactionException;

    /**
     * Performs cleanup operations for the transaction manager, releasing any resources held.
     * This method should be called when the transaction manager is no longer needed to ensure
     * proper resource management.
     * <p>
     * This method is idempotent; subsequent calls do nothing after the first cleanup.
     * <p>
     * This is typically called directly by implementations of {@link TransactionManager#commit()}
     * and {@link TransactionManager#rollback()}.
     *
     * @throws TransactionException if an error occurs during cleanup operations
     */
    void cleanup() throws TransactionException;

    /**
     * Indicates whether cleanup operations are yet to be performed for the transaction manager
     * (i.e. whether the manager itself "active" and holding onto resources).
     * <p>
     * This method returns {@code true} if cleanup still needs to be performed, {@code false} otherwise.
     *
     * @return {@code true} if cleanup still needs to be performed, {@code false} otherwise
     */
    boolean requiresCleanup();
}
