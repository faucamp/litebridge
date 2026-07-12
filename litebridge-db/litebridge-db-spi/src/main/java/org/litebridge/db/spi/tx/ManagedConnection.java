package org.litebridge.db.spi.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * A wrapper for a {@link Connection} object that ensures certain methods associated with
 * transaction and connection management are not directly invoked.
 * <p>
 * The purpose of this class is to provide a managed connection where specific operations
 * like commit, rollback, close, and others related to transaction state or connection properties
 * are restricted, while other operations are delegated to the underlying connection.
 * <p>
 * This class implements the {@link Connection} interface, proxying the majority of its methods
 * to an internal {@link Connection} instance. For methods that are deemed unsafe or restricted
 * for managed contexts, an {@link UnsupportedOperationException} is thrown.
 */
public final class ManagedConnection implements Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedConnection.class);
    private final Connection connection;

    /**
     * Constructs a new {@code ManagedConnection} wrapping the specified connection.
     *
     * @param connection the underlying JDBC connection to manage
     */
    public ManagedConnection(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return connection.prepareCall(sql);
    }

    @Override
    public String nativeSQL(final String sql) throws SQLException {
        return connection.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) {
        throw managedMethodUnsupported("setAutoCommit(boolean)");
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }

    @Override
    public void commit() {
        throw managedMethodUnsupported("commit()");
    }

    @Override
    public void rollback() {
        throw managedMethodUnsupported("rollback()");
    }

    @Override
    public void close() {
        LOGGER.trace("Ignoring close(); managed by transaction manager");
    }

    @Override
    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }

    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        throw managedMethodUnsupported("setReadOnly(boolean)");
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return connection.isReadOnly();
    }

    @Override
    public void setCatalog(final String catalog) throws SQLException {
        throw managedMethodUnsupported("setCatalog(String)");
    }

    @Override
    public String getCatalog() throws SQLException {
        return connection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        throw managedMethodUnsupported("setTransactionIsolation(int)");
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return connection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return connection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        connection.clearWarnings();
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return connection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(
            final String sql,
            final int resultSetType,
            final int resultSetConcurrency
    ) throws SQLException {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(
            final String sql,
            final int resultSetType,
            final int resultSetConcurrency
    ) throws SQLException {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return connection.getTypeMap();
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        throw managedMethodUnsupported("setTypeMap(Map<String, Class<?>>)");
    }

    @Override
    public void setHoldability(final int holdability) throws SQLException {
        throw managedMethodUnsupported("setHoldability(int)");
    }

    @Override
    public int getHoldability() throws SQLException {
        return connection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() {
        throw managedMethodUnsupported("setSavepoint()");
    }

    @Override
    public Savepoint setSavepoint(final String name) {
        throw managedMethodUnsupported("setSavepoint(String)");
    }

    @Override
    public void rollback(final Savepoint savepoint) {
        throw managedMethodUnsupported("rollback(Savepoint)");
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) {
        throw managedMethodUnsupported("releaseSavepoint(Savepoint)");
    }

    @Override
    public Statement createStatement(
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability
    ) throws SQLException {
        return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(
            final String sql,
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability
    ) throws SQLException {
        return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(
            final String sql,
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability
    ) throws SQLException {
        return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return connection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return connection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return connection.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return connection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return connection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return connection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return connection.createSQLXML();
    }

    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return connection.isValid(timeout);
    }

    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        connection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        connection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(final String name) throws SQLException {
        return connection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return connection.getClientInfo();
    }

    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        return connection.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        return connection.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(final String schema) throws SQLException {
        throw managedMethodUnsupported("setSchema(String)");
    }

    @Override
    public String getSchema() throws SQLException {
        return connection.getSchema();
    }

    @Override
    public void abort(final Executor executor) {
        throw managedMethodUnsupported("abort(Executor)");
    }

    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        throw managedMethodUnsupported("setNetworkTimeout(Executor, int)");
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return connection.getNetworkTimeout();
    }

    @Override
    public void beginRequest() throws SQLException {
        throw managedMethodUnsupported("beginRequest()");
    }

    @Override
    public void endRequest() throws SQLException {
        throw managedMethodUnsupported("endRequest()");
    }

    @Override
    public boolean setShardingKeyIfValid(
            final ShardingKey shardingKey,
            final ShardingKey superShardingKey,
            final int timeout
    ) throws SQLException {
        throw managedMethodUnsupported("setShardingKeyIfValid(ShardingKey, ShardingKey, int)");
    }

    @Override
    public boolean setShardingKeyIfValid(final ShardingKey shardingKey, final int timeout) throws SQLException {
        throw managedMethodUnsupported("setShardingKeyIfValid(ShardingKey, int)");
    }

    @Override
    public void setShardingKey(final ShardingKey shardingKey, final ShardingKey superShardingKey)
            throws SQLException {
        throw managedMethodUnsupported("setShardingKey(ShardingKey, ShardingKey)");
    }

    @Override
    public void setShardingKey(final ShardingKey shardingKey) throws SQLException {
        throw managedMethodUnsupported("setShardingKey(ShardingKey)");
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw managedMethodUnsupported("unwrap(Class<T>)");
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        throw managedMethodUnsupported("isWrapperFor(Class<T>)");
    }

    private UnsupportedOperationException managedMethodUnsupported(final String method) {
        return new UnsupportedOperationException("Method managed by transaction manager: " + method);
    }
}
