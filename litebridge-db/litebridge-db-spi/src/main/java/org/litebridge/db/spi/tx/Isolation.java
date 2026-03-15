package org.litebridge.db.spi.tx;

import java.sql.Connection;

/**
 * Transaction isolation levels.
 */
public enum Isolation {
    /**
     * Let the database decide
     */
    DEFAULT(-1),
    /**
     * Read uncommitted isolation level.
     *
     * @see Connection#TRANSACTION_READ_UNCOMMITTED
     */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    /**
     * Read committed isolation level.
     *
     * @see Connection#TRANSACTION_READ_COMMITTED
     */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    /**
     * Repeatable read isolation level.
     *
     * @see Connection#TRANSACTION_REPEATABLE_READ
     */
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    /**
     * Serializable isolation level.
     *
     * @see Connection#TRANSACTION_SERIALIZABLE
     */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    /**
     * Represents the numeric value corresponding to a specific transaction isolation level.
     * Each isolation level is mapped to an integer value that aligns with the constants
     * defined in {@link Connection}.
     */
    private final int level;

    /**
     * Constructs an isolation level instance with the specified numeric value.
     *
     * @param level the integer value representing a specific transaction isolation level.
     *              This value corresponds to isolation levels defined in {@link Connection}.
     */
    Isolation(int level) {
        this.level = level;
    }

    /**
     * Retrieves the numeric isolation level associated with this instance.
     *
     * @return the numeric representation of the isolation level
     */
    public int level() {
        return level;
    }
}
