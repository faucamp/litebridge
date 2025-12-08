package org.litebridge.db.api;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public interface DatabaseProvider {

    TableMetaData getTableMetaData(String catalog, String schema, String table) throws SQLException;

    Object insert(TableMetaData tableMetaData, Map<String, Object> columnValueMap) throws SQLException;
    Object update(TableMetaData tableMetaData, Map<String, Object> columnValueMap, LinkedHashMap<String, Object> primaryKey) throws SQLException;
}
