package org.litebridge.db.spi.update;

/**
 * Result of a batch update operation performed on the database.
 * <p>
 * It encapsulates the number of rows affected by each operation in an array.
 */
public final class BatchUpdateResult implements UpdateOpResult {

    private final int[] rowsAffected;

    public BatchUpdateResult(final int[] rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    public int[] rowsAffected() {
        return rowsAffected;
    }
}
