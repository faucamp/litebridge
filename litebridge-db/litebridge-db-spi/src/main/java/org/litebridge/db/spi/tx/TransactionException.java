package org.litebridge.db.spi.tx;

public final class TransactionException extends RuntimeException {

    public TransactionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TransactionException(final String message) {
        super(message);
    }
}
