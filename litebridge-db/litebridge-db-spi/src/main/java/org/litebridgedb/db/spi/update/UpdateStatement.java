package org.litebridgedb.db.spi.update;

import org.litebridgedb.db.spi.Operation;

/**
 * Common marker interface for database update operations.
 * <p>
 * Specific update-related operations such as INSERT or UPDATE implement this interface to ensure a standard structure.
 * It serves as a marker type for classes representing various types of update statements in a database.
 * <p>
 * Implementing classes include:
 * <ul>
 *     <li>{@link Insert}: Represents an insert operation with a target table, columns, and rows.</li>
 *     <li>{@link Update}: Represents an update operation with target table, column values, and conditions.</li>
 * </ul>
 * <p>
 * This interface is part of the update-related models for database operations.
 */
public sealed interface UpdateStatement extends Operation permits Delete, Insert, Update {
}
