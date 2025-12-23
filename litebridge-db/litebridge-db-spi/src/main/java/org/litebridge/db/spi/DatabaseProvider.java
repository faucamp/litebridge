package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Select;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface DatabaseProvider {

    TableMetaData getTableMetaData(String catalog, String schema, String table) throws SQLException;

    @Nullable
    List<Object> insert(TableMetaData tableMetaData, Map<String, Object> columnValueMap) throws SQLException;

    @Nullable
    List<Object> update(TableMetaData tableMetaData, Map<String, Object> columnValueMap, LinkedHashMap<String, Object> primaryKey) throws SQLException;

    List<Map<String, Object>> select(Select select) throws SQLException;
    
    TypeConverter getTypeConverter();
}
