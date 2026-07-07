package org.litebridgedb.db.spi.update;

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

    public UpdateResult(final int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

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
