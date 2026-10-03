package org.litebridge.db.spi.update;

/**
 * Result of a batch update operation performed on the database.
 * <p>
 * It encapsulates the number of rows affected by each operation in an array.
 */
public record BatchUpdateResult(int[] rowsAffected) implements UpdateOpResult {

}
