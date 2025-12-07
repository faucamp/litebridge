package org.litebridge.db.api;

import java.sql.SQLException;

public interface DatabaseProvider {

    TableMetaData getTableMetaData(String catalog, String schema, String table) throws SQLException;
}
