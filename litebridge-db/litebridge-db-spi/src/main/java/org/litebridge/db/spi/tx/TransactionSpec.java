package org.litebridge.db.spi.tx;

public record TransactionSpec(boolean readOnly, Isolation isolation) {
}
