package org.litebridgedb.db.spi.tx;

/**
 * Exception type representing errors that occur during transactional operations.
 * <p>
 * This exception serves as a runtime wrapper for issues that arise in the context
 * of database transactions, such as failures in transactional boundaries,
 * integrity violations, or isolation level-related errors.
 */
public final class TransactionException extends RuntimeException {

    /**
     * Constructs a new {@code TransactionException} with the specified detail.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public TransactionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code TransactionException} with the specified detail.
     *
     * @param message the detail message
     */
    public TransactionException(final String message) {
        super(message);
    }
}
