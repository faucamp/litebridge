package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.RowValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractDatabaseProviderTest {

    @Mock
    private Connection connection;

    @Mock
    private TypeConverter typeConverter;

    @InjectMocks
    private TestDatabaseProvider databaseProvider;

    @Test
    void getTableMetaData() throws Exception {
        getTableMetaDataImpl();
    }

    private TableMetaData getTableMetaDataImpl() throws SQLException {
        return getTableMetaDataImpl("TEST_SCHEMA");
    }

    private TableMetaData getTableMetaDataImpl(final String schema) throws SQLException {
        // Given
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
        final TableMetaData result = databaseProvider.getTableMetaData(table);

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

    @Test
    void insert() throws Exception {
        // Given
        final TableMetaData table = getTableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        column.setSequence("TEST_SEQUENCE");
        final ColumnValue columnValue1 = new ColumnValue(column, "testValue1");
        final ColumnValue columnValue2 = new ColumnValue(column, null);
        final RowValue rowValue1 = new RowValue(List.of(columnValue1));
        final RowValue rowValue2 = new RowValue(List.of(columnValue2));

        final Insert insert = new Insert(table, List.of(column), List.of(rowValue1, rowValue2), true);

        when(typeConverter.convert("testValue1", Types.VARCHAR)).thenReturn("testValue1");
        //when(typeConverter.convert("", Types.VARCHAR)).thenReturn("testValue2");

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject(table.primaryKey().get(0).name())).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertEquals(1, result.generatedKeys().size());
        assertEquals("testValue", result.generatedKeys().get(0));
    }

    @Test
    void insert_noSchema() throws Exception {
        // Given
        final TableMetaData table = getTableMetaDataImpl("");
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column, "testValue");
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table, rowValue, true);

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getObject(table.primaryKey().get(0).name())).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertEquals(1, result.generatedKeys().size());
        assertEquals("testValue", result.generatedKeys().get(0));
    }

    @Test
    void insert_noAffectedRows() throws Exception {
        // Given
        final TableMetaData table = getTableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column, "testValue");
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table, List.of(rowValue), true);

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert);

        // Then
        assertNotNull(result);
        assertEquals(0, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertTrue(result.generatedKeys().isEmpty());
    }

    @Test
    void insert_noGeneratedKeys() throws Exception {
        // Given
        final TableMetaData table = getTableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column, "testValue");
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table, List.of(column), List.of(rowValue), true);

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);

        // When
        final InsertResult result = databaseProvider.insert(insert);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertNotNull(result.generatedKeys());
        assertTrue(result.generatedKeys().isEmpty());
    }

    @Test
    void insert_nullValue_notNullColumn() throws Exception {
        // Given
        final TableMetaData table = getTableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column, null);
        final RowValue rowValue = new RowValue(List.of(columnValue));

        final Insert insert = new Insert(table, List.of(column), List.of(rowValue), true);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> databaseProvider.insert(insert));
    }

    @Test
    void update() throws Exception {
        // Given
        final TableMetaData table = getTableMetaDataImpl();
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue1 = new ColumnValue(column, "testValue");
        final ColumnValue columnValue2 = new ColumnValue(column, "testValue");
        final Condition condition1 = new Condition(column.toColumn(), Operator.EQ, "conditionValue");
        final Condition condition2 = new Condition(column.toColumn(), Operator.IS_NOT_NULL);
        final Condition condition3 = new Condition(column.toColumn(), Operator.IS_NULL);

        final Update update = new Update(table, List.of(columnValue1, columnValue2), List.of(condition1, condition2, condition3));

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.update(update);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
    }

    @Test
    void update_noSchema_noConditions() throws Exception {
        // Given
        final TableMetaData table = getTableMetaDataImpl("");
        final ColumnMetaData column = table.column("TEST_COLUMN");
        final ColumnValue columnValue = new ColumnValue(column, "testValue");

        final Update update = new Update(table, List.of(columnValue), Collections.emptyList());

        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        final UpdateResult result = databaseProvider.update(update);

        // Then
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
    }

    @Test
    void select() throws Exception {
        // Given
        final TableMetaData tableMetaData = getTableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), "t1");
        final Column column = tableMetaData.column("TEST_COLUMN").toColumn();

        final Select select = new Select(
                table,
                List.of(column),
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
        when(resultSetMetaData.getSchemaName(1)).thenReturn(tableMetaData.schema());
        when(resultSetMetaData.getTableName(1)).thenReturn(tableMetaData.name());
        when(resultSetMetaData.getColumnName(1)).thenReturn(column.name());
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // When
        List<Row> result = databaseProvider.select(select);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        final Row row = result.get(0);
        assertNotNull(row.column(column.name()));
    }

    @Test
    void getTypeConverter() {
        assertEquals(typeConverter, databaseProvider.getTypeConverter());
    }

    @Test
    void toSql() throws Exception {
        // Given
        final TableMetaData tableMetaData = getTableMetaDataImpl();
        final Table table = new Table(tableMetaData.catalog(), tableMetaData.schema(), tableMetaData.name(), "t1");
        final Column column1 = tableMetaData.column("TEST_PK").toColumn().as("col1");
        column1.table().setAlias("t1");
        final Column column2 = tableMetaData.column("TEST_COLUMN").toColumn().as("col2");
        column2.table().setAlias("t1");

        final Select select = new Select(
                table,
                List.of(column1, column2),
                List.of(new Join(table, List.of(new Condition(column2, Operator.EQ, "TEST_VALUE")))),
                List.of(new OrderBy(column1, true)),
                List.of(new Condition(column2, Operator.EQ, "TEST_VALUE"),
                        new Condition(column2, Operator.NEQ, "OTHER_VALUE")),
                Optional.of(new Limit(Optional.of(10), Optional.of(20))));

        // When
        final String result = databaseProvider.toSql(select);

        // Then
        assertNotNull(result);
        assertEquals("SELECT t1.TEST_PK AS col1, t1.TEST_COLUMN AS col2 FROM TEST_SCHEMA.TEST_TABLE AS t1 JOIN TEST_SCHEMA.TEST_TABLE AS t1 ON t1.TEST_COLUMN = ? WHERE t1.TEST_COLUMN = ? AND t1.TEST_COLUMN <> ? ORDER BY t1.TEST_PK ASC LIMIT 10 OFFSET 20", result);
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
        final Column column = getTableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.EQ, "testValue");

        // When
        final String result = databaseProvider.createCondition(condition);

        // Then
        assertNotNull(result);
        assertEquals("TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_isNull() throws Exception {
        // Given
        final Column column = getTableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.IS_NULL);

        // When
        final String result = databaseProvider.createCondition(condition);

        // Then
        assertNotNull(result);
        assertEquals("TEST_COLUMN IS NULL", result);
    }

    @Test
    void createCondition_isNotNull() throws Exception {
        // Given
        final Column column = getTableMetaDataImpl().column("TEST_COLUMN").toColumn();
        final Condition condition = new Condition(column, Operator.IS_NOT_NULL);

        // When
        final String result = databaseProvider.createCondition(condition);

        // Then
        assertNotNull(result);
        assertEquals("TEST_COLUMN IS NOT NULL", result);
    }

    @Test
    void prepareStatement() throws Exception {
        // Given
        final Object objectVal = new Object();
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
                new AbstractDatabaseProvider.BindValue(null, Types.NUMERIC),
                new AbstractDatabaseProvider.BindValue(objectVal, Types.OTHER)
        );
        final AbstractDatabaseProvider.PreparedSql preparedSql = new AbstractDatabaseProvider.PreparedSql("SELECT * FROM TEST_TABLE", bindValues);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(preparedSql.sql())).thenReturn(preparedStatement);

        // When
        final PreparedStatement result = databaseProvider.prepareStatement(preparedSql, false);

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
        verify(result).setNull(10, Types.NUMERIC);
        verify(result).setObject(11, objectVal, Types.OTHER);
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
    void getLogger() {
        assertNotNull(databaseProvider.getLogger());
    }

    private static class TestDatabaseProvider extends AbstractDatabaseProvider {

        public TestDatabaseProvider(final Connection connection, final TypeConverter typeConverter) {
            super(connection, typeConverter);
        }
    }
}