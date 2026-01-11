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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
        // Given
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
        when(pkResultSet.getString("COLUMN_NAME")).thenReturn("TEST_COLUMN");
        when(databaseMetaData.getPrimaryKeys(table.catalog(), table.schema(), table.name())).thenReturn(pkResultSet);

        final ResultSet columnResultSet = mock(ResultSet.class);
        when(columnResultSet.next()).thenReturn(true).thenReturn(false);
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("TEST_COLUMN");
        when(columnResultSet.getBoolean("IS_NULLABLE")).thenReturn(Boolean.TRUE);
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.VARCHAR);
        when(columnResultSet.getInt("COLUMN_SIZE")).thenReturn(10);
        when(databaseMetaData.getColumns(table.catalog(), table.schema(), table.name(), null)).thenReturn(columnResultSet);

        // When
        final TableMetaData result = databaseProvider.getTableMetaData(table);

        // Then
        assertNotNull(result);
        assertEquals("TEST_COLUMN", result.column("TEST_COLUMN").name());
        assertEquals(true, result.column("TEST_COLUMN").isNullable());
        assertEquals(Types.VARCHAR, result.column("TEST_COLUMN").getDataType());
        assertEquals(10, result.column("TEST_COLUMN").getSize());
        assertNotNull(result.primaryKey());
        assertEquals(1, result.primaryKey().size());
        assertEquals("TEST_COLUMN", result.primaryKey().get(0).name());
    }

    @Test
    void insert() {
    }

    @Test
    void update() {
    }

    @Test
    void select() throws Exception {
        // Given
        getTableMetaData();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");

        final Select select = new Select(
                table,
                List.of(column),
                List.of(new Join(table, List.of(new Condition(column, Operator.EQ, "TEST_VALUE")))),
                List.of(new OrderBy(column.name(), true)),
                List.of(new Condition(column, Operator.EQ, "TEST_VALUE")),
                Optional.of(new Limit(Optional.of(10), Optional.of(20))));

        when(typeConverter.convert("TEST_VALUE", Types.VARCHAR)).thenReturn("TEST_VALUE");
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        final ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getSchemaName(1)).thenReturn(table.schema());
        when(resultSetMetaData.getTableName(1)).thenReturn(table.name());
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
        getTableMetaData();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");

        final Select select = new Select(
                table,
                List.of(column),
                List.of(new Join(table, List.of(new Condition(column, Operator.EQ, "TEST_VALUE")))),
                List.of(new OrderBy(column.name(), true)),
                List.of(new Condition(column, Operator.EQ, "TEST_VALUE")),
                Optional.of(new Limit(Optional.of(10), Optional.of(20))));

        // When
        final String result = databaseProvider.toSql(select);

        // Then
        assertNotNull(result);
        assertEquals("SELECT TEST_TABLE.TEST_COLUMN FROM TEST_SCHEMA.TEST_TABLE JOIN TEST_SCHEMA.TEST_TABLE ON TEST_COLUMN = ? WHERE TEST_COLUMN = ? ORDER BY TEST_COLUMN ASC LIMIT 10 OFFSET 20", result);
    }

    private static class TestDatabaseProvider extends AbstractDatabaseProvider {

        public TestDatabaseProvider(final Connection connection, final TypeConverter typeConverter) {
            super(connection, typeConverter);
        }
    }
}