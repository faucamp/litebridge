package org.litebridgedb.orm.persistence;

import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.commons.ModuleUtils;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.commons.type.ConcurrentLazy;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.MappedFieldTarget;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.ManyToMany;
import org.litebridgedb.orm.api.spec.NoFieldMapping;
import org.litebridgedb.orm.api.spec.OneToMany;
import org.litebridgedb.orm.api.spec.TableSpec;
import org.litebridgedb.orm.persistence.manytomany.HiddenJoinEntity;
import org.litebridgedb.orm.persistence.manytomany.NoOpFieldAccessor;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class TableMapper {

    private final TransactionalDatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry;
    private final ChangeTracker changeTracker;
    private final ClassFieldAccessorCache classFieldAccessorCache;

    public TableMapper(final TransactionalDatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry,
                       final ChangeTracker changeTracker) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = tableRegistry;
        this.changeTracker = changeTracker;
        this.classFieldAccessorCache = changeTracker.classFieldAccessorCache();
    }

    public MappedTable mapToTable(final MethodHandles.Lookup lookup, final Class<?> dtoClass, final TableSpec tableSpec, final Set<Class<?>> allDtoClasses) {
        // Up-front validation
        Objects.requireNonNull(lookup, "MethodHandles lookup is required for reflection");
        Objects.requireNonNull(dtoClass, "DTO class cannot be null");

        // HiddenJoinEntity is used for intermediate join tables and is a JDK proxy - don't check module accessiblity for those
        if (!HiddenJoinEntity.class.isAssignableFrom(dtoClass)) {
            ModuleUtils.requireAccessible(dtoClass);
        }

        if (ClassUtils.isBasicType(dtoClass)) {
            throw new IllegalArgumentException("Not a DTO: " + dtoClass.getName());
        } else if (CollectionUtils.isEmpty(tableSpec.fieldColumnMap())) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData;
        try {
            tableMetaData = ObjectUtils.requireNonNull(databaseProvider.tableMetaData(tableSpec, databaseProvider.transactionManager()), () -> new IllegalStateException("Database provider returned null table metadata for table: " + tableSpec.name()));
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to read table metadata for table:" + tableSpec, ex);
        }

        final MappedDto mappedDto = mapFields(lookup, dtoClass, tableMetaData, tableSpec.fieldColumnMap(), allDtoClasses);
        final OrmTable ormTable = new OrmTable(dtoClass, tableMetaData, mappedDto.mappedFields(), changeTracker, classFieldAccessorCache);
        return new MappedTable(ormTable, mappedDto.manyToOneDependencies());
    }

    private MappedDto mapFields(final MethodHandles.Lookup lookup,
                                final Class<?> dtoClass,
                                final TableMetaData tableMetaData,
                                final Map<FieldMapping, ColumnMapping> fieldColumnSpecMap,
                                final Set<Class<?>> allDtoClasses) {
        final Set<String> unmappedColumns = tableMetaData.columns().stream()
                .map(ColumnMetaData::name)
                .collect(Collectors.toSet());
        final Map<FieldAccessor, MappedFieldTarget> mappedFields = new HashMap<>();
        final List<FieldAccessor> manyToOneDependencies = new ArrayList<>();

        // Validate and formalise field mapping
        fieldColumnSpecMap.forEach((fieldMapping, columnMapping) -> {
            switch (columnMapping) {
                case ColumnSpec columnSpec ->
                        mapColumnSpec(columnSpec, fieldMapping, lookup, dtoClass, tableMetaData, unmappedColumns, mappedFields, manyToOneDependencies, allDtoClasses);
                case OneToMany oneToMany ->
                        mapOneToMany(oneToMany, (FieldSpec) fieldMapping, dtoClass, tableMetaData, unmappedColumns, mappedFields, manyToOneDependencies);
                case ManyToMany manyToMany ->
                        mapManyToMany(manyToMany, (FieldSpec) fieldMapping, lookup, dtoClass, tableMetaData, unmappedColumns, mappedFields, manyToOneDependencies);
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

        return new MappedDto(mappedFields, manyToOneDependencies);
    }

    private void mapColumnSpec(final ColumnSpec columnSpec,
                               final FieldMapping fieldMapping,
                               final MethodHandles.Lookup lookup, final Class<?> dtoClass,
                               final TableMetaData tableMetaData,
                               final Set<String> unmappedColumns,
                               final Map<FieldAccessor, MappedFieldTarget> mappedFields,
                               final List<FieldAccessor> manyToOneDependencies,
                               final Set<Class<?>> allDtoClasses) {
        if (!tableMetaData.hasColumn(columnSpec.name())) {
            if (fieldMapping instanceof FieldSpec fieldSpec) {
                throw new IllegalArgumentException(String.format("Column '%s', mapped by %s '%s' of DTO '%s', does not exist in table: '%s'", columnSpec.name(), (fieldSpec.property() ? "property" : "field"), fieldSpec.name(), dtoClass, tableMetaData.name()));
            } else {
                throw new IllegalArgumentException(String.format("Column '%s', mapped by '%s' of DTO '%s', does not exist in table: '%s'", columnSpec.name(), fieldMapping, dtoClass, tableMetaData.name()));
            }
        }

        if (!unmappedColumns.contains(columnSpec.name())) {
            // Column is already mapped
            final String conflictingFieldName = mappedFields.entrySet().stream()
                    .filter(fieldColumnEntry -> fieldColumnEntry.getValue() instanceof ColumnMetaData)
                    .filter(fieldColumnEntry -> ((ColumnMetaData) fieldColumnEntry.getValue()).name().equals(columnSpec.name()))
                    .map(Map.Entry::getKey)
                    .map(FieldAccessor::name)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Conflicting field for column '%s' not found; current field mapping: '%s'".formatted(columnSpec, fieldMapping)));
            throw new IllegalArgumentException(String.format("Column '%s' is already mapped by field '%s'", columnSpec, conflictingFieldName));
        }

        // Add field-column mapping
        final FieldAccessor fieldAccessor;

        if (fieldMapping instanceof FieldSpec fieldSpec) {
            fieldAccessor = fieldAccessor(dtoClass, fieldSpec);
        } else {
            fieldAccessor = new NoOpFieldAccessor();
        }

        final ColumnMetaData column = Objects.requireNonNull(tableMetaData.column(columnSpec.name()), "Column metadata not found: " + columnSpec.name());

        if (columnSpec.generator() != null) {
            column.setGenerator(columnSpec.generator());
        }

        MappedTable nestedTable = null;

        if (!(fieldAccessor instanceof NoOpFieldAccessor) && !ClassUtils.isBasicType(fieldAccessor.type())) {
            // Check for self-referencing DTOs, then if we know how to persist the nested DTO
            if (fieldAccessor.type() != dtoClass) {
                if (columnSpec.mappedTable() != null) {
                    // In-line DTO-table mapping
                    try {
                        nestedTable = mapToTable(lookup, columnSpec.mappedTable().dtoClass(), columnSpec.mappedTable().tableSpec(), allDtoClasses);
                        manyToOneDependencies.addAll(nestedTable.manyToOneDependencies());
                    } catch (Exception ex) {
                        throw new IllegalStateException("Failed to map nested DTO class '" + columnSpec.mappedTable().dtoClass() + "' to table: " + columnSpec.mappedTable().tableSpec(), ex);
                    }
                } else if (!tableRegistry.containsTable(fieldAccessor.type())
                        && !allDtoClasses.contains(fieldAccessor.type())) {
                    // Nested child DTO, but no table mapping exists
                    throw new IllegalArgumentException(String.format("Referenced DTO not registered: '%s', in field '%s' of DTO '%s'", fieldAccessor.type().getName(), fieldAccessor.name(), dtoClass.getName()));
                }
            }

            if (columnSpec.joinColumn() == null) {
                throw new IllegalArgumentException(String.format("No \"join on\" field specified for referenced DTO '%s' in field '%s' of DTO '%s'", fieldAccessor.type().getName(), fieldAccessor.name(), dtoClass.getName()));
            }

            column.setJoinColumn(columnSpec.joinColumn());
        } else {
            column.setJoinColumn(columnSpec.joinColumn());
        }

        if (nestedTable != null) {
            mappedFields.put(fieldAccessor, new ColumnAndInlineTable(column, nestedTable.ormTable()));
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
                              final Map<FieldAccessor, MappedFieldTarget> mappedFields,
                              final List<FieldAccessor> manyToOneDependencies) {
        // Verify we are dealing with a collection
        final FieldAccessor fieldAccessor = fieldAccessor(dtoClass, fieldSpec);
        final Class<?> targetDto = getJoinTargetDto(dtoClass, fieldAccessor);

        // Get the "mapped by" field from the target DTO of the collection wrapped by the field spec
        final FieldAccessor mappedByField = fieldAccessor(targetDto, oneToMany.mappedByField());
        final MappedOneToMany mappedOneToMany = new MappedOneToMany(mappedByField, fieldAccessor);

        mappedFields.put(fieldAccessor, mappedOneToMany);

        // Indicate the reverse dependency on the target DTO
        manyToOneDependencies.add(fieldAccessor);
    }

    private void mapManyToMany(final ManyToMany manyToMany,
                               final FieldSpec fieldSpec,
                               final MethodHandles.Lookup lookup,
                               final Class<?> dtoClass,
                               final TableMetaData tableMetaData,
                               final Set<String> unmappedColumns,
                               final Map<FieldAccessor, MappedFieldTarget> mappedFields,
                               final List<FieldAccessor> manyToOneDependencies) {
        // Verify we are dealing with a collection
        final FieldAccessor fieldAccessor = fieldAccessor(dtoClass, fieldSpec);
        final OrmTable joinTable = ensureManyToManyJoinTable(manyToMany, lookup, manyToOneDependencies);

        final Class<?> targetDto = getJoinTargetDto(dtoClass, fieldAccessor);
        final ConcurrentLazy<OrmTable> targetTable = new ConcurrentLazy<>(() -> tableRegistry.getTableOrThrow(targetDto));

        final MappedManyToMany mappedManyToMany = new MappedManyToMany(
                joinTable,
                manyToMany.joinColumn(),
                fieldAccessor,
                targetTable,
                manyToMany.inverseJoinColumn());
        mappedFields.put(fieldAccessor, mappedManyToMany);
    }

    private static Class<?> getJoinTargetDto(final Class<?> dtoClass, final FieldAccessor fieldAccessor) {
        if (!Collection.class.isAssignableFrom(fieldAccessor.type())) {
            throw new IllegalArgumentException(String.format("Field '%s' of DTO '%s' is not a collection; cannot apply reverse one-to-many mapping. Field type: %s", fieldAccessor.name(), dtoClass.getName(), fieldAccessor.type()));
        }

        // Get the generic type of the collection to detect the target DTO
        final Class<?> targetDto = fieldAccessor.genericType();

        if (ClassUtils.isBasicType(targetDto)) {
            throw new IllegalArgumentException(String.format("Field '%s' of DTO '%s' is a collection of basic type '%s'; cannot apply reverse one-to-many mapping", fieldAccessor.name(), dtoClass.getName(), targetDto.getName()));
        }

        return targetDto;
    }

    private OrmTable ensureManyToManyJoinTable(final ManyToMany manyToMany, final MethodHandles.Lookup lookup, final List<FieldAccessor> manyToOneDependencies) {
        final OrmTable joinTable = tableRegistry.getTable(manyToMany.joinTable());

        if (joinTable != null) {
            return joinTable;
        } else {
            final MappedTable mappedJoinTable = mapManyToManyJoinTable(manyToMany, lookup);
            manyToOneDependencies.addAll(mappedJoinTable.manyToOneDependencies());
            return mappedJoinTable.ormTable();
        }
    }

    private MappedTable mapManyToManyJoinTable(final ManyToMany manyToMany, final MethodHandles.Lookup lookup) {
        final ColumnSpec joinColumnSpec = new ColumnSpec(manyToMany.joinColumn(), null, manyToMany.joinColumn());
        final ColumnSpec inverseJoinColumnSpec = new ColumnSpec(manyToMany.inverseJoinColumn(), null, manyToMany.inverseJoinColumn());

        final TableSpec tableSpec = new TableSpec(manyToMany.joinTable(), Map.of(
                new NoFieldMapping(), joinColumnSpec,
                new NoFieldMapping(), inverseJoinColumnSpec));

        final Class<?> hiddenJoinClass = Proxy.getProxyClass(HiddenJoinEntity.class.getClassLoader(), HiddenJoinEntity.class);

        try {
            return mapToTable(lookup, hiddenJoinClass, tableSpec, Collections.emptySet());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to map many-to-many join table from spec: " + manyToMany, ex);
        }
    }

    private FieldAccessor fieldAccessor(final Class<?> dtoClass, final FieldSpec fieldSpec) {
        if (fieldSpec.property()) {
            return classFieldAccessorCache.propertyAccessor(dtoClass, fieldSpec.name());
        } else {
            return classFieldAccessorCache.fieldAccessor(dtoClass, fieldSpec.name());
        }
    }

    private record MappedDto(Map<FieldAccessor, MappedFieldTarget> mappedFields,
                             List<FieldAccessor> manyToOneDependencies) {
    }

    public record MappedTable(OrmTable ormTable, List<FieldAccessor> manyToOneDependencies) {
    }
}
