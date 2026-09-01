package org.litebridge.orm.persistence;

import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ModuleUtils;
import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.api.spec.ColumnMapping;
import org.litebridge.orm.api.spec.ColumnSpec;
import org.litebridge.orm.api.spec.FieldMapping;
import org.litebridge.orm.api.spec.FieldSpec;
import org.litebridge.orm.api.spec.ManyToMany;
import org.litebridge.orm.api.spec.NoFieldMapping;
import org.litebridge.orm.api.spec.OneToMany;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.persistence.manytomany.HiddenJoinEntity;
import org.litebridge.orm.persistence.manytomany.NoOpFieldAccessor;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A utility class responsible for mapping DTOs to database tables and managing
 * the relationships between them.
 * <p>
 * The primary function of this class is to provide
 * mappings for various table specifications, handle entity relationships like
 * one-to-many and many-to-many mappings, and facilitate metadata management for
 * database operations.
 */
public final class TableMapper {

    private final TransactionalDatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry;
    private final ChangeTracker changeTracker;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final TableMetaDataCache tableMetaDataCache;

    public TableMapper(final TransactionalDatabaseProvider databaseProvider,
                       final TableRegistry tableRegistry,
                       final ChangeTracker changeTracker,
                       final TableMetaDataCache tableMetaDataCache) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = tableRegistry;
        this.changeTracker = changeTracker;
        this.classFieldAccessorCache = changeTracker.classFieldAccessorCache();
        this.tableMetaDataCache = tableMetaDataCache;
    }

    /**
     * Maps a Data Transfer Object (DTO) class to a database table based on the provided specification.
     * <p>
     * Performs validation, ensures proper module accessibility, and generates metadata necessary
     * for runtime table interactions.
     *
     * @param lookup        the {@link MethodHandles.Lookup} instance for reflection; must not be null
     * @param dtoClass      the DTO class to be mapped to the database table; must not be null
     * @param tableSpec     the specification of the database table, including field-column mappings; must not be null
     * @param allDtoClasses a set of all DTO classes involved in the ORM mapping; must not be null
     * @return a {@link MappedTable} instance representing the mapped table and its dependencies
     * @throws NullPointerException     if any of the required arguments are null
     * @throws IllegalArgumentException if the DTO class is invalid or the table specification is incomplete
     * @throws IllegalStateException    if the table metadata cannot be read due to a database access issue
     */
    public MappedTable mapToTable(final MethodHandles.Lookup lookup, final Class<?> dtoClass, final TableSpec tableSpec, final Set<Class<?>> allDtoClasses) {
        // Up-front validation
        Objects.requireNonNull(lookup, "MethodHandles lookup is required for reflection");
        Objects.requireNonNull(dtoClass, "DTO class cannot be null");

        // HiddenJoinEntity is used for intermediate join tables and is a JDK proxy - don't check module accessiblity for those
        if (!HiddenJoinEntity.class.isAssignableFrom(dtoClass)) {
            ModuleUtils.requireAccessible(dtoClass, Litebridge.class.getModule());
        }

        if (ClassUtils.isBasicType(dtoClass)) {
            throw new IllegalArgumentException("Not a DTO: " + dtoClass.getName());
        } else if (CollectionUtils.isEmpty(tableSpec.fieldColumnMap())) {
            throw new IllegalArgumentException("No field-column map provided");
        }

        // Read the table metadata
        final TableMetaData tableMetaData = tableMetaDataCache.ensureTableMetaData(tableSpec);

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

        // Check for unmapped expressions
        if (!unmappedColumns.isEmpty()) {
            // Check if any non-nullable expressions are missing
            final List<String> missingColumns = unmappedColumns.stream()
                    .filter(columnName -> !tableMetaData.column(columnName).isNullable())
                    .toList();

            if (!missingColumns.isEmpty()) {
                throw new IllegalArgumentException(String.format("Unmapped non-nullable expressions for table '%s': %s; DTO class: '%s'", tableMetaData.name(), missingColumns, dtoClass.getName()));
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
                throw new IllegalArgumentException(String.format("Column '%s', mapped by %s '%s' of DTO '%s', does not exist in table: '%s'", columnSpec.name(), (fieldSpec.property() ? "property" : "field"), fieldSpec.name(), dtoClass, tableMetaData.qualifiedName()));
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
        //TODO: Verify we are dealing with a collection
        final FieldAccessor leftCollectionFieldAccessor = fieldAccessor(dtoClass, fieldSpec);
        final ColumnMetaData leftColumnMetaData = tableMetaData.column(manyToMany.joinColumn());

        final OrmTable joinOrmTable = ensureManyToManyJoinTable(manyToMany, lookup, manyToOneDependencies);

        final Class<?> rightDto = getJoinTargetDto(dtoClass, leftCollectionFieldAccessor);
        final Supplier<OrmTable> rightOrmTable = () -> tableRegistry.getOrmTableOrThrow(rightDto);

        final MappedManyToMany mappedManyToMany = new MappedManyToMany(
                joinOrmTable,
                manyToMany.joinColumn(),
                leftCollectionFieldAccessor,
                rightOrmTable,
                manyToMany.inverseJoinColumn());
        mappedFields.put(leftCollectionFieldAccessor, mappedManyToMany);
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
        final OrmTable joinTable = tableRegistry.getOrmTable(manyToMany.joinTable());

        if (joinTable != null) {
            return joinTable;
        } else {
            final MappedTable mappedJoinTable = mapManyToManyJoinTable(manyToMany, lookup);
            manyToOneDependencies.addAll(mappedJoinTable.manyToOneDependencies());
            tableRegistry.addTable(mappedJoinTable.ormTable().dtoClass(), mappedJoinTable.ormTable());
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

    /**
     * A database table and its corresponding relationships mapped within the ORM (Object-Relational Mapping) layer.
     * <p>
     * A MappedTable instance encapsulates the metadata of a database table and its many-to-one dependencies.
     * It is used as part of the ORM infrastructure to facilitate operations like CRUD (Create, Read, Update, Delete),
     * while ensuring database relationships and constraints are respected.
     * <p>
     * This class is typically constructed through the table mapping logic provided in the parent
     * TableMapper class, which handles the specific mapping rules, validations, and metadata generation.
     *
     * @param ormTable              The metadata representation of the database table being mapped.
     *                              This includes table-specific information such as the table name,
     *                              columns, and primary keys.
     * @param manyToOneDependencies A list of field accessors that represent many-to-one relationships
     *                              associated with the table. These dependencies provide linkage
     *                              between the current table and other tables it references.
     *                              These relationships are typically mapped using foreign keys.
     */
    public record MappedTable(OrmTable ormTable, List<FieldAccessor> manyToOneDependencies) {
    }
}
