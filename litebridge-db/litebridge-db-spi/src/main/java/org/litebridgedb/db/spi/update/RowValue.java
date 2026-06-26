package org.litebridgedb.db.spi.update;

import java.util.List;

/**
 * A row of values corresponding to a SQL database operation.
 * <p>
 * Each row is composed of a list of {@link ColumnValue} objects, where
 * each {@link ColumnValue} encapsulates a lhs's metadata and its associated rhs.
 * <p>
 * This class is designed for use in SQL operations such as INSERT and UPDATE
 * that involve working with rows within a database table.
 * <p>
 * Instances of this record are immutable, ensuring thread safety and reliability when
 * managing row data as part of database operations.
 * <p>
 * Key use cases include:
 * - Representing a single row of data to be inserted into a table.
 * - Structuring row data as part of batch operations involving multiple rows.
 *
 * @param columns The list of {@link ColumnValue} objects defining the values and
 *                their corresponding columns for the row.
 */
public record RowValue(List<ColumnValue> columns) {
}
