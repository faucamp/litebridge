package org.litebridge.db.api;

import java.util.List;

public record TableMetaData(String tableName, List<String> primaryKey, List<Column> columns) {
}
