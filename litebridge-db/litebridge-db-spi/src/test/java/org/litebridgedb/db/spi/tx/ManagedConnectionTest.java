package org.litebridgedb.db.spi.tx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagedConnectionTest {

    @Mock
    private Connection mockConnection;

    private ManagedConnection managedConnection;

    @BeforeEach
    void setUp() {
        managedConnection = new ManagedConnection(mockConnection);
    }

    @Test
    void testDelegatedMethods() throws SQLException {
        managedConnection.createStatement();
        verify(mockConnection).createStatement();

        managedConnection.prepareStatement("sql");
        verify(mockConnection).prepareStatement("sql");

        managedConnection.prepareCall("sql");
        verify(mockConnection).prepareCall("sql");

        managedConnection.nativeSQL("sql");
        verify(mockConnection).nativeSQL("sql");

        managedConnection.getAutoCommit();
        verify(mockConnection).getAutoCommit();

        managedConnection.isClosed();
        verify(mockConnection).isClosed();

        managedConnection.getMetaData();
        verify(mockConnection).getMetaData();

        managedConnection.isReadOnly();
        verify(mockConnection).isReadOnly();

        managedConnection.getCatalog();
        verify(mockConnection).getCatalog();

        managedConnection.getTransactionIsolation();
        verify(mockConnection).getTransactionIsolation();

        managedConnection.getWarnings();
        verify(mockConnection).getWarnings();

        managedConnection.clearWarnings();
        verify(mockConnection).clearWarnings();

        managedConnection.createStatement(1, 2);
        verify(mockConnection).createStatement(1, 2);

        managedConnection.prepareStatement("sql", 1, 2);
        verify(mockConnection).prepareStatement("sql", 1, 2);

        managedConnection.prepareCall("sql", 1, 2);
        verify(mockConnection).prepareCall("sql", 1, 2);

        managedConnection.getTypeMap();
        verify(mockConnection).getTypeMap();

        managedConnection.getHoldability();
        verify(mockConnection).getHoldability();

        managedConnection.createStatement(1, 2, 3);
        verify(mockConnection).createStatement(1, 2, 3);

        managedConnection.prepareStatement("sql", 1, 2, 3);
        verify(mockConnection).prepareStatement("sql", 1, 2, 3);

        managedConnection.prepareCall("sql", 1, 2, 3);
        verify(mockConnection).prepareCall("sql", 1, 2, 3);

        managedConnection.prepareStatement("sql", 1);
        verify(mockConnection).prepareStatement("sql", 1);

        managedConnection.prepareStatement("sql", new int[]{1});
        verify(mockConnection).prepareStatement("sql", new int[]{1});

        managedConnection.prepareStatement("sql", new String[]{"col"});
        verify(mockConnection).prepareStatement("sql", new String[]{"col"});

        managedConnection.createClob();
        verify(mockConnection).createClob();

        managedConnection.createBlob();
        verify(mockConnection).createBlob();

        managedConnection.createNClob();
        verify(mockConnection).createNClob();

        managedConnection.createSQLXML();
        verify(mockConnection).createSQLXML();

        managedConnection.isValid(10);
        verify(mockConnection).isValid(10);

        managedConnection.setClientInfo("name", "rhs");
        verify(mockConnection).setClientInfo("name", "rhs");

        Properties props = new Properties();
        managedConnection.setClientInfo(props);
        verify(mockConnection).setClientInfo(props);

        managedConnection.getClientInfo("name");
        verify(mockConnection).getClientInfo("name");

        managedConnection.getClientInfo();
        verify(mockConnection).getClientInfo();

        managedConnection.createArrayOf("type", new Object[]{});
        verify(mockConnection).createArrayOf("type", new Object[]{});

        managedConnection.createStruct("type", new Object[]{});
        verify(mockConnection).createStruct("type", new Object[]{});

        managedConnection.getSchema();
        verify(mockConnection).getSchema();

        managedConnection.getNetworkTimeout();
        verify(mockConnection).getNetworkTimeout();
    }

    @Test
    void testUnsupportedMethods() {
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setAutoCommit(true));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.commit());
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.rollback());
        managedConnection.close();
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setReadOnly(true));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setCatalog("cat"));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setTransactionIsolation(1));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setTypeMap(Collections.emptyMap()));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setHoldability(1));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setSavepoint());
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setSavepoint("name"));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.rollback(mock(Savepoint.class)));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.releaseSavepoint(mock(Savepoint.class)));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setSchema("schema"));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.abort(mock(Executor.class)));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setNetworkTimeout(mock(Executor.class), 10));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.beginRequest());
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.endRequest());
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setShardingKeyIfValid(mock(ShardingKey.class), mock(ShardingKey.class), 10));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setShardingKeyIfValid(mock(ShardingKey.class), 10));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setShardingKey(mock(ShardingKey.class), mock(ShardingKey.class)));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.setShardingKey(mock(ShardingKey.class)));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.unwrap(String.class));
        assertThrows(UnsupportedOperationException.class, () -> managedConnection.isWrapperFor(String.class));
    }
}
