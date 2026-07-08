package org.litebridgedb.db.spi.update;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.ConditionGroup;

import java.util.List;

/**
 * A SQL UPDATE statement targeting a specific table, with associated column values and conditions.
 * <p>
 * This class is a record that combines:
 * <ul>
 *     <li>A target {@link TableMetaData} representing the table to update.</li>
 *     <li>A list of {@link ColumnValue} objects specifying the columns and their new values.</li>
 *     <li>A list of {@link Condition} objects that define the WHERE clause conditions for the update operation.</li>
 * </ul>
 * This class is immutable and serves as part of the structure for creating and representing
 * SQL UPDATE operations in a database context.
 *
 * @param table        {@link TableMetaData} provides metadata about the table being updated, including its structure.
 * @param columnValues The {@link ColumnValue} pairs a column with its new value, ensuring clear definition of updates.
 * @param where        The {@link ConditionGroup} objects define filter criteria that determine which rows of the table will be updated.
 */
public record Update(Table table, List<ColumnValue> columnValues, List<ConditionGroup> where)
        implements UpdateStatement {
}
