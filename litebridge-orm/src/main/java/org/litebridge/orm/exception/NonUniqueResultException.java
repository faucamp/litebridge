package org.litebridge.orm.exception;

public class NonUniqueResultException extends IllegalStateException {

    public NonUniqueResultException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
