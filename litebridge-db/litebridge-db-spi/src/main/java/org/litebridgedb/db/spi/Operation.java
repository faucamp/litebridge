package org.litebridgedb.db.spi;

import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.update.UpdateStatement;

/**
 * Common abstraction for SQL operations in the database.
 * <p>
 * This sealed interface serves as the root of the hierarchy for database operation
 * models, specifically for SQL {@code SELECT} queries and update-related statements such
 * as {@code INSERT}, {@code UPDATE}, and {@code DELETE}.
 * <p>
 * Permitted subtypes:
 * <ul>
 *     <li>{@link Select}: Encapsulates components required to construct a SQL {@code SELECT} query,
 *     including target table, expressions, joins, conditions, and more.</li>
 *     <li>{@link UpdateStatement}: Serves as a marker type for classes representing update-related operations in a database
 *     (e.g., {@code INSERT}, {@code UPDATE}, {@code DELETE} statements).</li>
 * </ul>
 * Implementing this interface indicates that a class represents a structured model
 * for interacting with SQL queries or statements.
 */
public sealed interface Operation permits Select, UpdateStatement {

    /**
     * Get the target table for the operation.
     *
     * @return the target table of the operation
     */
    Table table();
}
