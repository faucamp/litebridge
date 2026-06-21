package org.litebridgedb.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.db.spi.update.ColumnValue;
import org.litebridgedb.db.spi.update.Insert;
import org.litebridgedb.db.spi.update.RowValue;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.litebridgedb.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsertSqlGeneratorTest {

    @Mock
    private TypeConverter typeConverter;
    @Mock
    private BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;
    private InsertSqlGenerator insertSqlGenerator;

    @BeforeEach
    void beforeEach() {
        insertSqlGenerator = new InsertSqlGenerator(typeConverter, new ColumnIdentifierGenerator(), ensureTableMetaData);
    }

    @Test
    void prepareSql_insert_withMultipleRows() throws Exception {
        // Given
        final Column column = createTestColumn();

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.toColumn()).thenReturn(column);

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(tableMetaData.toTable()).thenReturn(column.table());
        when(ensureTableMetaData.apply(eq(column.table()), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        final ColumnValue columnValue1 = new ColumnValue(column, "value1");
        final ColumnValue columnValue2 = new ColumnValue(column, "value2");
        final RowValue rowValue1 = new RowValue(List.of(columnValue1));
        final RowValue rowValue2 = new RowValue(List.of(columnValue2));

        final Insert insert = new Insert(tableMetaData.toTable(), List.of(columnMetaData.toColumn()), List.of(rowValue1, rowValue2), false);

        when(typeConverter.convert(anyString(), anyInt())).then(i -> i.getArgument(0));

        // When
        final PreparedSql result = insertSqlGenerator.prepareSql(insert, mock(TransactionManager.class));

        // Then
        assertNotNull(result);
        assertTrue(result.sql().contains("VALUES"));
        assertEquals(2, result.bindValues().size());
    }

    @Test
    void prepareRow_withSequenceAndNonNullValue() throws Exception {
        // Given
        final Column column = createTestColumn();

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(ensureTableMetaData.apply(eq(column.table()), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        final RowValue rowValue = new RowValue(List.of(new ColumnValue(column, "testValue")));

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final PreparedRow result = insertSqlGenerator.prepareRow(rowValue, mock(TransactionManager.class));

        // Then
        assertNotNull(result);
        assertEquals(1, result.valueSpecifiers().size());
        assertEquals("?", result.valueSpecifiers().get(0));
        assertEquals(1, result.bindValues().size());
    }

    @Test
    void prepareRow_nullableColumnWithNullValue() throws Exception {
        // Given
        final Column column = createTestColumn();
        final RowValue rowValue = new RowValue(List.of(new ColumnValue(column, null)));

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.isNullable()).thenReturn(true);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(ensureTableMetaData.apply(eq(column.table()), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        // When
        final PreparedRow preparedRow = insertSqlGenerator.prepareRow(rowValue, mock(ConnectionProvider.class));

        // Then
        assertNotNull(preparedRow);
        assertTrue(preparedRow.valueSpecifiers().isEmpty());
        assertTrue(preparedRow.bindValues().isEmpty());
    }

    @Test
    void prepareRow_autoIncrementColumnWithNullValue() throws Exception {
        // Given
        final Column column = createTestColumn();
        final RowValue rowValue = new RowValue(List.of(new ColumnValue(column, null)));

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.isAutoIncrement()).thenReturn(true);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(ensureTableMetaData.apply(eq(column.table()), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        // When
        final PreparedRow preparedRow = insertSqlGenerator.prepareRow(rowValue, mock(ConnectionProvider.class));

        // Then
        assertNotNull(preparedRow);
        assertTrue(preparedRow.valueSpecifiers().isEmpty());
        assertTrue(preparedRow.bindValues().isEmpty());
    }
}