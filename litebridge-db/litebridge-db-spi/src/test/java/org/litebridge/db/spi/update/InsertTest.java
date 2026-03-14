package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;

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
        final List<ColumnMetaData> columns = List.of(
                tableMetaData.column("ID"),
                tableMetaData.column("NAME")
        );
        final RowValue row = new RowValue(List.of(
                new ColumnValue(tableMetaData.column("ID"), 1L),
                new ColumnValue(tableMetaData.column("NAME"), "testName")
        ));

        // When
        final Insert result = new Insert(tableMetaData, columns, List.of(row), true);

        // Then
        assertEquals(tableMetaData, result.table());
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
                new ColumnValue(tableMetaData.column("ID"), 1L),
                new ColumnValue(tableMetaData.column("NAME"), "testName")
        ));

        // When
        final Insert result = new Insert(tableMetaData, row, false);

        // Then
        assertEquals(tableMetaData, result.table());
        assertEquals(List.of(tableMetaData.column("ID"), tableMetaData.column("NAME")), result.columns());
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
        final RowValue row1 = new RowValue(List.of(
                new ColumnValue(tableMetaData.column("ID"), 1L),
                new ColumnValue(tableMetaData.column("NAME"), "testName1")
        ));
        final RowValue row2 = new RowValue(List.of(
                new ColumnValue(tableMetaData.column("ID"), 2L),
                new ColumnValue(tableMetaData.column("NAME"), "testName2")
        ));

        // When
        final Insert result = new Insert(tableMetaData, List.of(row1, row2), true);

        // Then
        assertEquals(tableMetaData, result.table());
        assertEquals(List.of(tableMetaData.column("ID"), tableMetaData.column("NAME")), result.columns());
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
                () -> new Insert(tableMetaData, List.of(), false));

        // Then
        assertEquals("No rows to insert for table: TEST_TABLE", result.getMessage());
    }
}