package org.litebridgedb.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final TableMetaData tableMetaData = new TableMetaData(
                table,
                List.of("ID"),
                List.of(
                        new ColumnMetaData(table, "ID", false, Types.BIGINT),
                        new ColumnMetaData(table, "NAME", true, Types.VARCHAR)
                )
        );
        final List<Column> columns = List.of(
                tableMetaData.column("ID").toColumn(),
                tableMetaData.column("NAME").toColumn()
        );
        final RowValue row = new RowValue(List.of(
                new ColumnValue(tableMetaData.column("ID").toColumn(), 1L),
                new ColumnValue(tableMetaData.column("NAME").toColumn(), "testName")
        ));

        // When
        final Insert result = new Insert(tableMetaData.toTable(), columns, List.of(row), true);

        // Then
        assertEquals(table, result.table());
        assertEquals(columns, result.columns());
        assertEquals(List.of(row), result.rows());
        assertTrue(result.returnGeneratedKeys());
    }

    @Test
    void constructor_singleRow() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final TableMetaData tableMetaData = new TableMetaData(
                table,
                List.of("ID"),
                List.of(
                        new ColumnMetaData(table, "ID", false, Types.BIGINT),
                        new ColumnMetaData(table, "NAME", true, Types.VARCHAR)
                )
        );
        final RowValue row = new RowValue(List.of(
                new ColumnValue(tableMetaData.column("ID").toColumn(), 1L),
                new ColumnValue(tableMetaData.column("NAME").toColumn(), "testName")
        ));

        // When
        final Insert result = new Insert(table, row, false);

        // Then
        assertEquals(table, result.table());
        assertEquals(List.of(tableMetaData.column("ID").toColumn(), tableMetaData.column("NAME").toColumn()), result.columns());
        assertEquals(List.of(row), result.rows());
        assertEquals(false, result.returnGeneratedKeys());
    }

    @Test
    void constructor_rows() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final TableMetaData tableMetaData = new TableMetaData(
                table,
                List.of("ID"),
                List.of(
                        new ColumnMetaData(table, "ID", false, Types.BIGINT),
                        new ColumnMetaData(table, "NAME", true, Types.VARCHAR)
                )
        );
        final Column idColumn = tableMetaData.column("ID").toColumn();
        final Column nameColumn = tableMetaData.column("NAME").toColumn();
        final RowValue row1 = new RowValue(List.of(
                new ColumnValue(idColumn, 1L),
                new ColumnValue(nameColumn, "testName1")
        ));
        final RowValue row2 = new RowValue(List.of(
                new ColumnValue(idColumn, 2L),
                new ColumnValue(nameColumn, "testName2")
        ));

        // When
        final Insert result = new Insert(tableMetaData.toTable(), List.of(row1, row2), true);

        // Then
        assertEquals(table, result.table());
        assertEquals(List.of(tableMetaData.column("ID").toColumn(), tableMetaData.column("NAME").toColumn()), result.columns());
        assertEquals(List.of(row1, row2), result.rows());
        assertTrue(result.returnGeneratedKeys());
    }

    @Test
    void constructor_rows_empty() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final TableMetaData tableMetaData = new TableMetaData(
                table,
                List.of("ID"),
                List.of(
                        new ColumnMetaData(table, "ID", false, Types.BIGINT),
                        new ColumnMetaData(table, "NAME", true, Types.VARCHAR)
                )
        );

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class,
                () -> new Insert(tableMetaData.toTable(), List.of(), false));

        // Then
        assertEquals("No rows to insert for table: TEST_TABLE", result.getMessage());
    }
}