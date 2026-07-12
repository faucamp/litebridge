package org.litebridge.db.spi.update;

import java.util.StringJoiner;

/**
 * Result of an update operation performed on the database.
 * <p>
 * It encapsulates the number of rows affected by the operation.
 * <p>
 * This class is a sealed type and only permits a specific subclass: {@link InsertResult}.
 */
public sealed class UpdateResult permits InsertResult {

    private final int rowsAffected;

    /**
     * Constructs an {@code UpdateResult} instance representing the result of an update
     * operation performed on the database.
     *
     * @param rowsAffected The number of rows affected by the update operation.
     */
    public UpdateResult(final int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    /**
     * Retrieves the number of rows affected by the database update operation.
     *
     * @return The number of rows that were modified or impacted as a result of
     * the executed update operation.
     */
    public int rowsAffected() {
        return rowsAffected;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UpdateResult.class.getSimpleName() + "[", "]")
                .add("rowsAffected=" + rowsAffected)
                .toString();
    }
}
