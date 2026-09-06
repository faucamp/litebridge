package org.litebridge.orm.util;

import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseProviderTestUtil {

    private DatabaseProviderTestUtil() {
    }

    public static DatabaseProvider mockDatabaseProviderWithMetaData() {
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);

        try {
            when(databaseProvider.databaseMetaData(any(ConnectionProvider.class))).thenReturn(createDatabaseMetaData());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return databaseProvider;
    }

    public static DatabaseProviderMetaData createMetaData() {
        return new DatabaseProviderMetaData(true);
    }

    public static DatabaseMetaData createDatabaseMetaData() {
        return new DatabaseMetaData(
                new DatabaseMetaData.Component("Test Database",
                        "0.5",
                        0,
                        5),
                new DatabaseMetaData.Component("Test Driver",
                        "2.1",
                        2,
                        1));
    }
}
