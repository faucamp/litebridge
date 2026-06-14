package org.litebridgedb.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.impl.function.SelectColumn;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Limit;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.OrderBy;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.ManagedConnection;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.db.spi.update.ColumnValue;
import org.litebridgedb.db.spi.update.Insert;
import org.litebridgedb.db.spi.update.InsertResult;
import org.litebridgedb.db.spi.update.RowValue;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractDatabaseProviderTest {

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private TypeConverter typeConverter;

    @InjectMocks
    private TestDatabaseProvider databaseProvider;

    private ManagedConnection connection;

    @Test
    void tableMetaData() throws Exception {
        tableMetaDataImpl();
    }

    private TableMetaData tableMetaDataImpl() throws SQLException {
        return tableMetaDataImpl("TEST_SCHEMA");
    }

    private TableMetaData tableMetaDataImpl(final String schema) throws SQLException {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", schema, "TEST_TABLE");

        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(true);
        when(tableResultSet.getString("TABLE_NAME")).thenReturn(table.name());
        when(databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(tableResultSet);

        final ResultSet pkResultSet = mock(ResultSet.class);
        when(pkResultSet.next()).thenReturn(true).thenReturn(false);
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK").thenReturn("TEST_COLUMN");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR).thenReturn(Types.VARCHAR);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(10).thenReturn(10);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        // When
        final TableMetaData result = databaseProvider.tableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(2, result.columns().size());

        assertEquals("TEST_PK", result.column("TEST_PK").name());
        assertTrue(result.column("TEST_PK").isNullable());
        assertEquals(Types.VARCHAR, result.column("TEST_PK").getDataType());
        assertEquals(10, result.column("TEST_PK").getSize());

        assertEquals("TEST_COLUMN", result.column("TEST_COLUMN").name());
        assertFalse(result.column("TEST_COLUMN").isNullable());
        assertEquals(Types.VARCHAR, result.column("TEST_COLUMN").getDataType());
        assertEquals(10, result.column("TEST_COLUMN").getSize());

        assertNotNull(result.primaryKey());
        assertEquals(1, result.primaryKey().size());
        assertEquals("TEST_PK", result.primaryKey().get(0).name());
        return result;
    }

    private TableMetaData createTableMetaMultiPkData(
    ) throws SQLException {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(true);
        when(tableResultSet.getString("TABLE_NAME")).thenReturn(table.name());
        when(databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(tableResultSet);

        final ResultSet pkResultSet = mock(ResultSet.class);
        when(pkResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK").thenReturn("TEST_PK2");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK").thenReturn("TEST_PK2").thenReturn("TEST_COLUMN");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(Boolean.TRUE).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR).thenReturn(Types.VARCHAR).thenReturn(Types.VARCHAR);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(10).thenReturn(10);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        // When
        final TableMetaData result = databaseProvider.tableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(3, result.columns().size());

        assertEquals("TEST_PK", result.column("TEST_PK").name());
        assertTrue(result.column("TEST_PK").isNullable());
        assertEquals(Types.VARCHAR, result.column("TEST_PK").getDataType());
        assertEquals(10, result.column("TEST_PK").getSize());

        assertEquals("TEST_PK2", result.column("TEST_PK2").name());
        assertTrue(result.column("TEST_PK2").isNullable());
        assertEquals(Types.VARCHAR, result.column("TEST_PK2").getDataType());
        assertEquals(10, result.column("TEST_PK2").getSize());

        assertEquals("TEST_COLUMN", result.column("TEST_COLUMN").name());
        assertFalse(result.column("TEST_COLUMN").isNullable());
        assertEquals(Types.VARCHAR, result.column("TEST_COLUMN").getDataType());
        assertEquals(10, result.column("TEST_COLUMN").getSize());

        assertNotNull(result.primaryKey());
        assertEquals(2, result.primaryKey().size());
        assertEquals("TEST_PK", result.primaryKey().getFirst().name());
        assertEquals("TEST_PK2", result.primaryKey().getLast().name());
        return result;
    }

    @Test
    void insert() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        tableMetaData.column("TEST_PK").setAutoIncrement(true);
        final ColumnMetaData columnMetaData = tableMetaData.column("TEST_COLUMN");
        columnMetaData.setGenerator(new DefaultSequenceColumnValueGenerator("TEST_SEQUENCE"));
        final ColumnValue columnValue1 = new ColumnValue(columnMetaData.toColumn(), "testValue1");
        final ColumnValue columnValue2 = new ColumnValue(columnMetaData.toColumn(), null);
        final RowValue rowValue1 = new RowValue(List.of(columnValue1));
        final RowValue rowValue2 = new RowValue(List.of(columnValue2));

        final Insert insert = new Insert(tableMetaData.toTable(), List.of(columnMetaData.toColumn()), List.of(rowValue1, rowValue2), true);

        when(typeConverter.convert("testValue1", Types.VARCHAR)).thenReturn("testValue1");

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject(tableMetaData.primaryKey().getFirst().name())).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString(), any(String[].class))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertEquals(1, result.generatedKeys().size());
        assertEquals("testValue", result.generatedKeys().get(tableMetaData.primaryKey().getFirst()));
    }

    @Test
    void insert_noSchema() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl("");
        table.column("TEST_PK").setAutoIncrement(true);
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column.toColumn(), "testValue");
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table.toTable(), rowValue, true);

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject(table.primaryKey().getFirst().name())).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString(), any(String[].class))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertEquals(1, result.generatedKeys().size());
        assertEquals("testValue", result.generatedKeys().get(table.primaryKey().getFirst()));
    }

    @Test
    void insert_noAffectedRows() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column.toColumn(), "testValue");
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table.toTable(), List.of(rowValue), true);

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        when(connection.prepareStatement(anyString(), any(String[].class))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(0, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertTrue(result.generatedKeys().isEmpty());
    }

    @Test
    void insert_noGeneratedKeys() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column.toColumn(), "testValue");
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table.toTable(), List.of(column.toColumn()), List.of(rowValue), true);

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString(), any(String[].class))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertTrue(result.generatedKeys().isEmpty());
    }

    @Test
    void insert_nullValue_notNullColumn() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column.toColumn(), null);
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table.toTable(), List.of(column.toColumn()), List.of(rowValue), true);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> databaseProvider.insert(insert, transactionManager));
    }

    @Test
    void update() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue1 = new ColumnValue(column.toColumn(), "testValue");
        final ColumnValue columnValue2 = new ColumnValue(column.toColumn(), "testValue");
        final Condition condition1 = new Condition(column.toColumn(), Operator.EQ, "conditionValue");
        final Condition condition2 = new Condition(column.toColumn(), Operator.IS_NOT_NULL);
        final Condition condition3 = new Condition(column.toColumn(), Operator.IS_NULL);

        final Update update = new Update(table.toTable(), List.of(columnValue1, columnValue2), List.of(condition1, condition2, condition3));

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.update(update, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
    }

    @Test
    void update_noSchema_noConditions() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl("");
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column.toColumn(), "testValue");

        final Update update = new Update(table.toTable(), List.of(columnValue), Collections.emptyList());

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.update(update, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
    }

    @Test
    void select() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), "t1");
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();

        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                List.of(new Join(table, List.of(new Condition(column, Operator.USING, null),
                        new Condition(column, Operator.EQ, "TEST_VALUE")))),
                List.of(new OrderBy(column, true)),
                List.of(new Condition(column, Operator.EQ, "TEST_VALUE")),
                Optional.of(new Limit(Optional.of(10), Optional.of(20))));

        when(typeConverter.convert("TEST_VALUE", Types.VARCHAR)).thenReturn("TEST_VALUE");
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn(column.name());
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet.getObject(1)).thenReturn("dbValue");

        when(typeConverter.convert("dbValue", Types.VARCHAR)).thenReturn("dbValue");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        List<Row> result = databaseProvider.select(select, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        final Row row = result.get(0);
        assertNotNull(row.column(column.name()));
    }

    @Test
    void select_emptyResult() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name());
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();

        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final List<Row> result = databaseProvider.select(select, transactionManager);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTypeConverter() {
        assertEquals(typeConverter, databaseProvider.getTypeConverter());
    }

    @Test
    void prepareSql() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), "t1");
        final Column column1 = tableMetaData.column("TEST_PK").toColumn().as("col1");
        column1.table().setAlias("t1");
        final Column column2 = tableMetaData.column("TEST_COLUMN").toColumn().as("col2");
        column2.table().setAlias("t1");
        final ColumnIdentifierGenerator columnIdentifierGenerator = new ColumnIdentifierGenerator();

        final Select select = new Select(
                table,
                List.of(new SelectColumn(column1, columnIdentifierGenerator), new SelectColumn(column2, columnIdentifierGenerator)),
                List.of(new Join(table, List.of(new Condition(column2, Operator.EQ, "TEST_VALUE")))),
                List.of(new OrderBy(column1, true)),
                List.of(new Condition(column2, Operator.EQ, "TEST_VALUE"),
                        new Condition(column2, Operator.NEQ, "OTHER_VALUE")),
                Optional.of(new Limit(Optional.of(10), Optional.of(20))));

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals("SELECT t1.TEST_PK AS col1, t1.TEST_COLUMN AS col2 FROM TEST_SCHEMA.TEST_TABLE AS t1 JOIN TEST_SCHEMA.TEST_TABLE AS t1 ON t1.TEST_COLUMN = ? WHERE t1.TEST_COLUMN = ? AND t1.TEST_COLUMN <> ? ORDER BY t1.TEST_PK ASC LIMIT 10 OFFSET 20", result);
    }

    @Test
    void prepareSql_selectAllColumns() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name());

        final Select select = new Select(
                table,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        assertEquals("SELECT * FROM TEST_SCHEMA.TEST_TABLE", result);
    }

    @Test
    void mapOperator_eq() {
        // Given
        final Operator operator = Operator.EQ;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("=", result);
    }

    @Test
    void mapOperator_neq() {
        // Given
        final Operator operator = Operator.NEQ;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("<>", result);
    }

    @Test
    void mapOperator_gt() {
        // Given
        final Operator operator = Operator.GT;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals(">", result);
    }

    @Test
    void mapOperator_gte() {
        // Given
        final Operator operator = Operator.GTE;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals(">=", result);
    }

    @Test
    void mapOperator_lt() {
        // Given
        final Operator operator = Operator.LT;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("<", result);
    }

    @Test
    void mapOperator_lte() {
        // Given
        final Operator operator = Operator.LTE;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("<=", result);
    }

    @Test
    void mapOperator_in() {
        // Given
        final Operator operator = Operator.IN;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("IN", result);
    }

    @Test
    void mapOperator_isNull() {
        // Given
        final Operator operator = Operator.IS_NULL;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("IS NULL", result);
    }

    @Test
    void mapOperator_isNotNull() {
        // Given
        final Operator operator = Operator.IS_NOT_NULL;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("IS NOT NULL", result);
    }

    @Test
    void mapOperator_using() {
        // Given
        final Operator operator = Operator.USING;

        // When
        final String result = databaseProvider.mapOperator(operator);

        // Then
        assertEquals("USING", result);
    }

    @Test
    void createCondition() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.EQ, "testValue");

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_isNull() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.IS_NULL);

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN IS NULL", result);
    }

    @Test
    void createCondition_isNotNull() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.IS_NOT_NULL);

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN IS NOT NULL", result);
    }

    @Test
    void createCondition_using() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.USING, null);

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("USING (TEST_COLUMN)", result);
    }

    @Test
    void createCondition_withTableAlias() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        column.table().setAlias("t1");
        final Condition condition = new Condition(column, Operator.EQ, "testValue");

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("t1.TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_columnComparison() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Column left = tableMetaData.column("TEST_COLUMN").toColumn();
        left.table().setAlias("t1");
        final Column right = tableMetaData.column("TEST_PK").toColumn();
        right.table().setAlias("t2");
        final Condition condition = new Condition(left, Operator.EQ, right);

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("t1.TEST_COLUMN = t2.TEST_PK", result);
    }

    @Test
    void prepareStatement() throws Exception {
        // Given
        mockTransactionManager();
        final Object objectVal = new Object();
        final byte[] blobBytes = "blob-data".getBytes();
        final byte[] binaryBytes = "binary-data".getBytes();
        final List<AbstractDatabaseProvider.BindValue> bindValues = List.of(
                new AbstractDatabaseProvider.BindValue(123, Types.INTEGER),
                new AbstractDatabaseProvider.BindValue(12345L, Types.BIGINT),
                new AbstractDatabaseProvider.BindValue((short) 3, Types.SMALLINT),
                new AbstractDatabaseProvider.BindValue(123.45D, Types.DOUBLE),
                new AbstractDatabaseProvider.BindValue(123.45F, Types.FLOAT),
                new AbstractDatabaseProvider.BindValue(BigDecimal.valueOf(234L), Types.DECIMAL),
                new AbstractDatabaseProvider.BindValue(true, Types.BOOLEAN),
                new AbstractDatabaseProvider.BindValue("Hello World!", Types.VARCHAR),
                new AbstractDatabaseProvider.BindValue(Timestamp.valueOf("2021-01-01 00:00:00"), Types.TIMESTAMP),
                new AbstractDatabaseProvider.BindValue(blobBytes, Types.BLOB),
                new AbstractDatabaseProvider.BindValue(binaryBytes, Types.VARBINARY),
                new AbstractDatabaseProvider.BindValue("blob-as-string", Types.BLOB),
                new AbstractDatabaseProvider.BindValue(null, Types.NUMERIC),
                new AbstractDatabaseProvider.BindValue(objectVal, Types.OTHER)
        );
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql("SELECT * FROM TEST_TABLE", bindValues);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, false, tableMetaDataImpl(), transactionManager);

        // Then
        verify(result).setInt(1, 123);
        verify(result).setLong(2, 12345L);
        verify(result).setShort(3, (short) 3);
        verify(result).setDouble(4, 123.45D);
        verify(result).setFloat(5, 123.45F);
        verify(result).setBigDecimal(6, BigDecimal.valueOf(234L));
        verify(result).setBoolean(7, true);
        verify(result).setString(8, "Hello World!");
        verify(result).setTimestamp(9, Timestamp.valueOf("2021-01-01 00:00:00"));

        final ArgumentCaptor<InputStream> blobStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(result).setBinaryStream(eq(10), blobStreamCaptor.capture());
        assertArrayEquals(blobBytes, blobStreamCaptor.getValue().readAllBytes());

        verify(result).setBytes(11, binaryBytes);
        verify(result).setString(12, "blob-as-string");
        verify(result).setNull(13, Types.NUMERIC);
        verify(result).setObject(14, objectVal, Types.OTHER);
        verify(result).setBinaryStream(eq(10), any(InputStream.class));
        verify(result).setBytes(11, binaryBytes);
        verify(result).setString(12, "blob-as-string");
        verify(result).setNull(13, Types.NUMERIC);
        verify(result).setObject(14, objectVal, Types.OTHER);
    }

    @Test
    void prepareStatement_nullBindValueEntry() throws Exception {
        // Given
        mockTransactionManager();
        final List<AbstractDatabaseProvider.BindValue> bindValues = new ArrayList<>();
        bindValues.add(null);
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql("SELECT * FROM TEST_TABLE WHERE TEST_COLUMN = ?", bindValues);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, false, tableMetaDataImpl(), transactionManager);

        // Then
        verify(result).setString(1, null);
    }

    @Test
    void prepareStatement_emptyBindValues() throws Exception {
        // Given
        mockTransactionManager();
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql(
                "SELECT * FROM TEST_TABLE",
                Collections.emptyList());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, false, tableMetaDataImpl(), transactionManager);

        // Then
        assertSame(preparedStatement, result);
    }

    @Test
    void prepareStatement_returnGeneratedKeys() throws Exception {
        // Given
        mockTransactionManager();
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql(
                "INSERT INTO TEST_TABLE(TEST_COLUMN) VALUES (?)",
                List.of(new AbstractDatabaseProvider.BindValue("value", Types.VARCHAR)));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(eq(preparedSql.sql()), eq(new String[]{"TEST_PK"}))).thenReturn(preparedStatement);
        final TableMetaData tableMetaData = tableMetaDataImpl();
        tableMetaData.column("TEST_PK").setAutoIncrement(true);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, true, tableMetaData, transactionManager);

        // Then
        assertSame(preparedStatement, result);
        verify(result).setString(1, "value");
    }

    @Test
    void prepareRow_nullableColumnWithNullValue() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData nullableColumn = tableMetaData.column("TEST_PK");
        final RowValue rowValue = new RowValue(List.of(new ColumnValue(nullableColumn.toColumn(), null)));

        // When
        final AbstractDatabaseProvider.PreparedRow preparedRow = databaseProvider.prepareRow(rowValue, mock(ConnectionProvider.class));

        // Then
        assertNotNull(preparedRow);
        assertTrue(preparedRow.valueSpecifiers().isEmpty());
        assertTrue(preparedRow.bindValues().isEmpty());
    }

    @Test
    void prepareRow_autoIncrementColumnWithNullValue() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        column.setAutoIncrement(true);
        final RowValue rowValue = new RowValue(List.of(new ColumnValue(column.toColumn(), null)));

        // When
        final AbstractDatabaseProvider.PreparedRow preparedRow = databaseProvider.prepareRow(rowValue, mock(ConnectionProvider.class));

        // Then
        assertNotNull(preparedRow);
        assertTrue(preparedRow.valueSpecifiers().isEmpty());
        assertTrue(preparedRow.bindValues().isEmpty());
    }

    @Test
    void tableMetaData_usesCache() throws Exception {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(true);
        when(tableResultSet.getString("TABLE_NAME")).thenReturn(table.name());
        when(databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(tableResultSet);

        final ResultSet pkResultSet = mock(ResultSet.class);
        when(pkResultSet.next()).thenReturn(true).thenReturn(false);
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true).thenReturn(false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(Boolean.TRUE);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(10);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        // When
        final TableMetaData first = databaseProvider.tableMetaData(table, transactionManager);
        final TableMetaData second = databaseProvider.tableMetaData(table, transactionManager);

        // Then
        assertSame(first, second);
        verify(connection, times(1)).getMetaData();
    }

    @Test
    void tableMetaData_wrapsSqlException() throws Exception {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        when(connection.getMetaData()).thenThrow(new SQLException("boom"));

        // When
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> databaseProvider.tableMetaData(table, transactionManager));

        // Then
        assertEquals("Failed to get table metadata for table: " + table, exception.getMessage());
        assertInstanceOf(SQLException.class, exception.getCause());
    }

    @Test
    void verifySchemaAndTableExists_tableNotFound() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE_NOT_FOUND");
        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(false);
        when(databaseMetaData.getTables(eq(table.catalog()), eq(table.schema()), eq(table.name()), any(String[].class))).thenReturn(tableResultSet);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> databaseProvider.verifySchemaAndTableExists(table, databaseMetaData));
    }

    @Test
    void verifySchemaAndTableExists_schemaNotFound() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE_NOT_FOUND");
        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(false);
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> databaseProvider.verifySchemaAndTableExists(table, databaseMetaData));
    }

    @Test
    void verifySchemaAndTableExists_success() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(true);
        when(tableResultSet.getString("TABLE_NAME")).thenReturn(table.name());
        when(databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(tableResultSet);

        // When/Then
        databaseProvider.verifySchemaAndTableExists(table, databaseMetaData);
    }

    @Test
    void quoteIdentifier_reservedKeyword() {
        // Given
        final String identifier = "TABLE";

        // When
        final String result = databaseProvider.quoteIdentifier(identifier);

        // Then
        assertEquals("\"TABLE\"", result);
    }

    @Test
    void quoteIdentifier_notNeeded() {
        // Given
        final String identifier = "TEST";

        // When
        final String result = databaseProvider.quoteIdentifier(identifier);

        // Then
        assertEquals("TEST", result);
    }

    @Test
    void quoteIdentifier_null() {
        // Given
        final String identifier = null;

        // When
        final String result = databaseProvider.quoteIdentifier(identifier);

        // Then
        assertNull(result);
    }

    @Test
    void getLogger() {
        assertNotNull(databaseProvider.getLogger());
    }

    @Test
    void delete() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        final Condition condition1 = new Condition(column.toColumn(), Operator.EQ, "conditionValue");
        final Condition condition2 = new Condition(column.toColumn(), Operator.IS_NULL);

        final org.litebridgedb.db.spi.update.Delete delete = new org.litebridgedb.db.spi.update.Delete(tableMetaData.toTable(), List.of(condition1, condition2));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(2);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.delete(delete, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(2, result.rowsAffected());
    }

    @Test
    void delete_noConditions() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();

        final org.litebridgedb.db.spi.update.Delete delete = new org.litebridgedb.db.spi.update.Delete(tableMetaData.toTable(), Collections.emptyList());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.delete(delete, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(5, result.rowsAffected());
    }

    @Test
    void createMathOperation() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        final org.litebridgedb.db.spi.math.MathOperation mathOperation = new org.litebridgedb.db.spi.math.MathOperation(
                org.litebridgedb.db.spi.math.MathOperation.Operator.ADD,
                10
        );

        when(typeConverter.convert(10, column.getDataType())).thenReturn(10);

        // When
        final String result = databaseProvider.createMathOperation(column, mathOperation);

        // Then
        assertEquals("TEST_COLUMN + 10", result);
    }

    @Test
    void update_withMathOperation() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        final org.litebridgedb.db.spi.math.MathOperation mathOperation = new org.litebridgedb.db.spi.math.MathOperation(
                org.litebridgedb.db.spi.math.MathOperation.Operator.SUBTRACT,
                5
        );
        final ColumnValue columnValue = new ColumnValue(column.toColumn(), mathOperation);

        final Update update = new Update(tableMetaData.toTable(), List.of(columnValue), Collections.emptyList());

        when(typeConverter.convert(5, column.getDataType())).thenReturn(5);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.update(update, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
    }

    @Test
    void prepareSql_withOrderByDesc() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name());
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();

        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                List.of(new OrderBy(column, false)),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        System.out.println(result);
        assertTrue(result.contains("ORDER BY TEST_TABLE.TEST_COLUMN DESC"));
    }

    @Test
    void prepareSql_withLimitNoOffset() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name());

        final Select select = new Select(
                table,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.of(new Limit(Optional.of(10), Optional.empty())));

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        assertTrue(result.endsWith("LIMIT 10"));
    }

    @Test
    void prepareSql_withOffsetNoLimit() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name());

        final Select select = new Select(
                table,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.of(new Limit(Optional.empty(), Optional.of(20))));

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        assertTrue(result.endsWith("OFFSET 20"));
    }

    @Test
    void createJoin_withNoAlias() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name());
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.EQ, "testValue");

        final Join join = new Join(table, List.of(condition));

        // When
        final String result = databaseProvider.createJoin(join);

        // Then
        assertEquals(" JOIN TEST_SCHEMA.TEST_TABLE ON TEST_TABLE.TEST_COLUMN = ?", result);
    }

    @Test
    void createJoin_multipleConditions() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), "t2");
        final Column column1 = tableMetaData.column("TEST_PK").toColumn();
        final Column column2 = tableMetaData.column("TEST_COLUMN").toColumn();
        final Condition condition1 = new Condition(column1, Operator.EQ, "value1");
        final Condition condition2 = new Condition(column2, Operator.NEQ, "value2");

        final Join join = new Join(table, List.of(condition1, condition2));

        // When
        final String result = databaseProvider.createJoin(join);

        // Then
        assertTrue(result.contains("AND"));
        assertTrue(result.contains("TEST_TABLE.TEST_PK = ?"));
        assertTrue(result.contains("TEST_TABLE.TEST_COLUMN <> ?"));
    }

    @Test
    void createCondition_withOperatorGT() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.GT, "testValue");

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN > ?", result);
    }

    @Test
    void createCondition_withOperatorGTE() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.GTE, "testValue");

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN >= ?", result);
    }

    @Test
    void createCondition_withOperatorLT() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.LT, "testValue");

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN < ?", result);
    }

    @Test
    void createCondition_withOperatorLTE() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.LTE, "testValue");

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN <= ?", result);
    }

    @Test
    void createCondition_withOperatorIN() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.IN, List.of("value1", "value2"));

        // When
        final String result = databaseProvider.createCondition(condition, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN ?", result);
    }

    @Test
    void appendTable_withTableMetaData() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final StringBuilder sql = new StringBuilder("SELECT * FROM ");

        // When
        databaseProvider.appendTable(sql, tableMetaData);

        // Then
        assertEquals("SELECT * FROM TEST_SCHEMA.TEST_TABLE", sql.toString());
    }

    @Test
    void appendTable_withSchemaAndName() throws Exception {
        // Given
        final StringBuilder sql = new StringBuilder("SELECT * FROM ");

        // When
        databaseProvider.appendTable(sql, "TEST_SCHEMA", "TEST_TABLE");

        // Then
        assertEquals("SELECT * FROM TEST_SCHEMA.TEST_TABLE", sql.toString());
    }

    @Test
    void appendTable_withoutSchema() throws Exception {
        // Given
        final StringBuilder sql = new StringBuilder("SELECT * FROM ");

        // When
        databaseProvider.appendTable(sql, "", "TEST_TABLE");

        // Then
        assertEquals("SELECT * FROM TEST_TABLE", sql.toString());
    }

    @Test
    void createColumnIdentifier_withoutAlias() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();

        // When
        final String result = databaseProvider.createColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withAlias_notIncluded() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn().as("col_alias");

        // When
        final String result = databaseProvider.createColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withTableAlias() throws Exception {
        // Given
        final Column column = tableMetaDataImpl().column("TEST_COLUMN").toColumn();
        column.table().setAlias("t1");

        // When
        final String result = databaseProvider.createColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("t1.TEST_COLUMN", result);
    }

    @Test
    void executeSqlInsert_returnGeneratedKeysFalse() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql(
                "INSERT INTO TEST_TABLE(TEST_COLUMN) VALUES (?)",
                List.of(new AbstractDatabaseProvider.BindValue("value", Types.VARCHAR)));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.executeSqlInsert(preparedSql, tableMetaData, false, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertTrue(result.generatedKeys().isEmpty());
    }

    @Test
    void getGeneratedPrimaryKeyColumns() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        tableMetaData.column("TEST_PK").setAutoIncrement(true);

        // When
        final List<ColumnMetaData> result = databaseProvider.getGeneratedPrimaryKeyColumns(tableMetaData);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST_PK", result.get(0).name());
    }

    @Test
    void getGeneratedPrimaryKeyColumns_noneAutoIncrement() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();

        // When
        final List<ColumnMetaData> result = databaseProvider.getGeneratedPrimaryKeyColumns(tableMetaData);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractGeneratedKeys_multiplePrimaryKeys() throws Exception {
        // Given
        mockTransactionManager();
        // Add a second primary key column
        final TableMetaData tableMetaData = createTableMetaMultiPkData();
        tableMetaData.column("TEST_PK").setAutoIncrement(true);
        tableMetaData.column("TEST_PK2").setAutoIncrement(true);

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getObject("TEST_PK")).thenReturn("pk1Value");
        when(resultSet.getObject("TEST_PK2")).thenReturn("pk2Value");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

        // When
        final Map<ColumnMetaData, Object> result = databaseProvider.extractGeneratedKeys(tableMetaData, preparedStatement);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(tableMetaData.column("TEST_PK")));
        assertTrue(result.containsKey(tableMetaData.column("TEST_PK2")));
    }

    @Test
    void select_withColumnNotInMetadata() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name());
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();

        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn(column.name());
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("UNKNOWN_COLUMN");
        when(resultSetMetaData.getSchemaName(2)).thenReturn("TEST_SCHEMA");
        when(resultSetMetaData.getTableName(2)).thenReturn("TEST_TABLE");
        when(resultSetMetaData.getColumnName(2)).thenReturn("UNKNOWN_COLUMN");
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet.getObject(1)).thenReturn("dbValue1");
        when(resultSet.getObject(2)).thenReturn("dbValue2");

        when(typeConverter.convert("dbValue1", Types.VARCHAR)).thenReturn("dbValue1");
        when(typeConverter.convert("dbValue2", Types.VARCHAR)).thenReturn("dbValue2");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final List<Row> result = databaseProvider.select(select, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        final Row row = result.get(0);
        assertNotNull(row.column(column.name()));
        assertNotNull(row.column("UNKNOWN_COLUMN"));
    }

    @Test
    void getColumnMetaData() throws Exception {
        // Given
        mockTransactionManagerNoConnectionStub();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("COL1").thenReturn("COL2");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(true).thenReturn(false);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR).thenReturn(Types.INTEGER);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(50).thenReturn(10);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        // When
        final List<ColumnMetaData> result = databaseProvider.getColumnMetaData(table, databaseMetaData);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("COL1", result.get(0).name());
        assertEquals("COL2", result.get(1).name());
    }

    @Test
    void getPrimaryKeyColumnNames() throws Exception {
        // Given
        mockTransactionManagerNoConnectionStub();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);

        final ResultSet pkResultSet = mock(ResultSet.class);
        when(pkResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("PK1").thenReturn("PK2");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        // When
        final List<String> result = databaseProvider.getPrimaryKeyColumnNames(table, databaseMetaData);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PK1", result.get(0));
        assertEquals("PK2", result.get(1));
    }

    @Test
    void fetchTableMetaData() throws Exception {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(true);
        when(tableResultSet.getString("TABLE_NAME")).thenReturn(table.name());
        when(databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(tableResultSet);

        final ResultSet pkResultSet = mock(ResultSet.class);
        when(pkResultSet.next()).thenReturn(true).thenReturn(false);
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true).thenReturn(false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(false);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(10);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        // When
        final TableMetaData result = databaseProvider.fetchTableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE", result.name());
        assertEquals("TEST_SCHEMA", result.schema());
    }

    @Test
    void createAlias() {
        // Given
        final String alias = "my_alias";

        // When
        final String result = databaseProvider.createAlias(alias);

        // Then
        assertEquals("AS my_alias", result);
    }

    @Test
    void appendLimitClause_bothLimitAndOffset() {
        // Given
        final Limit limit = new Limit(Optional.of(100), Optional.of(50));
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        databaseProvider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST LIMIT 100 OFFSET 50", sql.toString());
    }

    @Test
    void appendLimitClause_onlyLimit() {
        // Given
        final Limit limit = new Limit(Optional.of(100), Optional.empty());
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        databaseProvider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST LIMIT 100", sql.toString());
    }

    @Test
    void appendLimitClause_onlyOffset() {
        // Given
        final Limit limit = new Limit(Optional.empty(), Optional.of(50));
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        databaseProvider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST OFFSET 50", sql.toString());
    }

    @Test
    void appendLimitClause_neitherLimitNorOffset() {
        // Given
        final Limit limit = new Limit(Optional.empty(), Optional.empty());
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        databaseProvider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST", sql.toString());
    }

    @Test
    void ensureColumnMetaData() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();

        // When
        final ColumnMetaData result = databaseProvider.ensureColumnMetaData(column, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals("TEST_COLUMN", result.name());
    }

    @Test
    void prepareSql_insert_withMultipleRows() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        final ColumnValue columnValue1 = new ColumnValue(column.toColumn(), "value1");
        final ColumnValue columnValue2 = new ColumnValue(column.toColumn(), "value2");
        final RowValue rowValue1 = new RowValue(List.of(columnValue1));
        final RowValue rowValue2 = new RowValue(List.of(columnValue2));

        final Insert insert = new Insert(tableMetaData.toTable(), List.of(column.toColumn()), List.of(rowValue1, rowValue2), false);

        when(typeConverter.convert("value1", Types.VARCHAR)).thenReturn("value1");
        when(typeConverter.convert("value2", Types.VARCHAR)).thenReturn("value2");

        // When
        final AbstractDatabaseProvider.PreparedSql result = databaseProvider.prepareSql(insert, transactionManager);

        // Then
        assertNotNull(result);
        assertTrue(result.sql().contains("VALUES"));
        assertEquals(2, result.bindValues().size());
    }

    @Test
    void prepareRow_withSequenceAndNonNullValue() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        column.setGenerator(new DefaultSequenceColumnValueGenerator("TEST_SEQUENCE"));
        final RowValue rowValue = new RowValue(List.of(new ColumnValue(column.toColumn(), "testValue")));

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final AbstractDatabaseProvider.PreparedRow result = databaseProvider.prepareRow(rowValue, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.valueSpecifiers().size());
        assertEquals("?", result.valueSpecifiers().get(0));
        assertEquals(1, result.bindValues().size());
    }

    @Test
    void createPreparedStatementUsingConnection_withoutReturnGeneratedKeys() throws Exception {
        // Given
        mockTransactionManager();
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql(
                "SELECT * FROM TEST_TABLE",
                Collections.emptyList());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.createPreparedStatementUsingConnection(preparedSql, false, tableMetaData, connection);

        // Then
        assertSame(preparedStatement, result);
    }

    @Test
    void createPreparedStatementUsingConnection_withReturnGeneratedKeys() throws Exception {
        // Given
        mockTransactionManager();
        final TableMetaData tableMetaData = tableMetaDataImpl();
        tableMetaData.column("TEST_PK").setAutoIncrement(true);
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql(
                "INSERT INTO TEST_TABLE(TEST_COLUMN) VALUES (?)",
                List.of(new AbstractDatabaseProvider.BindValue("value", Types.VARCHAR)));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(eq(preparedSql.sql()), eq(new String[]{"TEST_PK"}))).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.createPreparedStatementUsingConnection(preparedSql, true, tableMetaData, connection);

        // Then
        assertSame(preparedStatement, result);
    }

    @Test
    void getSequenceColumnValueGenerator() {
        // When
        final SequenceColumnValueGenerator result = databaseProvider.getSequenceColumnValueGenerator("test_sequence");

        // Then
        assertInstanceOf(DefaultSequenceColumnValueGenerator.class, result);
    }

    private void mockTransactionManager() throws SQLException {
        if (connection == null) {
            connection = mock(ManagedConnection.class);
            when(transactionManager.connection()).thenReturn(connection);
        }
    }

    private void mockTransactionManagerNoConnectionStub() throws SQLException {
        if (connection == null) {
            connection = mock(ManagedConnection.class);
        }
    }

    private static class TestDatabaseProvider extends AbstractDatabaseProvider {
        public TestDatabaseProvider(final TypeConverter typeConverter) {
            super(typeConverter);
        }
    }
}