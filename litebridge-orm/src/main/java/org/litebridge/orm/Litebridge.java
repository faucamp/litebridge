package org.litebridge.orm;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.dto.DtoSelector;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.api.sql.SqlFromClause;
import org.litebridge.orm.api.sql.SqlSelector;
import org.litebridge.orm.persistence.DefaultDtoMapper;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.DtoMapperRegistry;
import org.litebridge.orm.persistence.PersistenceFacade;
import org.litebridge.orm.persistence.Table;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ChangeTracker;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Litebridge {

    private final TableRegistry tableRegistry = new TableRegistry();
    private final DtoMapperRegistry dtoMapperRegistry = new DtoMapperRegistry();
    private final ChangeTracker changeTracker = new ChangeTracker();
    private final DatabaseProvider databaseProvider;
    private final PersistenceFacade persistenceFacade;

    public Litebridge(final DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
        this.persistenceFacade = new PersistenceFacade(tableRegistry, databaseProvider);
    }

    public void register(final Class<?> dtoClass, final TableSpec tableSpec) throws SQLException {
        tableRegistry.addTable(dtoClass, mapToTable(dtoClass, tableSpec));
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

    /**
     * Selects a registered Data Transfer Object (DTO) type for database query operations.
     *
     * @param <DTO>    The type of the DTO to select.
     * @param dtoClass The class of the DTO to be queried, which must already be registered.
     * @return A {@link DtoSelector} instance for querying and retrieving data for the specified DTO class.
     * @throws IllegalArgumentException if the specified DTO class is not registered in the table registry.
     */
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass) {
        final Table table = tableRegistry.getTableOrThrow(dtoClass);
        final DtoMapper<DTO> dtoMapper = dtoMapperRegistry.ensureDtoMapper(dtoClass, () -> new DefaultDtoMapper<>(dtoClass, table, databaseProvider.getTypeConverter()));
        return new DtoSelector<>(dtoClass, table, databaseProvider, dtoMapper).selectAll();
    }

    public SqlFromClause select(final String... columns) {
        return new SqlSelector(databaseProvider, tableRegistry).select(columns);
    }

    private Table mapToTable(final Class<?> dtoClass, final TableSpec tableSpec) throws SQLException {
        // Up-front validation
        if (dtoClass == null) {
            throw new IllegalArgumentException("DTO class cannot be null");
        } else if (ClassUtils.isBasicType(dtoClass)) {
            throw new IllegalArgumentException("Not a DTO: " + dtoClass.getName());
        } else if (CollectionUtils.isEmpty(tableSpec.getFieldColumnSpecMap())) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData = databaseProvider.getTableMetaData(tableSpec.getCatalog(), tableSpec.getSchema(), tableSpec.getTable());

        final Map<Field, Column> columnMap = mapFields(dtoClass, tableMetaData, tableSpec.getFieldColumnSpecMap());
        return new Table(tableMetaData, columnMap, changeTracker);
    }

    private Map<Field, Column> mapFields(final Class<?> dtoClass, final TableMetaData tableMetaData, final Map<String, ColumnSpec> fieldColumnSpecMap) {
        final Set<String> unmappedColumns = new TreeSet<>(tableMetaData.getColumns().keySet());
        final Map<Field, Column> mappedFields = new HashMap<>();

        // Validate and formalise field mapping
        fieldColumnSpecMap.forEach((fieldName, columnSpec) -> {
            if (!tableMetaData.getColumns().containsKey(columnSpec.getName())) {
                throw new IllegalArgumentException(String.format("Column '%s', mapped by field '%s' of DTO '%s', does not exist in table: '%s'", columnSpec, fieldName, dtoClass, tableMetaData.getTable()));
            }

            if (!unmappedColumns.contains(columnSpec.getName())) {
                // Column is already mapped
                final String conflictingFieldName = mappedFields.entrySet().stream()
                        .filter(fieldColumnEntry -> fieldColumnEntry.getValue().getName().equals(columnSpec.getName()))
                        .map(Map.Entry::getKey)
                        .map(Field::getName)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Conflicting field for column '%s' not found; current field: '%s'".formatted(columnSpec, fieldName)));
                throw new IllegalArgumentException(String.format("Column '%s' is already mapped by field '%s'", columnSpec, conflictingFieldName));
            }

            // Add field-column mapping
            final Field field = ClassUtils.getField(dtoClass, fieldName);
            final Column column = tableMetaData.getColumns().get(columnSpec.getName());

            if (!StringUtils.isBlank(columnSpec.getSequence())) {
                column.setSequence(columnSpec.getSequence());
            }

            if (!ClassUtils.isBasicType(field.getType())
                    && !tableRegistry.containsTable(field.getType())) {
                // Cascading child DTO, but no table mapping exists
                throw new IllegalArgumentException(String.format("Sub-DTO '%s' in field '%s' of DTO '%s' is not registered", field.getType().getName(), fieldName, dtoClass.getName()));
            }

            mappedFields.put(field, column);
            unmappedColumns.remove(columnSpec.getName());
        });

        // Check for unmapped columns
        if (!unmappedColumns.isEmpty()) {
            // Check if any non-nullable columns are missing
            final List<String> missingColumns = unmappedColumns.stream()
                    .filter(columnName -> !tableMetaData.getColumns().get(columnName).isNullable())
                    .toList();

            if (!missingColumns.isEmpty()) {
                throw new IllegalArgumentException(String.format("Unmapped non-nullable columns for table '%s': %s; DTO class: '%s'", tableMetaData.getTable(), missingColumns, dtoClass.getName()));
            }
        }

        return mappedFields;
    }

    public <DTO> DTO toDto(final LinkedHashMap<String, Object> row, final Class<DTO> dtoClass) {
        return dtoMapperRegistry.getDtoMapperOrThrow(dtoClass).toDto(row);
    }
}
