package org.litebridge.db.api;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.litebridge.db.api.convert.TypeConverter;
import org.litebridge.db.api.query.Condition;

import java.sql.ResultSet;
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

    List<Map<String, Object>> select(TableMetaData tableMetaData, List<String> columns, List<Condition> conditions) throws SQLException;

    @Nonnull
    TypeConverter getTypeConverter();
}
