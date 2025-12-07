package org.litebridge.core;

import jakarta.annotation.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.core.dto.ChangedField;
import org.litebridge.core.dto.TrackedDto;
import org.litebridge.db.api.Column;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.TableMetaData;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LiteBridge {

    private final DatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry = new TableRegistry();

    public LiteBridge(DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    public void register(final Class<?> dtoClass, @Nullable final String catalog, @Nullable final String schema, final String table, final Map<String, String> fieldColumnMap) throws SQLException {
        tableRegistry.addTable(dtoClass, mapToTable(dtoClass, catalog, schema, table, fieldColumnMap));
    }

    public void track(final Object dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }

        final Table table = tableRegistry.getTable(dto.getClass());

        if (table == null) {
            throw new IllegalArgumentException("DTO class not registered: '%s'".formatted(dto.getClass().getName()));
        }

        table.trackDto(dto);
    }

    public void save(final Object dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }

        final Table table = tableRegistry.getTable(dto.getClass());

        if (table == null) {
            throw new IllegalArgumentException("DTO class not registered: '%s'".formatted(dto.getClass().getName()));
        }

        final TrackedDto trackedDto = table.getTrackedDto(dto);

        if (trackedDto == null) {
            throw new IllegalArgumentException("DTO not tracked: '%s'".formatted(dto.toString()));
        }

        final Map<String, ChangedField> changedFields = trackedDto.getChangedFields(dto);

        System.out.println("The following fields have changed:");

        changedFields.entrySet().forEach(entry -> {
            System.out.println(entry.getKey() + " = " + entry.getValue().value());
        });
    }

    private Table mapToTable(final Class<?> dtoClass, @Nullable final String catalog, @Nullable final String schema, final String table, final Map<String, String> fieldColumnMap) throws SQLException {
        // Up-front validation
        if (dtoClass == null) {
            throw new IllegalArgumentException("DTO class cannot be null");
        }

        if (table == null) {
            throw new IllegalArgumentException("Table name cannot be null");
        }

        if (CollectionUtils.isEmpty(fieldColumnMap)) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData = databaseProvider.getTableMetaData(catalog, schema, table);
        final Map<Field, Column> columnMap = mapFields(dtoClass, tableMetaData, fieldColumnMap);
        return new Table(tableMetaData, columnMap);
    }

    private Map<Field, Column> mapFields(final Class<?> dtoClass, final TableMetaData tableMetaData, final Map<String, String> fieldColumnMap) {
        final Map<String, Column> columnMap = tableMetaData.columns().stream()
                .collect(Collectors.toMap(Column::name, Function.identity()));
        final Set<String> unmappedColumns = tableMetaData.columns().stream()
                .map(Column::name)
                .collect(Collectors.toCollection(TreeSet::new));
        final Map<Field, Column> mappedFields = new HashMap<>();

        // Validate and formalise field mapping
        fieldColumnMap.forEach((fieldName, columnName) -> {
            if (!columnMap.containsKey(columnName)) {
                throw new IllegalArgumentException(String.format("Column '%s', mapped by field '%s' of DTO '%s', does not exist in table: '%s'", columnName, fieldName, dtoClass, tableMetaData.tableName()));
            }

            if (!unmappedColumns.contains(columnName)) {
                // Column is already mapped
                final String conflictingFieldName = mappedFields.entrySet().stream()
                        .filter(fieldColumnEntry -> fieldColumnEntry.getValue().name().equals(columnName))
                        .map(Map.Entry::getKey)
                        .map(Field::getName)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Conflicting field for column '%s' not found; current field: '%s'".formatted(columnName, fieldName)));
                throw new IllegalArgumentException(String.format("Column '%s' is already mapped by field '%s'", columnName, conflictingFieldName));
            }

            // Add field-column mapping
            final Field field = ClassUtil.getField(dtoClass, fieldName);
            mappedFields.put(field, columnMap.get(columnName));
            unmappedColumns.remove(columnName);
        });

        // Check for unmapped columns
        if (!unmappedColumns.isEmpty()) {
            // Check if any non-nullable columns are missing
            final List<String> missingColumns = unmappedColumns.stream()
                    .filter(columnName -> !columnMap.get(columnName).nullable())
                    .toList();

            if (!missingColumns.isEmpty()) {
                throw new IllegalArgumentException(String.format("Unmapped non-nullable columns for table '%s': %s; DTO class: '%s'", tableMetaData.tableName(), missingColumns, dtoClass.getName()));
            }
        }

        return mappedFields;
    }
}
