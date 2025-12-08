package org.litebridge.core;

import jakarta.annotation.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.core.dto.ChangedField;
import org.litebridge.core.dto.TrackedDto;
import org.litebridge.core.persistence.PersistenceFacade;
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
    private final TableRegistry tableRegistry;
    private final PersistenceFacade persistenceFacade;

    public LiteBridge(final DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = new TableRegistry();
        this.persistenceFacade = new PersistenceFacade(tableRegistry, databaseProvider);
    }

    public void register(final Class<?> dtoClass, @Nullable final String catalog, @Nullable final String schema, final String table, final Map<String, String> fieldColumnMap) throws SQLException {
        tableRegistry.addTable(dtoClass, mapToTable(dtoClass, catalog, schema, table, fieldColumnMap));
    }

    public <T> T track(final T dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }

        final Table table = tableRegistry.getTable(dto.getClass());

        if (table == null) {
            throw new IllegalArgumentException("DTO class not registered: '%s'".formatted(dto.getClass().getName()));
        }

        table.trackDto(dto);
        return dto;
    }

    public void save(final Object dto) {
        try {
            persistenceFacade.save(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save DTO: " + dto, ex);
        }
    }

    private Table mapToTable(final Class<?> dtoClass, @Nullable final String catalog, @Nullable final String schema, final String table, final Map<String, String> fieldColumnMap) throws SQLException {
        // Up-front validation
        if (dtoClass == null) {
            throw new IllegalArgumentException("DTO class cannot be null");
        } else if (ClassUtil.isBasicType(dtoClass)) {
            throw new IllegalArgumentException("Not a DTO: " + dtoClass.getName());
        } else if (table == null) {
            throw new IllegalArgumentException("Table name cannot be null");
        } else if (CollectionUtils.isEmpty(fieldColumnMap)) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData = databaseProvider.getTableMetaData(catalog, schema, table);
        final Map<Field, Column> columnMap = mapFields(dtoClass, tableMetaData, fieldColumnMap);
        return new Table(tableMetaData, columnMap);
    }

    private Map<Field, Column> mapFields(final Class<?> dtoClass, final TableMetaData tableMetaData, final Map<String, String> fieldColumnMap) {
        final Set<String> unmappedColumns = new TreeSet<>(tableMetaData.columns().keySet());
        final Map<Field, Column> mappedFields = new HashMap<>();

        // Validate and formalise field mapping
        fieldColumnMap.forEach((fieldName, columnName) -> {
            if (!tableMetaData.columns().containsKey(columnName)) {
                throw new IllegalArgumentException(String.format("Column '%s', mapped by field '%s' of DTO '%s', does not exist in table: '%s'", columnName, fieldName, dtoClass, tableMetaData.table()));
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
            final Column column = tableMetaData.columns().get(columnName);

            if (!ClassUtil.isBasicType(field.getType())
                    && !tableRegistry.containsTable(field.getType())) {
                // Cascading child DTO, but no table mapping exists
                throw new IllegalArgumentException(String.format("Sub-DTO '%s' in field '%s' of DTO '%s' is not registered", field.getType().getName(), fieldName, dtoClass.getName()));
            }

            mappedFields.put(field, column);
            unmappedColumns.remove(columnName);
        });

        // Check for unmapped columns
        if (!unmappedColumns.isEmpty()) {
            // Check if any non-nullable columns are missing
            final List<String> missingColumns = unmappedColumns.stream()
                    .filter(columnName -> !tableMetaData.columns().get(columnName).nullable())
                    .toList();

            if (!missingColumns.isEmpty()) {
                throw new IllegalArgumentException(String.format("Unmapped non-nullable columns for table '%s': %s; DTO class: '%s'", tableMetaData.table(), missingColumns, dtoClass.getName()));
            }
        }

        return mappedFields;
    }
}
