package org.litebridge.db.api;

import jakarta.annotation.Nullable;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface DatabaseProvider {

    TableMetaData getTableMetaData(String catalog, String schema, String table) throws SQLException;

    @Nullable
    List<Object> insert(TableMetaData tableMetaData, Map<String, Object> columnValueMap) throws SQLException;

    @Nullable
    List<Object> update(TableMetaData tableMetaData, Map<String, Object> columnValueMap, LinkedHashMap<String, Object> primaryKey) throws SQLException;
}
