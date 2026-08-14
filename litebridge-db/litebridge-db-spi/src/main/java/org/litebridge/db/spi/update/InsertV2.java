package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
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
public record InsertV2(Table table,
                       List<InsertColumn> columns,
                       int rows,
                       boolean returnGeneratedKeys)
        implements UpdateStatement {

    public record InsertColumn(String name, @Nullable Object generatedValue) {
    }
}
