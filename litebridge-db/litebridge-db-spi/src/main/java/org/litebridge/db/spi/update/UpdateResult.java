package org.litebridge.db.spi.update;

public sealed class UpdateResult permits InsertResult {

    private final int rowsAffected;

    public UpdateResult(final int rowsAffected) {
        this.rowsAffected = rowsAffected;
    }

    public int rowsAffected() {
        return rowsAffected;
    }
}
