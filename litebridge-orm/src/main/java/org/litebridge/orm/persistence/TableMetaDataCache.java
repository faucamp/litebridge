package org.litebridge.orm.persistence;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.TransactionManager;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableMetaDataCache {

    private final DatabaseProvider databaseProvider;
    private final TransactionManager transactionManager;
    private final Map<String, TableMetaData> tableMetaDataMap = new ConcurrentHashMap<>();

    public TableMetaDataCache(final DatabaseProvider databaseProvider, final TransactionManager transactionManager) {
        this.databaseProvider = databaseProvider;
        this.transactionManager = transactionManager;
    }

    public TableMetaData ensureTableMetaData(final Table table) {
        return tableMetaDataMap.computeIfAbsent(table.qualifiedName(), tableName -> {
            try {
                return databaseProvider.tableMetaData(table, transactionManager);
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to retrieve table metadata for table: " + table.qualifiedName(), e);
            }
        });
    }
}
