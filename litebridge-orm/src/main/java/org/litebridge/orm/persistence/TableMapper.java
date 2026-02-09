package org.litebridge.orm.persistence;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.spec.AbstractColumnSpecBuilder;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.OneToMany;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class TableMapper {

    private final DatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry;
    private final ChangeTracker changeTracker;

    public TableMapper(final DatabaseProvider databaseProvider, final TableRegistry tableRegistry, final ChangeTracker changeTracker) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = tableRegistry;
        this.changeTracker = changeTracker;
    }

    public OrmTable mapToTable(final Class<?> dtoClass, final TableSpec tableSpec) throws SQLException {
        // Up-front validation
        if (dtoClass == null) {
            throw new IllegalArgumentException("DTO class cannot be null");
        } else if (ClassUtils.isBasicType(dtoClass)) {
            throw new IllegalArgumentException("Not a DTO: " + dtoClass.getName());
        } else if (CollectionUtils.isEmpty(tableSpec.fieldColumnMap())) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData = databaseProvider.getTableMetaData(tableSpec);

        final Map<FieldAccessor, MappedFieldTarget> columnMap = mapFields(dtoClass, tableMetaData, tableSpec.fieldColumnMap());
        return new OrmTable(dtoClass, tableMetaData, columnMap, changeTracker);
    }

    private Map<FieldAccessor, MappedFieldTarget> mapFields(final Class<?> dtoClass, final TableMetaData tableMetaData, final Map<FieldSpec, ColumnMapping> fieldColumnSpecMap) {
        final Set<String> unmappedColumns = tableMetaData.columns().stream()
                .map(ColumnMetaData::name)
                .collect(Collectors.toSet());
        final Map<FieldAccessor, MappedFieldTarget> mappedFields = new HashMap<>();

        // Validate and formalise field mapping
        fieldColumnSpecMap.forEach((fieldSpec, columnMapping) -> {
            switch (columnMapping) {
                case AbstractColumnSpecBuilder<?> columnSpecBuilder -> {
                    final ColumnSpec columnSpec = columnSpecBuilder.build();
                    mapColumnSpec(columnSpec, fieldSpec, dtoClass, tableMetaData, unmappedColumns, mappedFields);
                }
                case ColumnSpec columnSpec ->
                        mapColumnSpec(columnSpec, fieldSpec, dtoClass, tableMetaData, unmappedColumns, mappedFields);
                case OneToMany oneToMany ->
                        mapOneToMany(oneToMany, fieldSpec, dtoClass, tableMetaData, unmappedColumns, mappedFields);
            }
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

    private void mapColumnSpec(final ColumnSpec columnSpec,
                               final FieldSpec fieldSpec,
                               final Class<?> dtoClass,
                               final TableMetaData tableMetaData,
                               final Set<String> unmappedColumns,
                               final Map<FieldAccessor, MappedFieldTarget> mappedFields) {
        if (!tableMetaData.hasColumn(columnSpec.name())) {
            throw new IllegalArgumentException(String.format("Column '%s', mapped by " + (fieldSpec.property() ? "property" : "field") + " '%s' of DTO '%s', does not exist in table: '%s'", columnSpec.name(), fieldSpec.name(), dtoClass, tableMetaData.name()));
        }

        if (!unmappedColumns.contains(columnSpec.name())) {
            // Column is already mapped
            final String conflictingFieldName = mappedFields.entrySet().stream()
                    .filter(fieldColumnEntry -> fieldColumnEntry.getValue() instanceof ColumnMetaData)
                    .filter(fieldColumnEntry -> ((ColumnMetaData) fieldColumnEntry.getValue()).name().equals(columnSpec.name()))
                    .map(Map.Entry::getKey)
                    .map(FieldAccessor::name)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Conflicting field for column '%s' not found; current field spec: '%s'".formatted(columnSpec, fieldSpec)));
            throw new IllegalArgumentException(String.format("Column '%s' is already mapped by field '%s'", columnSpec, conflictingFieldName));
        }

        // Add field-column mapping
        final FieldAccessor fieldAccessor = DtoIntrospector.fieldAccessor(dtoClass, fieldSpec);
        final ColumnMetaData column = Objects.requireNonNull(tableMetaData.column(columnSpec.name()), "Column metadata not found: " + columnSpec.name());

        if (columnSpec.isAutoIncrement()) {
            column.setAutoIncrement(true);

            if (!StringUtils.isBlank(columnSpec.sequence())) {
                column.setSequence(columnSpec.sequence());
            }
        }

        OrmTable nestedTable = null;

        if (!ClassUtils.isBasicType(fieldAccessor.type())) {
            // Check for self-referencing DTOs, then if we know how to persist the nested DTO
            if (fieldAccessor.type() != dtoClass) {
                if (columnSpec.mappedTable() != null) {
                    // In-line DTO-table mapping
                    try {
                        nestedTable = mapToTable(columnSpec.mappedTable().dtoClass(), columnSpec.mappedTable().tableSpec());
                    } catch (SQLException ex) {
                        throw new IllegalStateException("Failed to map nested DTO class '" + columnSpec.mappedTable().dtoClass() + "' to table: " + columnSpec.mappedTable().tableSpec(), ex);
                    }
                } else if (!tableRegistry.containsTable(fieldAccessor.type())) {
                    // Nested child DTO, but no table mapping exists
                    throw new IllegalArgumentException(String.format("Sub-DTO '%s' in field '%s' of DTO '%s' is not registered", fieldAccessor.type().getName(), fieldSpec.name(), dtoClass.getName()));
                }
            }

            if (columnSpec.joinColumn() == null) {
                throw new IllegalArgumentException(String.format("No \"join on\" field specified for sub-DTO '%s' in field '%s' of DTO '%s'", fieldAccessor.type().getName(), fieldSpec.name(), dtoClass.getName()));
            }

            column.setJoinColumn(columnSpec.joinColumn());
        }

        if (nestedTable != null) {
            mappedFields.put(fieldAccessor, new ColumnAndInlineTable(column, nestedTable));
        } else {
            mappedFields.put(fieldAccessor, column);
        }

        unmappedColumns.remove(columnSpec.name());
    }

    private void mapOneToMany(final OneToMany oneToMany,
                              final FieldSpec fieldSpec,
                              final Class<?> dtoClass,
                              final TableMetaData tableMetaData,
                              final Set<String> unmappedColumns,
                              final Map<FieldAccessor, MappedFieldTarget> mappedFields) {
        // Verify we are dealing with a collection
        final FieldAccessor fieldAccessor = DtoIntrospector.fieldAccessor(dtoClass, fieldSpec);

        if (!Collection.class.isAssignableFrom(fieldAccessor.type())) {
            throw new IllegalArgumentException(String.format("Field '%s' of DTO '%s' is not a collection; cannot apply reverse one-to-many mapping. Field type: %s", fieldAccessor.name(), dtoClass.getName(), fieldAccessor.type()));
        }

        // Get the generic type of the collection to detect the target DTO
        final Class<?> targetDto = fieldAccessor.genericType();

        if (ClassUtils.isBasicType(targetDto)) {
            throw new IllegalArgumentException(String.format("Field '%s' of DTO '%s' is a collection of basic type '%s'; cannot apply reverse one-to-many mapping", fieldAccessor.name(), dtoClass.getName(), targetDto.getName()));
        }

        // Get the "mapped by" field from the target DTO of the collection wrapped by the field spec
        final FieldAccessor mappeByField = DtoIntrospector.fieldAccessor(targetDto, new FieldSpec(oneToMany.mappedByField(), false));
        final MappedOneToMany mappedOneToMany = new MappedOneToMany(mappeByField, fieldAccessor);
        mappedFields.put(fieldAccessor, mappedOneToMany);
    }
}
