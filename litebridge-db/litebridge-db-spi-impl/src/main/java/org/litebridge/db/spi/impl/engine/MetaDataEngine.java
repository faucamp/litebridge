package org.litebridge.db.spi.impl.engine;

import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.sql.SQLException;

public interface MetaDataEngine {
    /**
     * Retrieve metadata/capabilities of this database provider.
     *
     * @return metadata for this database provider.
     */
    DatabaseProviderMetaData metaData();

    /**
     * Retrieve metadata for the connected database.
     *
     * @param connectionProvider the {@link ConnectionProvider} used to get a database connection.
     * @return a {@link DatabaseMetaData} object containing information about the connected database.
     * @throws SQLException if any SQL error occurs while retrieving the metadata.
     */
    DatabaseMetaData databaseMetaData(ConnectionProvider connectionProvider);

    /**
     * Ensure that table metadata is available for the specified table, fetching it if not already cached.
     *
     * @param table              the {@link Table} object representing the table
     * @param connectionProvider the {@link ConnectionProvider} used to fetch metadata if needed
     * @return the {@link TableMetaData} for the table
     */
    TableMetaData ensureTableMetaData(Table table, ConnectionProvider connectionProvider);

}
