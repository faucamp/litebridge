package org.litebridge.orm;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.dto.DtoSelector;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.api.sql.SqlFromClause;
import org.litebridge.orm.api.sql.SqlSelector;
import org.litebridge.orm.persistence.DefaultDtoMapper;
import org.litebridge.orm.persistence.DtoAliasRegistry;
import org.litebridge.orm.persistence.DtoIntrospector;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.orm.persistence.PersistenceFacade;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ChangeTracker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Litebridge {

    private static final Aliased[] ALL_COLUMNS = new Aliased[0];

    private final TableRegistry tableRegistry = new TableRegistry();
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

        final OrmTable table = tableRegistry.getTable(dto.getClass());

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
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);
        final DtoAliasRegistry dtoAliasRegistry = new DtoAliasRegistry();
        final DtoMapper dtoMapper = new DefaultDtoMapper(tableRegistry, databaseProvider.getTypeConverter(), dtoAliasRegistry);
        return new DtoSelector<>(dtoClass, table, tableRegistry, databaseProvider, dtoMapper, dtoAliasRegistry).select();
    }

    public SqlFromClause select(final String... columns) {
        return new SqlSelector(databaseProvider, tableRegistry).select(columns);
    }

    public SqlFromClause select(final Aliased... columns) {
        return new SqlSelector(databaseProvider, tableRegistry).select(columns);
    }

    public SqlFromClause select() {
        return new SqlSelector(databaseProvider, tableRegistry).select(ALL_COLUMNS);
    }

    private OrmTable mapToTable(final Class<?> dtoClass, final TableSpec tableSpec) throws SQLException {
        // Up-front validation
        if (dtoClass == null) {
            throw new IllegalArgumentException("DTO class cannot be null");
        } else if (ClassUtils.isBasicType(dtoClass)) {
            throw new IllegalArgumentException("Not a DTO: " + dtoClass.getName());
        } else if (CollectionUtils.isEmpty(tableSpec.fieldColumnSpecMap())) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData = databaseProvider.getTableMetaData(tableSpec);

        final Map<FieldAccessor, ColumnMetaData> columnMap = mapFields(dtoClass, tableMetaData, tableSpec.fieldColumnSpecMap());
        return new OrmTable(tableMetaData, columnMap, changeTracker);
    }

    private Map<FieldAccessor, ColumnMetaData> mapFields(final Class<?> dtoClass, final TableMetaData tableMetaData, final Map<FieldSpec, ColumnSpec> fieldColumnSpecMap) {
        final Set<String> unmappedColumns = tableMetaData.columns().stream()
                .map(ColumnMetaData::name)
                .collect(Collectors.toSet());
        final Map<FieldAccessor, ColumnMetaData> mappedFields = new HashMap<>();

        // Validate and formalise field mapping
        fieldColumnSpecMap.forEach((fieldSpec, columnSpec) -> {
            if (!tableMetaData.hasColumn(columnSpec.name())) {
                throw new IllegalArgumentException(String.format("Column '%s', mapped by field spec '%s' of DTO '%s', does not exist in table: '%s'", columnSpec, fieldSpec, dtoClass, tableMetaData.name()));
            }

            if (!unmappedColumns.contains(columnSpec.name())) {
                // Column is already mapped
                final String conflictingFieldName = mappedFields.entrySet().stream()
                        .filter(fieldColumnEntry -> fieldColumnEntry.getValue().name().equals(columnSpec.name()))
                        .map(Map.Entry::getKey)
                        .map(FieldAccessor::name)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Conflicting field for column '%s' not found; current field spec: '%s'".formatted(columnSpec, fieldSpec)));
                throw new IllegalArgumentException(String.format("Column '%s' is already mapped by field '%s'", columnSpec, conflictingFieldName));
            }

            // Add field-column mapping
            final FieldAccessor fieldAccessor = DtoIntrospector.fieldAccessor(dtoClass, fieldSpec);
            final ColumnMetaData column = ObjectUtils.requireNonNull(tableMetaData.column(columnSpec.name()), "Column metadata not found: " + columnSpec.name());

            if (!StringUtils.isBlank(columnSpec.sequence())) {
                column.setSequence(columnSpec.sequence());
            }

            if (!ClassUtils.isBasicType(fieldAccessor.type())) {
                if (!tableRegistry.containsTable(fieldAccessor.type())) {
                    // Cascading child DTO, but no table mapping exists
                    throw new IllegalArgumentException(String.format("Sub-DTO '%s' in field '%s' of DTO '%s' is not registered", fieldAccessor.type().getName(), fieldSpec.name(), dtoClass.getName()));
                }

                if (columnSpec.joinColumn() == null) {
                    throw new IllegalArgumentException(String.format("No \"join on\" field specified for sub-DTO '%s' in field '%s' of DTO '%s'", fieldAccessor.type().getName(), fieldSpec.name(), dtoClass.getName()));
                }

                column.setJoinColumn(columnSpec.joinColumn());
            }

            mappedFields.put(fieldAccessor, column);
            unmappedColumns.remove(columnSpec.name());
        });

        // Check for unmapped columns
        if (!unmappedColumns.isEmpty()) {
            // Check if any non-nullable columns are missing
            final List<String> missingColumns = unmappedColumns.stream()
                    .filter(columnName -> !tableMetaData.column(columnName).isNullable())
                    .toList();

            if (!missingColumns.isEmpty()) {
                throw new IllegalArgumentException(String.format("Unmapped non-nullable columns for table '%s': %s; DTO class: '%s'", tableMetaData.name(), missingColumns, dtoClass.getName()));
            }
        }

        return mappedFields;
    }

    public <DTO> DTO toDto(final Row row, final Class<DTO> dtoClass) {
        return new DefaultDtoMapper(tableRegistry, databaseProvider.getTypeConverter(), new DtoAliasRegistry())
                .toDto(row, dtoClass);
    }
}
