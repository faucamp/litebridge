package org.litebridge.db.spi.impl.engine;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.ManagedConnection;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class ExecutionEngineTest {

    @Test
    void executeUpdate_withEmptyBinds_returnsAffectedRows() throws Exception {
        // Given
        final PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenReturn(3);
        final Connection connection = connection(statement);
        final ExecutionEngine engine = engine();

        // When
        final UpdateResult result = engine.executeUpdate(new PreparedSql("update test set value = 1"), provider(connection));

        // Then
        assertEquals(3, result.rowsAffected());
        verify(statement).executeUpdate();
        verify(statement).close();
    }

    @Test
    void executeQuery_withResultSetMetadata_returnsMappedRows() throws Exception {
        // Given
        final PreparedStatement statement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(resultSet.next()).thenReturn(true, false);
        when(metadata.getColumnCount()).thenReturn(1);
        when(metadata.getColumnLabel(1)).thenReturn("name");
        when(metadata.getSchemaName(1)).thenReturn("");
        when(metadata.getTableName(1)).thenReturn("test");
        when(metadata.getColumnName(1)).thenReturn("name");
        when(metadata.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getObject(1)).thenReturn("Alice");
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.convert("Alice", Types.VARCHAR)).thenReturn("converted");
        final ExecutionEngine engine = new ExecutionEngineReturnedKeysAuto(typeConverter, String::toLowerCase);
        final Connection connection = connection(statement);

        // When
        final List<Row> rows = engine.executeQuery(new PreparedSql("select name from test"), provider(connection));

        // Then
        assertEquals(1, rows.size());
        assertEquals(1, rows.getFirst().size());
        assertEquals("converted", rows.getFirst().getValue(0));
        assertEquals("name", rows.getFirst().column(0).column().alias());
        verify(statement).close();
    }

    @Test
    void executeQuery_withConversionMetaData_usesAliasAndTypeOverride() throws Exception {
        // Given
        final PreparedStatement statement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(resultSet.next()).thenReturn(true, false);
        when(metadata.getColumnCount()).thenReturn(1);
        when(metadata.getColumnLabel(1)).thenReturn("name");
        when(resultSet.getObject(1)).thenReturn("Alice");
        final Table aliasedTable = new Table("TEST_TABLE", "t");
        final ColumnMetaData columnMetaData = new ColumnMetaData(new Table("TEST_TABLE"), "NAME", true, Types.VARCHAR);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        when(typeConverter.convert("Alice", Integer.class)).thenReturn(7);
        final TypeConversionMetaData conversionMetaData = new TypeConversionMetaData(
                Map.of("name", columnMetaData), new Class<?>[]{Integer.class}, Map.of("name", aliasedTable));
        final ExecutionEngine engine = new ExecutionEngineReturnedKeysAuto(typeConverter, String::toLowerCase);
        final Connection connection = connection(statement);

        // When
        final List<Row> rows = engine.executeQuery(new PreparedSql("select name", List.of(), conversionMetaData, null), provider(connection));

        // Then
        assertEquals(7, rows.getFirst().getValue(0));
        assertEquals("t", rows.getFirst().column(0).column().table().alias());
    }

    @Test
    void executeInsert_withoutGeneratedKeys_returnsAffectedRows() throws Exception {
        // Given
        final PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenReturn(1);
        final ExecutionEngine engine = engine();

        // When
        final PreparedSql preparedSql = new PreparedSql("insert into test values (1)", List.of(), null,
                new UpdateMetaData(false, List.of(), new String[0]));
        final InsertResult result = engine.executeInsert(preparedSql, provider(connection(statement)));

        // Then
        assertEquals(1, result.rowsAffected());
        assertEquals(0, result.generatedKeys().size());
    }

    @Test
    void prepareStatement_withNullAndTypedBinds_usesNullSetters() throws Exception {
        // Given
        final PreparedStatement statement = mock(PreparedStatement.class);
        final ExecutionEngine engine = engine();
        final PreparedSql preparedSql = new PreparedSql("update test", Arrays.asList(
                null,
                new BindValue(null, Types.INTEGER)));

        // When
        engine.executeUpdate(preparedSql, provider(connection(statement)));

        // Then
        verify(statement).setString(1, null);
        verify(statement).setNull(2, Types.INTEGER);
    }

    @Test
    void prepareStatement_withSupportedBindTypes_usesMatchingJdbcSetters() throws Exception {
        // Given
        final PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenReturn(1);
        final byte[] blob = {1, 2};
        final byte[] bytes = {3, 4};
        final Timestamp timestamp = Timestamp.valueOf("2026-09-06 20:45:00");
        final Object fallback = new Object();
        final PreparedSql preparedSql = new PreparedSql("update test", List.of(
                new BindValue(7),
                new BindValue(8L),
                new BindValue((short) 9),
                new BindValue(1.5d),
                new BindValue(2.5f),
                new BindValue(new BigDecimal("3.5")),
                new BindValue(true),
                new BindValue("text"),
                new BindValue(timestamp),
                new BindValue(blob, Types.BLOB),
                new BindValue(bytes),
                new BindValue(fallback, Types.JAVA_OBJECT)));
        final ExecutionEngine engine = engine();

        // When
        engine.executeUpdate(preparedSql, provider(connection(statement)));

        // Then
        verify(statement).setInt(1, 7);
        verify(statement).setLong(2, 8L);
        verify(statement).setShort(3, (short) 9);
        verify(statement).setDouble(4, 1.5d);
        verify(statement).setFloat(5, 2.5f);
        verify(statement).setBigDecimal(6, new BigDecimal("3.5"));
        verify(statement).setBoolean(7, true);
        verify(statement).setString(8, "text");
        verify(statement).setTimestamp(9, timestamp);
        final var blobStream = org.mockito.ArgumentCaptor.forClass(java.io.InputStream.class);
        verify(statement).setBinaryStream(eq(10), blobStream.capture());
        assertArrayEquals(blob, blobStream.getValue().readAllBytes());
        verify(statement).setBytes(11, bytes);
        verify(statement).setObject(12, fallback, Types.JAVA_OBJECT);
    }

    @Test
    void executeInsert_withAutoGeneratedKeys_returnsGeneratedKey() throws Exception {
        // Given
        final ColumnMetaData key = key("ID");
        final PreparedStatement statement = mock(PreparedStatement.class);
        final ResultSet generatedKeys = mock(ResultSet.class);
        when(statement.executeUpdate()).thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("ID")).thenReturn(42);
        final Connection connection = connection(statement);
        final ExecutionEngine engine = new ExecutionEngineReturnedKeysAuto(mock(TypeConverter.class), String::toLowerCase);
        final PreparedSql preparedSql = new PreparedSql("insert", List.of(), null,
                new UpdateMetaData(true, List.of(key), new String[]{"ID"}));

        // When
        final InsertResult result = engine.executeInsert(preparedSql, provider(connection));

        // Then
        assertEquals(42, result.generatedKeys().get(key));
        verify(connection).prepareStatement("insert", Statement.RETURN_GENERATED_KEYS);
    }

    @Test
    void executeInsert_withNamedGeneratedKeys_returnsGeneratedKey() throws Exception {
        // Given
        final ColumnMetaData key = key("ID");
        final PreparedStatement statement = mock(PreparedStatement.class);
        final ResultSet generatedKeys = mock(ResultSet.class);
        when(statement.executeUpdate()).thenReturn(1);
        when(statement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true, false);
        when(generatedKeys.getObject("ID")).thenReturn(84);
        final Connection connection = connection(statement);
        final ExecutionEngine engine = new ExecutionEngineReturnedKeysNamed(mock(TypeConverter.class), String::toLowerCase);
        final PreparedSql preparedSql = new PreparedSql("insert", List.of(), null,
                new UpdateMetaData(true, List.of(key), new String[]{"ID"}));

        // When
        final InsertResult result = engine.executeInsert(preparedSql, provider(connection));

        // Then
        assertEquals(84, result.generatedKeys().get(key));
        final var generatedKeyNames = forClass(String[].class);
        verify(connection).prepareStatement(eq("insert"), generatedKeyNames.capture());
        assertArrayEquals(new String[]{"ID"}, generatedKeyNames.getValue());
    }

    @Test
    void executeInsert_withNoAffectedRows_doesNotReadGeneratedKeys() throws Exception {
        // Given
        final PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenReturn(0);
        final ExecutionEngine engine = new ExecutionEngineReturnedKeysAuto(mock(TypeConverter.class), String::toLowerCase);
        final ColumnMetaData key = key("ID");
        final PreparedSql preparedSql = new PreparedSql("insert", List.of(), null,
                new UpdateMetaData(true, List.of(key), new String[]{"ID"}));

        // When
        final InsertResult result = engine.executeInsert(preparedSql, provider(connection(statement)));

        // Then
        assertEquals(0, result.rowsAffected());
        verify(statement, never()).getGeneratedKeys();
    }

    private static ExecutionEngine engine() {
        return new ExecutionEngineReturnedKeysAuto(mock(TypeConverter.class), String::toLowerCase);
    }

    private static Connection connection(final PreparedStatement statement) throws Exception {
        final Connection connection = mock(Connection.class);
        when(connection.prepareStatement(any(String.class))).thenReturn(statement);
        when(connection.prepareStatement(any(String.class), anyInt())).thenReturn(statement);
        when(connection.prepareStatement(any(String.class), any(String[].class))).thenReturn(statement);
        return connection;
    }

    private static ConnectionProvider provider(final Connection connection) throws Exception {
        final ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.connection()).thenReturn(new ManagedConnection(connection));
        return provider;
    }

    private static ColumnMetaData key(final String name) {
        return new ColumnMetaData(new Table("test"), name, false, Types.INTEGER);
    }
}