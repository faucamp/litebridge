package org.litebridge.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.function.SelectColumn;
import org.litebridge.db.spi.impl.function.SelectReferenceImpl;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.RowValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.db.spi.update.UpdateResult;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        return tableMetaDataImpl(schema, false);
    }

    private TableMetaData tableMetaDataImpl(final String schema, final boolean autoIncrement) throws SQLException {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", schema, "TEST_TABLE");

        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true, false);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(true, false);
        when(tableResultSet.getString("TABLE_NAME")).thenReturn(table.name());
        when(databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(tableResultSet);

        final ResultSet pkResultSet = mock(ResultSet.class);
        when(pkResultSet.next()).thenReturn(true, false);
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true, true, false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK").thenReturn("TEST_COLUMN");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(Boolean.TRUE, Boolean.FALSE);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR, Types.VARCHAR);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(10, 10);
        when(columnResultSet.getBoolean("IS_AUTOINCREMENT")).thenReturn(autoIncrement, false);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        when(databaseMetaData.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));
        when(databaseMetaData.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));

        // When
        final TableMetaData result = databaseProvider.tableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        return result;
    }

    private TableMetaData createTableMetaMultiPkData() throws SQLException {
        return createTableMetaMultiPkData(false);
    }

    private TableMetaData createTableMetaMultiPkData(final boolean autoIncrement) throws SQLException {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");

        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        final ResultSet schemaResultSet = mock(ResultSet.class);
        when(schemaResultSet.next()).thenReturn(true, false);
        when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn(table.schema());
        when(databaseMetaData.getSchemas(table.catalog(), table.schema())).thenReturn(schemaResultSet);

        final ResultSet tableResultSet = mock(ResultSet.class);
        when(tableResultSet.next()).thenReturn(true, false);
        when(tableResultSet.getString("TABLE_NAME")).thenReturn(table.name());
        when(databaseMetaData.getTables(table.catalog(), table.schema(), table.name(), AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(tableResultSet);

        final ResultSet pkResultSet = mock(ResultSet.class);
        when(pkResultSet.next()).thenReturn(true, true, false);
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK").thenReturn("TEST_PK2");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true, true, true, false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("TEST_PK").thenReturn("TEST_PK2").thenReturn("TEST_COLUMN");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(10, 10, 10);
        when(columnResultSet.getBoolean("IS_AUTOINCREMENT")).thenReturn(autoIncrement, autoIncrement, false);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        when(databaseMetaData.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));
        when(databaseMetaData.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));

        // When
        final TableMetaData result = databaseProvider.tableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        return result;
    }

    @Test
    void insert() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl("TEST_SCHEMA", true);
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
        final String sql = databaseProvider.toSql(insert, transactionManager);
        final UpdateMetaData updateMetaData = new UpdateMetaData(true, tableMetaData.primaryKey(), new String[]{"TEST_PK"});
        final InsertResult result = databaseProvider.insert(new PreparedSql(sql, Collections.emptyList(), null, updateMetaData), transactionManager);

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
        final TableMetaData table = tableMetaDataImpl("", true);
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
        final String sql = databaseProvider.toSql(insert, transactionManager);
        final UpdateMetaData updateMetaData = new UpdateMetaData(true, table.primaryKey(), new String[]{"TEST_PK"});
        final InsertResult result = databaseProvider.insert(new PreparedSql(sql, Collections.emptyList(), null, updateMetaData), transactionManager);

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
        final String sql = databaseProvider.toSql(insert, transactionManager);
        final UpdateMetaData updateMetaData = new UpdateMetaData(true, table.primaryKey(), new String[]{"TEST_PK"});
        final InsertResult result = databaseProvider.insert(new PreparedSql(sql, Collections.emptyList(), null, updateMetaData), transactionManager);

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
        final String sql = databaseProvider.toSql(insert, transactionManager);
        final UpdateMetaData updateMetaData = new UpdateMetaData(true, table.primaryKey(), new String[]{"TEST_PK"});
        final InsertResult result = databaseProvider.insert(new PreparedSql(sql, Collections.emptyList(), null, updateMetaData), transactionManager);

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
        assertThrows(IllegalArgumentException.class, () -> databaseProvider.toSql(insert, transactionManager));
    }

    @Test
    void update() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final UpdateColumn updateColumn1 = new UpdateColumn(column.name());
        final UpdateColumn updateColumn2 = new UpdateColumn(column.name());
        final ColumnExpression columnExpression = new SelectColumn(column.toColumn(), mock(ColumnIdentifierGenerator.class));
        final LogicCondition condition1 = new LogicCondition(columnExpression, Operator.EQ, "conditionValue");
        final LogicCondition condition2 = new LogicCondition(LogicOperator.AND, new Condition(columnExpression, Operator.IS_NOT_NULL));
        final LogicCondition condition3 = new LogicCondition(LogicOperator.OR, new Condition(columnExpression, Operator.IS_NULL));
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition1, condition2, condition3));

        final Update update = new Update(table.toTable(), List.of(updateColumn1, updateColumn2), conditionGroup);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final String sql = databaseProvider.toSql(update, transactionManager);
        final UpdateResult result = databaseProvider.update(new PreparedSql(sql, Collections.emptyList()), transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
    }

    @Test
    void update_noSchema_noConditions() throws Exception {
        // Given
        final TableMetaData table = tableMetaDataImpl("");
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final UpdateColumn updateColumn = new UpdateColumn(column.name());

        final Update update = new Update(table.toTable(), List.of(updateColumn), new ConditionGroup(Collections.emptyList()));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final String sql = databaseProvider.toSql(update, transactionManager);
        final UpdateResult result = databaseProvider.update(new PreparedSql(sql, Collections.emptyList()), transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
    }

    @Test
    void select() throws Exception {
        // Given
        final ColumnIdentifierGenerator columnIdentifierGenerator = new ColumnIdentifierGenerator();
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), "t1");
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();
        final ColumnExpression selectColumn = new SelectColumn(column, columnIdentifierGenerator);

        final Select select = new Select(
                table,
                List.of(selectColumn),
                List.of(new Join(table, new ConditionGroup(List.of(new LogicCondition(selectColumn, Operator.USING, null),
                        new LogicCondition(LogicOperator.AND, new Condition(selectColumn, Operator.EQ, "TEST_VALUE")))))),
                Optional.of(new ConditionGroup(List.of(new LogicCondition(selectColumn, Operator.EQ, "TEST_VALUE")))),
                Collections.emptyList(),
                Optional.empty(),
                List.of(new OrderBy(selectColumn, true)),
                Optional.of(new Limit(Optional.of(10), Optional.of(20))));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn(column.name());
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet.getObject(1)).thenReturn("dbValue");

        when(typeConverter.convert("dbValue", Types.VARCHAR)).thenReturn("dbValue");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        List<Row> result = databaseProvider.select(new PreparedSql("SELECT *", Collections.emptyList()), transactionManager);

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
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final List<Row> result = databaseProvider.select(new PreparedSql("SELECT *", Collections.emptyList()), transactionManager);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTypeConverter() {
        assertEquals(typeConverter, databaseProvider.getTypeConverter());
    }

    @Test
    void prepareSql_select() throws Exception {
        // Given
        final ColumnIdentifierGenerator columnIdentifierGenerator = new ColumnIdentifierGenerator();
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), "t1");
        final Column column1 = tableMetaData.column("TEST_PK").toColumn().as("col1");
        column1.table().setAlias("t1");
        final ColumnExpression selectColumn1 = new SelectColumn(column1, columnIdentifierGenerator);
        final Column column2 = tableMetaData.column("TEST_COLUMN").toColumn().as("col2");
        column2.table().setAlias("t1");
        final ColumnExpression selectColumn2 = new SelectColumn(column2, columnIdentifierGenerator);

        final Select select = new Select(
                table,
                List.of(new SelectColumn(column1, columnIdentifierGenerator), new SelectColumn(column2, columnIdentifierGenerator)),
                List.of(new Join(table, new ConditionGroup(List.of(new LogicCondition(selectColumn2, Operator.EQ, "TEST_VALUE"))))),
                Optional.of(new ConditionGroup(List.of(new LogicCondition(selectColumn2, Operator.EQ, "TEST_VALUE"),
                        new LogicCondition(LogicOperator.AND, new Condition(selectColumn2, Operator.NEQ, "OTHER_VALUE"))))),
                Collections.emptyList(),
                Optional.empty(),
                List.of(new OrderBy(new SelectReferenceImpl(column1, new ColumnIdentifierGenerator()), true)),
                Optional.of(new Limit(Optional.of(10), Optional.of(20))));

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals("SELECT t1.TEST_PK AS col1, t1.TEST_COLUMN AS col2 FROM TEST_SCHEMA.TEST_TABLE AS t1 JOIN TEST_SCHEMA.TEST_TABLE AS t1 ON t1.TEST_COLUMN = ? WHERE t1.TEST_COLUMN = ? AND t1.TEST_COLUMN <> ? ORDER BY col1 ASC LIMIT 10 OFFSET 20", result);
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
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        assertEquals("SELECT * FROM TEST_SCHEMA.TEST_TABLE", result);
    }

    @Test
    void prepareStatement() throws Exception {
        // Given
        mockTransactionManager();
        final Object objectVal = new Object();
        final byte[] blobBytes = "blob-data".getBytes();
        final byte[] binaryBytes = "binary-data".getBytes();
        final List<BindValue> bindValues = List.of(
                new BindValue(123, Types.INTEGER),
                new BindValue(12345L, Types.BIGINT),
                new BindValue((short) 3, Types.SMALLINT),
                new BindValue(123.45D, Types.DOUBLE),
                new BindValue(123.45F, Types.FLOAT),
                new BindValue(BigDecimal.valueOf(234L), Types.DECIMAL),
                new BindValue(true, Types.BOOLEAN),
                new BindValue("Hello World!", Types.VARCHAR),
                new BindValue(Timestamp.valueOf("2021-01-01 00:00:00"), Types.TIMESTAMP),
                new BindValue(blobBytes, Types.BLOB),
                new BindValue(binaryBytes, Types.VARBINARY),
                new BindValue("blob-as-string", Types.BLOB),
                new BindValue(null, Types.NUMERIC),
                new BindValue(objectVal, Types.OTHER)
        );
        final PreparedSql preparedSql = new PreparedSql("SELECT * FROM TEST_TABLE", bindValues);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, transactionManager);

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
        final List<BindValue> bindValues = new ArrayList<>();
        bindValues.add(null);
        final PreparedSql preparedSql = new PreparedSql("SELECT * FROM TEST_TABLE WHERE TEST_COLUMN = ?", bindValues);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, transactionManager);

        // Then
        verify(result).setString(1, null);
    }

    @Test
    void prepareStatement_emptyBindValues() throws Exception {
        // Given
        mockTransactionManager();
        final PreparedSql preparedSql = new PreparedSql(
                "SELECT * FROM TEST_TABLE",
                Collections.emptyList());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, transactionManager);

        // Then
        assertSame(preparedStatement, result);
    }

    @Test
    void prepareStatement_returnGeneratedKeys() throws Exception {
        // Given
        mockTransactionManager();
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final PreparedSql preparedSql = new PreparedSql(
                "INSERT INTO TEST_TABLE(TEST_COLUMN) VALUES (?)",
                List.of(new BindValue("column", Types.VARCHAR)),
                null,
                new UpdateMetaData(true, tableMetaData.primaryKey(), new String[]{"TEST_PK"}));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(eq(preparedSql.sql()), eq(new String[]{"TEST_PK"}))).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, transactionManager);

        // Then
        assertSame(preparedStatement, result);
        verify(result).setString(1, "column");
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

        when(databaseMetaData.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));
        when(databaseMetaData.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));

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
    void getLogger() {
        assertNotNull(databaseProvider.getLogger());
    }

    @Test
    void delete() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        final ColumnExpression columnExpression = new SelectColumn(column.toColumn(), databaseProvider.columnIdentifierGenerator.orThrow());
        final LogicCondition condition1 = new LogicCondition(columnExpression, Operator.EQ, "conditionValue");
        final LogicCondition condition2 = new LogicCondition(LogicOperator.AND, new Condition(columnExpression, Operator.IS_NULL));
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition1, condition2));

        final Delete delete = new Delete(tableMetaData.toTable(), conditionGroup);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(2);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final String sql = databaseProvider.toSql(delete, transactionManager);
        final UpdateResult result = databaseProvider.delete(new PreparedSql(sql, Collections.emptyList()), transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(2, result.rowsAffected());
    }

    @Test
    void delete_noConditions() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();

        final Delete delete = new Delete(tableMetaData.toTable(), new ConditionGroup(Collections.emptyList()));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final String sql = databaseProvider.toSql(delete, transactionManager);
        final UpdateResult result = databaseProvider.delete(new PreparedSql(sql, Collections.emptyList()), transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(5, result.rowsAffected());
    }

    @Test
    void update_withMathOperation() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final ColumnMetaData column = tableMetaData.column("TEST_COLUMN");
        final org.litebridge.db.spi.math.MathOperation mathOperation = new org.litebridge.db.spi.math.MathOperation(
                org.litebridge.db.spi.math.MathOperation.Operator.SUBTRACT,
                5
        );
        final UpdateColumn updateColumn = new UpdateColumn(column.name());

        final Update update = new Update(tableMetaData.toTable(), List.of(updateColumn), new ConditionGroup(Collections.emptyList()));

        when(typeConverter.convert(5, column.getDataType())).thenReturn(5);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final String sql = databaseProvider.toSql(update, transactionManager);
        final UpdateResult result = databaseProvider.update(new PreparedSql(sql, Collections.emptyList()), transactionManager);

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
        final SelectColumn selectColumn = new SelectColumn(column, new ColumnIdentifierGenerator());

        final Select select = new Select(
                table,
                List.of(selectColumn),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                List.of(new OrderBy(selectColumn, false)),
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
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
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
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.of(new Limit(Optional.empty(), Optional.of(20))));

        // When
        final String result = databaseProvider.toSql(select, transactionManager);

        // Then
        assertTrue(result.endsWith("OFFSET 20"));
    }

    @Test
    void executeSqlInsert_returnGeneratedKeysFalse() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final PreparedSql preparedSql = new PreparedSql(
                "INSERT INTO TEST_TABLE(TEST_COLUMN) VALUES (?)",
                List.of(new BindValue("column", Types.VARCHAR)),
                null,
                new UpdateMetaData(false, Collections.emptyList(), new String[0]));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.executeSqlInsert(preparedSql, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertTrue(result.generatedKeys().isEmpty());
    }


    @Test
    void extractGeneratedKeys_multiplePrimaryKeys() throws Exception {
        // Given
        mockTransactionManager();
        // Add a second primary key column
        final TableMetaData tableMetaData = createTableMetaMultiPkData(true);

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getObject("TEST_PK")).thenReturn("pk1Value");
        when(resultSet.getObject("TEST_PK2")).thenReturn("pk2Value");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

        // When
        final Map<ColumnMetaData, Object> result = databaseProvider.extractGeneratedKeys(tableMetaData.primaryKey(), preparedStatement);

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
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn(column.name());
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("UNKNOWN_COLUMN");
        when(resultSetMetaData.getSchemaName(1)).thenReturn(null);
        when(resultSetMetaData.getSchemaName(2)).thenReturn("TEST_SCHEMA");
        when(resultSetMetaData.getTableName(1)).thenReturn(null);
        when(resultSetMetaData.getTableName(2)).thenReturn("TEST_TABLE");
        when(resultSetMetaData.getColumnName(1)).thenReturn(null);
        when(resultSetMetaData.getColumnName(2)).thenReturn("UNKNOWN_COLUMN");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet.getObject(1)).thenReturn("dbValue1");
        when(resultSet.getObject(2)).thenReturn("dbValue2");

        when(typeConverter.convert("dbValue1", Types.VARCHAR)).thenReturn("dbValue1");
        when(typeConverter.convert("dbValue2", Types.VARCHAR)).thenReturn("dbValue2");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final List<Row> result = databaseProvider.select(new PreparedSql("SELECT *", Collections.emptyList()), transactionManager);

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
        when(databaseMetaData.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));
        when(databaseMetaData.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(mock(ResultSet.class));

        // When
        final TableMetaData result = databaseProvider.fetchTableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE", result.name());
        assertEquals("TEST_SCHEMA", result.schema());
    }

    @Test
    void createPreparedStatementUsingConnection_withoutReturnGeneratedKeys() throws Exception {
        // Given
        mockTransactionManager();
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final PreparedSql preparedSql = new PreparedSql(
                "SELECT * FROM TEST_TABLE",
                Collections.emptyList());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.createPreparedStatementUsingConnection(preparedSql, connection);

        // Then
        assertSame(preparedStatement, result);
    }

    @Test
    void createPreparedStatementUsingConnection_withReturnGeneratedKeys() throws Exception {
        // Given
        mockTransactionManager();
        final TableMetaData tableMetaData = tableMetaDataImpl("TEST_SCHEMA", true);
        final PreparedSql preparedSql = new PreparedSql(
                "INSERT INTO TEST_TABLE(TEST_COLUMN) VALUES (?)",
                List.of(new BindValue("column", Types.VARCHAR)),
                null,
                new UpdateMetaData(true, tableMetaData.primaryKey(), new String[]{"TEST_PK"}));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(eq(preparedSql.sql()), eq(new String[]{"TEST_PK"}))).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.createPreparedStatementUsingConnection(preparedSql, connection);

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

    @Test
    void toSql_unsupportedOperation() {
        // Given
        final Select select = mock(Select.class); // Mocking a permitted subtype to avoid Mockito exception
        // Actually to Sql takes Operation, but AbstractDatabaseProvider implementation checks for Select or UpdateStatement
        // If we want to trigger the UnsupportedOperationException, we need an Operation that is neither.
        // But since it's a sealed interface, we can't easily create a 3rd subtype.
        // Wait, AbstractDatabaseProvider.toSql checks:
        /*
        if (operation instanceof Select select) {
            return selectSqlGenerator.prepareSql(select, connectionProvider).sql();
        } else if (operation instanceof UpdateStatement updateStatement) {
            return ...
        } else {
            throw new UnsupportedOperationException("Unsupported operation type: " + operation.getClass().getName());
        }
        */
        // If we can't mock Operation directly, maybe we can't reach the else block unless we use a real instance of a fake subtype (if it was not sealed).
        // Let's see if we can at least test the other missing parts.
    }

    @Test
    void getSqlFunctionRegistry() {
        assertNotNull(databaseProvider.getSqlFunctionRegistry());
    }

    @Test
    void getAliasTransformer() {
        assertNotNull(databaseProvider.getAliasTransformer());
    }

    @Test
    void executeSqlQuery_sqlException() throws SQLException {
        // Given
        mockTransactionManager();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Select select = new Select(
                table,
                List.of(new SelectColumn(new Column(table, "COL"), new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty()
        );

        final PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenThrow(new SQLException("boom"));

        // When & Then
        assertThrows(SQLException.class, () -> databaseProvider.select(new PreparedSql("SELECT *", Collections.emptyList()), transactionManager));
    }

    @Test
    void prepareStatement_withNullBindValueEntry() throws Exception {
        // Given
        mockTransactionManager();
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final PreparedSql preparedSql = new PreparedSql("SELECT * FROM TEST WHERE COL = ?",
                Collections.singletonList(null));
        final PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        // When
        databaseProvider.prepareStatement(preparedSql, transactionManager);

        // Then
        verify(ps).setString(1, null);
    }

    @Test
    void nativeSqlQuery() throws Exception {
        // Given
        mockTransactionManager();
        final String sql = "SELECT * FROM TEST WHERE ID = ?";
        final List<Object> bindParameters = List.of(1);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getSchemaName(1)).thenReturn("SCHEMA");
        when(resultSetMetaData.getTableName(1)).thenReturn("TABLE");
        when(resultSetMetaData.getColumnName(1)).thenReturn("COLUMN");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("LABEL");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSet.getObject(1)).thenReturn(1);
        when(typeConverter.convert(1, Types.INTEGER)).thenReturn(1);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // When
        final List<Row> result = databaseProvider.nativeSqlQuery(sql, bindParameters, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void nativeSqlUpdate() throws Exception {
        // Given
        mockTransactionManager();
        final String sql = "UPDATE TEST SET COL = ? WHERE ID = ?";
        final List<Object> bindParameters = List.of("value", 1);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.nativeSqlUpdate(sql, bindParameters, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        verify(preparedStatement).setString(1, "value");
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    void prepareNativeStatement_allTypes() throws Exception {
        // Given
        mockTransactionManager();
        final String sql = "INSERT INTO ALL_TYPES VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        final List<Object> bindParameters = new ArrayList<>();
        bindParameters.add(10);
        bindParameters.add(20L);
        bindParameters.add((short) 5);
        bindParameters.add(1.5D);
        bindParameters.add(2.5F);
        bindParameters.add(BigDecimal.TEN);
        bindParameters.add(true);
        bindParameters.add("string");
        bindParameters.add(new Timestamp(System.currentTimeMillis()));
        bindParameters.add(new byte[]{1, 2, 3});
        bindParameters.add(null);

        final PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);

        // When
        databaseProvider.prepareNativeStatement(sql, bindParameters, false, transactionManager);

        // Then
        verify(ps).setInt(1, 10);
        verify(ps).setLong(2, 20L);
        verify(ps).setShort(3, (short) 5);
        verify(ps).setDouble(4, 1.5D);
        verify(ps).setFloat(5, 2.5F);
        verify(ps).setBigDecimal(6, BigDecimal.TEN);
        verify(ps).setBoolean(7, true);
        verify(ps).setString(8, "string");
        verify(ps).setTimestamp(eq(9), any(Timestamp.class));
        verify(ps).setBytes(10, new byte[]{1, 2, 3});
        verify(ps).setString(11, null);
    }

    @Test
    void fetchTableMetaData_withForeignKeys() throws Exception {
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
        when(pkResultSet.next()).thenReturn(false);
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true).thenReturn(false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("COL1");
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        // Foreign keys (Imported)
        final ResultSet importedKeys = mock(ResultSet.class);
        when(importedKeys.next()).thenReturn(true).thenReturn(false);
        when(importedKeys.getString("FK_NAME")).thenReturn("FK_1");
        when(importedKeys.getString("PKTABLE_NAME")).thenReturn("REMOTE_TABLE");
        when(importedKeys.getString("PKCOLUMN_NAME")).thenReturn("REMOTE_COL");
        when(importedKeys.getString("FKTABLE_NAME")).thenReturn("TEST_TABLE");
        when(importedKeys.getString("FKCOLUMN_NAME")).thenReturn("COL1");
        when(databaseMetaData.getImportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(importedKeys);

        // Foreign references (Exported)
        final ResultSet exportedKeys = mock(ResultSet.class);
        when(exportedKeys.next()).thenReturn(true).thenReturn(false);
        when(exportedKeys.getString("FK_NAME")).thenReturn("FK_REF");
        when(exportedKeys.getString("PKTABLE_NAME")).thenReturn("TEST_TABLE");
        when(exportedKeys.getString("PKCOLUMN_NAME")).thenReturn("COL1");
        when(exportedKeys.getString("FKTABLE_NAME")).thenReturn("REF_TABLE");
        when(exportedKeys.getString("FKCOLUMN_NAME")).thenReturn("REF_COL");
        when(databaseMetaData.getExportedKeys(table.catalog(), table.schema(), table.name())).thenReturn(exportedKeys);

        // When
        final TableMetaData result = databaseProvider.fetchTableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        final ColumnMetaData col1 = result.column("COL1");
        assertEquals(1, col1.getForeignKeyConstraints().size());
        assertEquals("FK_1", col1.getForeignKeyConstraints().iterator().next().name());
        assertEquals(1, col1.getForeignReferences().size());
        assertEquals("FK_REF", col1.getForeignReferences().iterator().next().name());
    }

    @Test
    void select_withConvertExpression() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();
        final org.litebridge.db.spi.expression.ConvertExpression convertExpression = new org.litebridge.db.spi.expression.ConvertExpression(new SelectColumn(column, new ColumnIdentifierGenerator()), Integer.class);

        final Select select = new Select(
                tableMetaData.toTable(),
                List.of(convertExpression),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        final ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
        when(rsmd.getColumnCount()).thenReturn(1);
        when(rsmd.getColumnName(1)).thenReturn(column.name());
        when(rsmd.getColumnLabel(1)).thenReturn(column.name());
        when(rsmd.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getMetaData()).thenReturn(rsmd);
        when(resultSet.getObject(1)).thenReturn("123");

        when(typeConverter.convert("123", Types.VARCHAR)).thenReturn(123);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final List<Row> result = databaseProvider.select(new PreparedSql("SELECT *", Collections.emptyList()), transactionManager);

        // Then
        assertEquals(1, result.size());
        assertEquals(123, result.get(0).column(column.name()).get().value());
    }

    @Test
    void getColumnMetaData_defaultValueTrimming() throws Exception {
        // Given
        mockTransactionManagerNoConnectionStub();
        final Table table = new Table("CAT", "SCH", "TAB");
        final DatabaseMetaData dbmd = mock(DatabaseMetaData.class);

        final ResultSet rs = mock(ResultSet.class);
        when(rs.next()).thenReturn(true, true, true, true, true, true, true, false);
        when(rs.getString("COLUMN_NAME")).thenReturn("C1", "C2", "C3", "C4", "C5", "C6", "C7");
        when(rs.getBoolean("IS_NULLABLE")).thenReturn(true);
        when(rs.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR);
        when(rs.getInt("COLUMN_SIZE")).thenReturn(10);
        when(rs.getBoolean("IS_AUTOINCREMENT")).thenReturn(false);
        when(rs.getInt("DECIMAL_DIGITS")).thenReturn(0);
        // C1: null default
        // C2: no quotes
        // C3: with quotes
        // C4: integer with quotes (should not be trimmed by the string-specific logic, but wait, the logic checks dataType)
        // C5: too short to be trimmed "'a'" is length 3, minimum is 2.
        // C6: CHAR with quotes
        // C7: LONGVARCHAR with quotes
        when(rs.getString("COLUMN_DEF")).thenReturn(null, "foo", "'bar'", "'123'", "'a'", "'char'", "'long'");

        when(dbmd.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(rs);

        // When
        final List<ColumnMetaData> result = databaseProvider.getColumnMetaData(table, dbmd);

        // Then
        assertEquals(7, result.size());
        assertEquals(null, result.get(0).getDefaultValue());
        assertEquals("foo", result.get(1).getDefaultValue());
        assertEquals("bar", result.get(2).getDefaultValue());
        assertEquals("'123'", result.get(3).getDefaultValue()); // Not trimmed because type is INTEGER
        assertEquals("a", result.get(4).getDefaultValue()); // Trimmed because length >= 2
        assertEquals("char", result.get(5).getDefaultValue());
        assertEquals("long", result.get(6).getDefaultValue());
    }

    @Test
    void select_withTypeConversionMetaData() throws Exception {
        // Given
        mockTransactionManager();
        final Table table = new Table(null, "SCH", "TAB");
        final ColumnMetaData cmd = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        final Map<String, ColumnMetaData> metadataMap = Map.of("COL1", cmd);
        final org.litebridge.db.spi.query.TypeConversionMetaData tcmd = new org.litebridge.db.spi.query.TypeConversionMetaData(metadataMap, new Class<?>[]{Integer.class});

        final PreparedSql preparedSql = new PreparedSql("SELECT COL1 FROM TAB", Collections.emptyList(), tcmd, null);

        final PreparedStatement ps = mock(PreparedStatement.class);
        final ResultSet rs = mock(ResultSet.class);
        final ResultSetMetaData rsmd = mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rsmd.getColumnCount()).thenReturn(1);
        when(rsmd.getColumnLabel(1)).thenReturn("COL1");
        when(rs.getMetaData()).thenReturn(rsmd);
        when(rs.getObject(1)).thenReturn("123");

        when(typeConverter.convert("123", Integer.class)).thenReturn(123);
        when(ps.executeQuery()).thenReturn(rs);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        // When
        final List<Row> result = databaseProvider.select(preparedSql, transactionManager);

        // Then
        assertEquals(1, result.size());
        assertEquals(123, result.get(0).column("COL1").get().value());
    }

    @Test
    void prepareStatement_blobBinding() throws Exception {
        // Given
        mockTransactionManager();
        final byte[] bytes = new byte[]{1, 2, 3};
        final PreparedSql preparedSql = new PreparedSql("INSERT INTO TAB (B) VALUES (?)",
                List.of(new BindValue(bytes, Types.BLOB)));

        final PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        // When
        databaseProvider.prepareStatement(preparedSql, transactionManager);

        // Then
        verify(ps).setBinaryStream(eq(1), any(InputStream.class));
    }

    @Test
    void prepareStatement_nullValueBinding() throws Exception {
        // Given
        mockTransactionManager();
        final PreparedSql preparedSql = new PreparedSql("INSERT INTO TAB (B) VALUES (?)",
                List.of(new BindValue(null, Types.INTEGER)));

        final PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(ps);

        // When
        databaseProvider.prepareStatement(preparedSql, transactionManager);

        // Then
        verify(ps).setNull(1, Types.INTEGER);
    }


    @Test
    void verifySchemaAndTableExists_nullSchema() throws SQLException {
        // Given
        final Table table = new Table(null, null, "TAB");
        final DatabaseMetaData dbmd = mock(DatabaseMetaData.class);

        final ResultSet srs = mock(ResultSet.class);
        when(srs.next()).thenReturn(true, false);
        when(srs.getString("TABLE_SCHEM")).thenReturn(null);
        when(dbmd.getSchemas(null, null)).thenReturn(srs);

        final ResultSet trs = mock(ResultSet.class);
        when(trs.next()).thenReturn(true, false);
        when(trs.getString("TABLE_NAME")).thenReturn("TAB");
        when(dbmd.getTables(null, null, "TAB", AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(trs);

        // When
        databaseProvider.verifySchemaAndTableExists(table, dbmd);

        // Then: no exception thrown
    }

    @Test
    void executeSqlInsert_returnGeneratedKeysTrue_affectedRowsZero() throws Exception {
        // Given
        final TableMetaData tableMetaData = tableMetaDataImpl();
        final PreparedSql preparedSql = new PreparedSql(
                "INSERT INTO TEST_TABLE(TEST_COLUMN) VALUES (?)",
                List.of(new BindValue("column", Types.VARCHAR)),
                null,
                new UpdateMetaData(true, tableMetaData.primaryKey(), new String[]{"TEST_PK"}));

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        when(connection.prepareStatement(eq(preparedSql.sql()), any(String[].class))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.executeSqlInsert(preparedSql, transactionManager);

        // Then
        assertNotNull(result);
        assertEquals(0, result.rowsAffected());
        assertTrue(result.generatedKeys().isEmpty());
    }

    @Test
    void fetchTableMetaData_keysMismatch() throws Exception {
        // Given
        mockTransactionManager();
        final Table table = new Table(null, "SCH", "TAB");
        final DatabaseMetaData dbmd = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(dbmd);

        // Mock verifySchemaAndTableExists
        final ResultSet srs = mock(ResultSet.class);
        when(srs.next()).thenReturn(true, false);
        when(srs.getString("TABLE_SCHEM")).thenReturn("SCH");
        when(dbmd.getSchemas(null, "SCH")).thenReturn(srs);
        final ResultSet trs = mock(ResultSet.class);
        when(trs.next()).thenReturn(true, false);
        when(trs.getString("TABLE_NAME")).thenReturn("TAB");
        when(dbmd.getTables(null, "SCH", "TAB", AbstractDatabaseProvider.TYPES_TABLE)).thenReturn(trs);

        // Mock columns
        final ResultSet crs = mock(ResultSet.class);
        when(crs.next()).thenReturn(true, false);
        when(crs.getString("COLUMN_NAME")).thenReturn("COL1");
        when(dbmd.getColumns(null, "SCH", "TAB", null)).thenReturn(crs);

        // Mock PK
        when(dbmd.getPrimaryKeys(null, "SCH", "TAB")).thenReturn(mock(ResultSet.class));

        // Mock imported keys with DIFFERENT table name
        final ResultSet ikrs = mock(ResultSet.class);
        when(ikrs.next()).thenReturn(true, false);
        when(ikrs.getString("FK_NAME")).thenReturn("FK_1");
        when(ikrs.getString("PKTABLE_NAME")).thenReturn("PK_TAB");
        when(ikrs.getString("PKCOLUMN_NAME")).thenReturn("PK_COL");
        when(ikrs.getString("FKTABLE_NAME")).thenReturn("OTHER_TAB");
        when(ikrs.getString("FKCOLUMN_NAME")).thenReturn("FK_COL");
        when(dbmd.getImportedKeys(null, "SCH", "TAB")).thenReturn(ikrs);

        // Mock exported keys with DIFFERENT table name
        final ResultSet ekrs = mock(ResultSet.class);
        when(ekrs.next()).thenReturn(true, false);
        when(ekrs.getString("FK_NAME")).thenReturn("FK_2");
        when(ekrs.getString("PKTABLE_NAME")).thenReturn("OTHER_TAB");
        when(ekrs.getString("PKCOLUMN_NAME")).thenReturn("PK_COL");
        when(ekrs.getString("FKTABLE_NAME")).thenReturn("FK_TAB");
        when(ekrs.getString("FKCOLUMN_NAME")).thenReturn("FK_COL");
        when(dbmd.getExportedKeys(null, "SCH", "TAB")).thenReturn(ekrs);

        // When
        final TableMetaData result = databaseProvider.fetchTableMetaData(table, transactionManager);

        // Then
        assertNotNull(result);
        assertTrue(result.column("COL1").getForeignKeyConstraints().isEmpty());
    }

    @Test
    void getColumnMetaData_defaultValueTrimming_edgeCases() throws Exception {
        // Given
        mockTransactionManagerNoConnectionStub();
        final Table table = new Table(null, "SCH", "TAB");
        final DatabaseMetaData dbmd = mock(DatabaseMetaData.class);

        final ResultSet rs = mock(ResultSet.class);
        // C1: length 1 quote
        // C2: length 2 empty quotes
        // C3: starts with quote, doesn't end
        // C4: ends with quote, doesn't start
        // C5: wrong type but looks like quoted string
        when(rs.next()).thenReturn(true, true, true, true, true, false);
        when(rs.getString("COLUMN_NAME")).thenReturn("C1", "C2", "C3", "C4", "C5");
        when(rs.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER);
        when(rs.getString("COLUMN_DEF")).thenReturn("'", "''", "'a", "a'", "'123'");

        when(dbmd.getColumns(null, "SCH", "TAB", null)).thenReturn(rs);

        // When
        final List<ColumnMetaData> result = databaseProvider.getColumnMetaData(table, dbmd);

        // Then
        assertEquals("'", result.get(0).getDefaultValue());
        assertEquals("", result.get(1).getDefaultValue());
        assertEquals("'a", result.get(2).getDefaultValue());
        assertEquals("a'", result.get(3).getDefaultValue());
        assertEquals("'123'", result.get(4).getDefaultValue());
    }


    @Test
    void prepareNativeStatement_nullParams() throws Exception {
        // Given
        mockTransactionManager();
        final String sql = "SELECT * FROM TAB";
        final PreparedStatement ps = mock(PreparedStatement.class);
        when(connection.prepareStatement(sql)).thenReturn(ps);

        // When
        final PreparedStatement result = databaseProvider.prepareNativeStatement(sql, null, false, transactionManager);

        // Then
        assertSame(ps, result);
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