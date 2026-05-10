package org.litebridge.db.spi.update;

import org.litebridge.db.spi.Table;

/**
 * Common marker interface for database update operations.
 * <p>
 * Specific update-related operations such as INSERT or UPDATE implement this interface to ensure a standard structure.
 * It serves as a marker type for classes representing various types of update statements in a database.
 * <p>
 * Implementing classes include:
 * - Insert: Represents an insert operation with a target table, columns, and rows.
 * - Update: Represents an update operation with target table, column values, and conditions.
 * <p>
 * This interface is part of the update-related models for database operations.
 */
public interface UpdateStatement {

    /**
     * Get the target table for the update statement.
     *
     * @return the target table of the update statement
     */
    Table table();
}
