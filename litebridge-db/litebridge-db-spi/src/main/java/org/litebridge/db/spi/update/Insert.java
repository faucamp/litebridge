package org.litebridge.db.spi.update;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;

import java.util.List;

/**
 * A SQL INSERT statement targeting specific table metadata with associated columns and rows of values.
 * <p>
 * This class is a record that combines:
 * <ul>
 *  <li>A target {@link TableMetaData} for the insertion</li>
 *  <li>A list of {@link ColumnMetaData} representing the columns involved in the insertion</li>
 *  <li>A list of {@link RowValue} representing the rows to be inserted</li>
 * </ul>
 * <p>
 * This class also provides constructors for creating instances with either a single row
 * or multiple rows of data, automatically populating column information based on the rows provided.
 * <p>
 * Instances of this class are immutable and serve as part of the structure for building SQL
 * update operations targeting a database.
 *
 * @param table               The target table for the insertion operation.
 * @param columns             The list of columns involved in the insertion operation.
 * @param rows                The list of rows to be inserted.
 * @param returnGeneratedKeys If true, generated keys will be returned after the insert operation.
 */
public record Insert(Table table,
                     List<Column> columns,
                     List<RowValue> rows,
                     boolean returnGeneratedKeys)
        implements UpdateStatement {

    /**
     * Constructs a new {@code Insert} instance for a single row.
     *
     * @param table               the target table for the insertion
     * @param row                 the row to be inserted
     * @param returnGeneratedKeys whether to return generated keys
     */
    public Insert(final Table table, final RowValue row, final boolean returnGeneratedKeys) {
        this(table, row.columns().stream().map(ColumnValue::column).toList(), List.of(row), returnGeneratedKeys);
    }

    /**
     * Constructs a new {@code Insert} instance for multiple rows.
     *
     * @param table               the target table for the insertion
     * @param rows                the list of rows to be inserted
     * @param returnGeneratedKeys whether to return generated keys
     */
    public Insert(final Table table, final List<RowValue> rows, final boolean returnGeneratedKeys) {
        this(table,
                CollectionUtils.requireNonEmpty(rows, "No rows to insert for table: " + table.name())
                        .getFirst()
                        .columns().stream()
                        .map(ColumnValue::column)
                        .toList(),
                rows,
                returnGeneratedKeys);
    }
}
