//package org.litebridge.core;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import java.sql.DatabaseMetaData;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//public class TableSnap {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(TableSnap.class);
//
//    private final JdbcTemplate jdbcTemplate;
//    private final String schema;
//    private final String table;
//    private final List<String> columns;
//    private final List<String> primaryKeyColumnNames;
//    private final List<TableSnapshot> snapshots = new ArrayList<>();
//
//    public TableSnap(JdbcTemplate jdbcTemplate, String schema, String table) {
//        this.jdbcTemplate = jdbcTemplate;
//        this.schema = schema;
//        this.table = table;
//
//        try {
//            this.columns = this.getColumnNames();
//            this.primaryKeyColumnNames = getPrimaryKeyColumnNames();
//        } catch (SQLException e) {
//            throw new IllegalStateException("Failed to load table metadata for table %s.%s".formatted(schema, table), e);
//        }
//    }
//
//    private List<String> getColumnNames() throws SQLException {
//        final DatabaseMetaData databaseMetaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
//
//        final ResultSet cols = databaseMetaData.getColumns("", schema, table, null);
//        final List<String> columnNames = new ArrayList<>();
//
//        while (cols.next()) {
//            String columnName = cols.getString("COLUMN_NAME");
//            columnNames.add(columnName);
//        }
//
//        return columnNames;
//    }
//
//    private List<String> getPrimaryKeyColumnNames() throws SQLException {
//        final DatabaseMetaData databaseMetaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
//
//        final ResultSet primaryKeys = databaseMetaData.getPrimaryKeys("", schema, table);
//        final List<String> primaryKeyColumnNames = new ArrayList<>();
//
//        while (primaryKeys.next()) {
//            String columnName = primaryKeys.getString("COLUMN_NAME");
//            primaryKeyColumnNames.add(columnName);
//        }
//
//        return primaryKeyColumnNames;
//    }
//
//    public int snapshotAndLog() {
//        final int snapshotIndex = snapshot();
//        logContents(snapshotIndex);
//        return snapshotIndex;
//    }
//
//    public int snapshot() {
//        final List<List<Object>> rows = new ArrayList<>();
//
//        jdbcTemplate.query("SELECT * FROM %s.%s".formatted(schema, table), rs -> {
//            final List<Object> row = new ArrayList<>();
//
//            for (String columnName : columns) {
//                row.add(rs.getObject(columnName));
//            }
//
//            rows.add(row);
//        });
//
//        final TableSnapshot snapshot = new TableSnapshot(rows);
//        snapshots.add(snapshot);
//        return snapshots.size() - 1;
//    }
//
//    public void logLastSnapshot() {
//        logContents(snapshots.size() - 1);
//    }
//
//    public void logLastDiff() {
//        if (snapshots.size() < 2) {
//            LOGGER.info("\n{}.{}:\nNot enough snapshots to calculate diff\n", schema, table);
//            return;
//        }
//
//        final TableSnapshot newSnapshot = snapshots.get(snapshots.size() - 1);
//        final TableSnapshot oldSnapshot = snapshots.get(snapshots.size() - 2);
//        final TableDiff diff = oldSnapshot.compare(newSnapshot);
//
//        if (diff.isEmpty()) {
//            LOGGER.info("\n{}.{}:\nNo differences\n", schema, table);
//        } else {
//            if (!diff.addedRows.isEmpty()) {
//                LOGGER.info("\n{}.{}:\nAdded rows:\n{}\n", schema, table, StringUtils.join(diff.addedRows, "\n"));
//            }
//
//            if (!diff.deletedRows.isEmpty()) {
//                LOGGER.info("\n{}.{}:\nDeleted rows:\n{}\n", schema, table, StringUtils.join(diff.deletedRows, "\n"));
//            }
//
//            if (!diff.changedValues.isEmpty()) {
//                final List<List<String>> changesPerRow = new LinkedList<>();
//
//                diff.changedValues.forEach((rowKey, changedValuesForRow) -> {
//                    final List<String> changedValues = changedValuesForRow.stream()
//                            .map(coords -> '[' + rowKey.substring(0, rowKey.length() - 1) + "] " + columns.get(coords.column) + " = " + oldSnapshot.getRows().get(rowKey).get(coords.column) + " -> " + newSnapshot.getRows().get(rowKey).get(coords.column))
//                            .toList();
//
//                    changesPerRow.add(changedValues);
//                });
//
//                changesPerRow.forEach(row -> LOGGER.info("\n{}.{}:\nChanged values:\n{}\n", schema, table, StringUtils.join(row, "\n")));
//            }
//        }
//    }
//
//    private void logContents(final int snapshotIndex) {
//        final TableSnapshot snapshot = snapshots.get(snapshotIndex);
//        final boolean[] headers = {true};
//        final StringBuilder sb = new StringBuilder();
//        sb.append(StringUtils.join(columns, ",")).append('\n');
//
//        snapshot.getRows().forEach((rowKey, row) -> {
//            sb.append(StringUtils.join(row, ",")).append('\n');
//        });
//
//        LOGGER.info("\n{}.{}:\n{}\n", schema, table, sb.toString());
//    }
//
//    private class TableSnapshot {
//
//        private final LinkedHashMap<String, List<Object>> rows = new LinkedHashMap<>();
//
//        public TableSnapshot(List<List<Object>> rows) {
//            for (List<Object> row : rows) {
//                final String rowKey = primaryKeyColumnNames.stream()
//                        .reduce("", (acc, columnName) -> acc + row.get(columns.indexOf(columnName)) + ",");
//                this.rows.put(rowKey, row);
//            }
//        }
//
//        public LinkedHashMap<String, List<Object>> getRows() {
//            return rows;
//        }
//
//        public TableDiff compare(final TableSnapshot newSnapshot) {
//            final List<List<Object>> deletedRows = new ArrayList<>();
//            final List<List<Object>> addedRows = new ArrayList<>();
//            final LinkedHashMap<String, List<ValueCoordinates>> changedValues = new LinkedHashMap<>();
//            int rowNo = 0;
//
//            for (Map.Entry<String, List<Object>> entry : rows.entrySet()) {
//                final String rowKey = entry.getKey();
//                final List<Object> row = entry.getValue();
//
//                final List<Object> newRow = newSnapshot.getRows().get(rowKey);
//
//                if (newRow == null) {
//                    deletedRows.add(row);
//                } else {
//                    final List<ValueCoordinates> changedValuesForRow = new ArrayList<>();
//
//                    for (int colNo = 1; colNo < columns.size(); colNo++) {
//                        if (!Objects.equals(row.get(colNo), newRow.get(colNo))) {
//                            changedValuesForRow.add(new ValueCoordinates(rowNo, colNo));
//                        }
//                    }
//
//                    if (!changedValuesForRow.isEmpty()) {
//                        changedValues.put(rowKey, changedValuesForRow);
//                    }
//                }
//
//                rowNo++;
//            }
//
//            for (Map.Entry<String, List<Object>> entry : newSnapshot.getRows().entrySet()) {
//                final String rowKey = entry.getKey();
//
//                if (rows.get(rowKey) == null) {
//                    addedRows.add(entry.getValue());
//                }
//            }
//
//            return new TableDiff(addedRows, deletedRows, changedValues);
//        }
//
//
//    }
//
//    private record ValueCoordinates(int row, int column) {}
//
//    private record TableDiff(List<List<Object>> addedRows, List<List<Object>> deletedRows,
//                             Map<String, List<ValueCoordinates>> changedValues) {
//
//        public boolean isEmpty() {
//            return addedRows.isEmpty() && deletedRows.isEmpty() && changedValues.isEmpty();
//        }
//    }
//}
