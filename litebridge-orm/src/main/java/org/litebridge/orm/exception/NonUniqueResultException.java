package org.litebridge.orm.exception;

/**
 * Thrown to indicate that a query returned more than one result when exactly one was expected.
 * <p>
 * This exception is typically used in scenarios where unique results are required, such as in
 * methods that retrieve a single entity, but the result set contains multiple records.
 */
public class NonUniqueResultException extends IllegalStateException {

    /**
     * Constructs a {@code NonUniqueResultException} with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the underlying cause of the exception, which may be {@code null}
     */
    public NonUniqueResultException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
