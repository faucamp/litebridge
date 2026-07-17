package org.litebridge.orm.persistence;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.MapUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.dto.DtoJoinSpec;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The SelectSpecDtoMapper class is responsible for mapping data from rows of a database query to DTO (Data Transfer Object) instances.
 * It facilitates the conversion of raw query results into structured DTO objects by using specifications, type conversions,
 * table metadata, and a context for orchestration.
 * <p>
 * This class provides a flexible mechanism to handle complex mapping scenarios, including resolving dependencies between DTOs,
 * handling related data through one-to-one, one-to-many, and many-to-many relationships, and processing data as per the blueprint
 * defined for DTO creation.
 * <p>
 * This class supports custom mapping logic, lazy-loading of dependencies, and handling of various relationship types
 * among DTOs.
 */
public class SelectSpecDtoMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSpecDtoMapper.class);
    private final PartiallyConstructedDtoCache dtoCache;
    private final TypeConverter typeConverter;
    private final DtoSelectSpec selectSpec;
    private final TableRegistry tableRegistry;
    private final DtoConstructor dtoConstructor;
    private final LitebridgeContext litebridgeContext;

    /**
     * Constructs a new instance of the SelectSpecDtoMapper, initializing the required dependencies
     * for mapping database query results into Data Transfer Objects (DTOs).
     *
     * @param dtoSelectSpec     The DTO selection specification, which provides the blueprint for mapping
     *                          database rows to DTOs.
     * @param typeConverter     The type converter responsible for converting database values into the
     *                          appropriate type used in DTO fields.
     * @param tableRegistry     The registry containing metadata about database tables, used for resolving
     *                          table and column mappings.
     * @param dtoConstructor    The constructor utility used to instantiate DTO instances during the
     *                          mapping process.
     * @param litebridgeContext The operational context that encapsulates system-wide configurations
     *                          required for the mapping and object construction.
     */
    public SelectSpecDtoMapper(final DtoSelectSpec dtoSelectSpec,
                               final TypeConverter typeConverter,
                               final TableRegistry tableRegistry,
                               final DtoConstructor dtoConstructor,
                               final LitebridgeContext litebridgeContext) {
        this.dtoCache = new PartiallyConstructedDtoCache();
        this.typeConverter = typeConverter;
        this.selectSpec = dtoSelectSpec;
        this.tableRegistry = tableRegistry;
        this.dtoConstructor = dtoConstructor;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Converts a list of database rows into a list of DTO (Data Transfer Object) instances of the specified type.
     *
     * @param <DTO>    The generic type of the DTO to which the rows will be mapped.
     * @param dtoClass The target DTO class to which the rows should be converted.
     * @param rows     The list of database rows to be converted into DTOs.
     * @return A list of DTOs of the specified type, created by mapping the given rows.
     */
    public <DTO> List<DTO> toDtos(final Class<DTO> dtoClass, final List<Row> rows) {
        final List<DtoBlueprint> blueprints = createDtoBlueprints(rows);

        // Phase 1: Populate Cache
        for (final DtoBlueprint blueprint : blueprints) {
            toDto(blueprint.dtoData());
            blueprint.joinedDtoData().forEach(this::toDto);
        }

        // Phase 2: Batch Resolve
        final Set<Object> resolvedDtoInstances = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean hasNewResolved;
        do {
            hasNewResolved = false;
            final List<PartiallyConstructedDto> currentCacheSnapshot = dtoCache.stream().toList();
            for (final PartiallyConstructedDto partialDto : currentCacheSnapshot) {
                if (!resolvedDtoInstances.contains(partialDto.dto())) {
                    resolveRelatedDtoDependencies(partialDto, resolvedDtoInstances);
                    hasNewResolved = true;
                }
            }
        } while (hasNewResolved);

        // Phase 3: Extract Results
        return blueprints.stream()
                .map(blueprint -> {
                    final PartiallyConstructedDto partialDto = dtoCache.get(blueprint.dtoData().dtoClass(), blueprint.dtoData().primaryKey());
                    return partialDto != null ? dtoClass.cast(partialDto.dto()) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private @Nullable PartiallyConstructedDto toDto(final DtoBlueprint.SelectDtoData dtoData) {
        return toDto(dtoData, dtoData.fieldColumns());
    }

    private @Nullable PartiallyConstructedDto toDto(final DtoBlueprint.JoinDtoData dtoData) {
        return toDto(dtoData, dtoData.fieldColumns());
    }

    private @Nullable PartiallyConstructedDto toDto(final DtoBlueprint.DtoData<?> dtoData, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        return toDto(dtoData.dtoClass(), dtoData.spec().dtoTable(), dtoData, fieldColumns);
    }

    private Object resolveRelatedDtoDependencies(final PartiallyConstructedDto partialDto, final Set<Object> resolvedDtoInstances) {
        if (resolvedDtoInstances.contains(partialDto.dto())) {
            return partialDto.dto();
        }

        PartiallyConstructedDto currentDto = partialDto;
        Map<FieldAccessor, Object> recordOverrides = null;

        for (final DtoConstructor.DtoDependency dependency : partialDto.dependencies()) {
            final PartiallyConstructedDto relatedDto = dtoCache.get(dependency.targetDtoClass(), dependency.targetPrimaryKeyValue());

            if (relatedDto != null) {
                final Object resolvedRelatedInstance = resolveRelatedDtoDependencies(relatedDto, resolvedDtoInstances);
                if (currentDto.dto() instanceof Record) {
                    if (recordOverrides == null) {
                        recordOverrides = new HashMap<>();
                    }
                    recordOverrides.put(dependency.field(), resolvedRelatedInstance);
                } else {
                    dependency.field().set(currentDto.dto(), resolvedRelatedInstance);
                }
            } else {
                LOGGER.debug("Unresolved dependency for field '{}' in DTO class '{}' with target key: {}: no matching DTO found", dependency.field().name(), partialDto.dto().getClass(), dependency.targetPrimaryKey());
            }
        }

        if (recordOverrides != null) {
            currentDto = recreateDto(currentDto, recordOverrides);
            // Update cache with the new record instance
            dtoCache.put(currentDto.dtoData().primaryKey(), currentDto);
        }

        updateOneToManyCollectionMappings(currentDto);
        updateManyToManyCollectionMappings(currentDto);
        resolvedDtoInstances.add(currentDto.dto());
        return currentDto.dto();
    }

    private @Nullable PartiallyConstructedDto toDto(final Class<?> dtoClass, final OrmTable ormTable, final DtoBlueprint.DtoData<?> dtoData, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        if (CollectionUtils.isEmpty(fieldColumns)) {
            return null;
        }

        // Ensure we target the correct DTO class (in case "allowed interfaces" is used)
        final Class<?> targetDtoClass;

        if (dtoClass != ormTable.dtoClass() && ormTable.getDtoClassInterfaces().contains(dtoClass)) {
            targetDtoClass = ormTable.dtoClass();
        } else {
            targetDtoClass = dtoClass;
        }

        final PartiallyConstructedDto cachedDto = dtoData.primaryKey().isEmpty() ? null : dtoCache.get(targetDtoClass, dtoData.primaryKey());

        if (cachedDto != null) {
            return cachedDto;
        }

        final PartiallyConstructedDto partialDto = createDto(targetDtoClass, ormTable, dtoData, fieldColumns);
        dtoCache.put(dtoData.primaryKey(), partialDto);
        return partialDto;
    }

    private PartiallyConstructedDto createDto(final Class<?> dtoClass, final OrmTable table, final DtoBlueprint.DtoData<?> dtoData, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        return createDto(dtoClass, table, dtoData, fieldColumns, null);
    }

    private PartiallyConstructedDto recreateDto(final PartiallyConstructedDto partialDto, final Map<FieldAccessor, Object> valueOverrides) {
        return createDto(partialDto.dto().getClass(), partialDto.ormTable(), partialDto.dtoData(), partialDto.fieldColumns(), valueOverrides);
    }

    @SuppressWarnings("unchecked")
    private PartiallyConstructedDto createDto(final Class<?> dtoClass, final OrmTable table, final DtoBlueprint.DtoData<?> dtoData, final List<DtoSelectSpec.FieldColumn> fieldColumns, @Nullable final Map<FieldAccessor, Object> valueOverrides) {
        final Row row = dtoData.row();
        final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues = new ArrayList<>(row.size());
        final List<DtoConstructor.DtoDependency> dependencies = new ArrayList<>();

        fieldColumns.forEach(fieldColumn -> {
            final FieldAccessor field;
            final boolean sameTableNestedDto;

            if (fieldColumn.fieldAccessor() instanceof FieldAccessorChain fieldAccessorChain) {
                // If the current DTO does not matches the accessor chain target, use it directly, else build up the DTO chain
                if (fieldAccessorChain.dtoClass() != dtoClass) {
                    // Check if the current DTO class matches a step in the chain
                    field = fieldAccessorChain.fieldAccessors().stream()
                            .filter(fieldAccessor -> fieldAccessor.dtoClass() == dtoClass)
                            .findFirst()
                            .orElse(null);
                    sameTableNestedDto = field != null;
                } else {
                    field = fieldAccessorChain.fieldAccessors().getLast();
                    sameTableNestedDto = false;
                }
            } else {
                field = fieldColumn.fieldAccessor();
                sameTableNestedDto = false;
            }

            if (field == null || !field.dtoClass().isAssignableFrom(dtoClass)) {
                return;
            }

            if (MapUtils.containsKey(field, valueOverrides)) {
                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(field, valueOverrides.get(field)));
            } else if (sameTableNestedDto) {
                // Nested DTO built up from the same table
                final Object nestedDto = ObjectUtils.requireNonNull(toDto(field.type(), table, dtoData, fieldColumns), () -> new IllegalStateException("Failed to construct nested DTO")).dto();
                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(field, nestedDto));
            } else if (ClassUtils.isBasicType(field.type())) {
                // Standard column: find the value, convert it to target DTO's field type, and set the field
                final Row.RowColumn rowColumn = row.column(fieldColumn.column())
                        .orElseThrow(() -> new IllegalStateException("No column found for alias '%s' in row: %s".formatted(fieldColumn.column().alias(), row)));
                final Object convertedValue = typeConverter.convert(rowColumn.value(), field.type());
                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(field, convertedValue));
            } else {
                // Related DTO: note dependency and allow outer process populate these
                final DtoConstructor.DtoDependency dependency = createDtoDependency(table, fieldColumn, row, dtoClass);
                dependencies.add(dependency);
                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(field, dependency));
            }
        });

        final Object dto = switch (litebridgeContext.config().getRelatedDtoStrategy()) {
            case PARTIAL_OBJECT_IF_NO_JOIN -> constructDtoResolveDeps(dtoClass, fieldAccessorValues);
            case NULL_IF_NO_JOIN -> constructDto(dtoClass, fieldAccessorValues);
        };

        return new PartiallyConstructedDto(dto, table, dtoData, fieldColumns, dependencies);
    }

    private DtoConstructor.@NonNull DtoDependency createDtoDependency(final OrmTable ormTable, final DtoSelectSpec.FieldColumn fieldColumn, final Row row, final Class<?> parentDtoClass) {
        final FieldAccessor field = fieldColumn.fieldAccessor();
        final Row.RowColumn rowColumn = row.column(fieldColumn.column())
                .orElseThrow(() -> new IllegalStateException("No column found for alias '%s' in row: %s".formatted(fieldColumn.column().alias(), row)));
        final Object targetPkValue = rowColumn.value();
        // Get the table and column for the related DTO
        final ColumnMetaData columnMetaData = ormTable.getColumnForFieldName(field.name());

        if (columnMetaData.getJoinColumn() == null) {
            throw new IllegalStateException("No join column found for column '%s' in table '%s'".formatted(columnMetaData.name(), ormTable.getMetaData().name()));
        }

        final OrmTable relatedOrmTable = tableRegistry.getTableInContext(field.type(), parentDtoClass)
                .orElseGet(() -> tableRegistry.getTableOrThrow(field.type()));
        final FieldAccessor pkFieldAccessor = relatedOrmTable.getFieldForColumnName(columnMetaData.getJoinColumn());
        final List<DtoConstructor.FieldAccessorValue> relatedPkFieldAccessorValues = new ArrayList<>();
        final Object convertedTargetPkValue = typeConverter.convert(targetPkValue, pkFieldAccessor.type());
        relatedPkFieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(pkFieldAccessor, convertedTargetPkValue));

        final DtoConstructor.DtoDependency dependency = new DtoConstructor.DtoDependency(field, field.type(), relatedPkFieldAccessorValues);
        return dependency;
    }

    private <DTO> DTO constructDtoResolveDeps(final Class<DTO> dtoClass, final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues) {
        final List<DtoConstructor.FieldAccessorValue> processedFieldAccessorValues = new ArrayList<>();

        for (DtoConstructor.FieldAccessorValue fieldAccessorValue : fieldAccessorValues) {
            if (fieldAccessorValue.value() instanceof DtoConstructor.DtoDependency dependency) {
                final PartiallyConstructedDto cachedDto = dtoCache.get(dependency.targetDtoClass(), dependency.targetPrimaryKeyValue());

                if (cachedDto != null) {
                    fieldAccessorValue = new DtoConstructor.FieldAccessorValue(fieldAccessorValue.field(), cachedDto.dto());
                } else {
                    // Construct partial DTO if not in cache
                    final Object relatedDtoPkOnly = constructDtoResolveDeps(dependency.targetDtoClass(), dependency.targetPrimaryKey());
                    fieldAccessorValue = new DtoConstructor.FieldAccessorValue(fieldAccessorValue.field(), relatedDtoPkOnly);
                }
            }

            processedFieldAccessorValues.add(fieldAccessorValue);
        }

        return constructDto(dtoClass, processedFieldAccessorValues);
    }

    @SuppressWarnings("unchecked")
    <DTO> DTO constructDto(final Class<DTO> dtoClass, final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues) {
        return constructDto(dtoClass, fieldAccessorValues, dtoConstructor);
    }

    static <DTO> DTO constructDto(final Class<DTO> dtoClass, final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues, final DtoConstructor dtoConstructor) {
        final DtoConstructor.ConstructionResult<DTO> constructionResult = dtoConstructor.newInstance(dtoClass, fieldAccessorValues);
        final DTO dto = constructionResult.dto();

        if (constructionResult.defaultConstructorUsed()) {
            // Set the fields via field accessors since the default constructor was used
            fieldAccessorValues.forEach(fieldAccessorValue -> {
                final FieldAccessor fieldAccessor = fieldAccessorValue.field();
                final Object rawValue = fieldAccessorValue.value();
                final Object value;

                if (rawValue == null) {
                    value = ClassUtils.getDefaultValue(fieldAccessor.type());
                } else if (fieldAccessorValue.value() instanceof DtoConstructor.DtoDependency dependency) {
                    value = null;
                } else {
                    value = fieldAccessorValue.value();
                }

                fieldAccessorValue.field().set(dto, value);
            });
        }

        return dto;
    }

    private List<DtoBlueprint> createDtoBlueprints(final List<Row> rows) {
        final OrmTable ormTable = selectSpec.dtoTable();

        // Find the primary key(s) for this table
        final List<ColumnMetaData> pkColumns = ormTable.getMetaData().primaryKey();
        // Match that to aliased expressions (if any) in the select spec
        final List<DtoSelectSpec.FieldColumn> pkFieldColumns = extractPrimaryKeyFieldColumns(pkColumns, selectSpec.getTable(), selectSpec.getExpressions());

        // Group rows by the DTO table's primary key value for DTO assembly
        final Map<List<Object>, List<Row>> dtoPkGroupedRows = new LinkedHashMap<>();

        for (final Row row : rows) {
            final List<Object> pkValues = pkFieldColumns.stream()
                    .map(pkFieldColumn -> row.column(pkFieldColumn.column())
                            .map(rowColumn -> {
                                final ColumnMetaData columnMetaData = ormTable.getColumnMetaData(rowColumn.column().name());
                                final Class<?> pkFieldType = pkFieldColumn.fieldAccessor().type();

                                if (columnMetaData.getJoinColumn() != null
                                        && !ClassUtils.isBasicType(pkFieldType)) {
                                    // Primary foreign key mapped to a DTO field; get the related DTO's primary key/join column field
                                    final OrmTable relatedOrmTable = tableRegistry.getTableInContext(pkFieldType, ormTable.dtoClass())
                                            .orElseGet(() -> tableRegistry.getTableOrThrow(pkFieldType));
                                    final FieldAccessor relatedPkFieldAccessor = relatedOrmTable.getFieldForColumnName(columnMetaData.getJoinColumn());
                                    return (Object) typeConverter.convert(rowColumn.value(), relatedPkFieldAccessor.type());
                                }

                                return (Object) typeConverter.convert(rowColumn.value(), pkFieldColumn.fieldAccessor().type());
                            })
                            // No PK present in selected expressions; use a hash of the row as the identifier
                            .orElseGet(row::hashCode))
                    .toList();

            dtoPkGroupedRows.computeIfAbsent(pkValues, k -> new ArrayList<>())
                    .add(row);
        }

        // Construct the DTO blueprint by grouping other DTO data contained within each primary key-grouped list of rows
        final List<DtoBlueprint> blueprints = new ArrayList<>(dtoPkGroupedRows.size());
        final boolean hasJoins = !CollectionUtils.isEmpty(selectSpec.getJoins());

        for (final Map.Entry<List<Object>, List<Row>> entry : dtoPkGroupedRows.entrySet()) {
            final List<Row> rowGroup = entry.getValue();
            final DtoBlueprint blueprint = new DtoBlueprint(selectSpec, entry.getKey(), rowGroup.getFirst());

            if (hasJoins) {
                selectSpec.getJoins().stream()
                        .map(DtoJoinSpec.class::cast)
                        .forEach(dtoJoinSpec -> {
                            final List<ColumnMetaData> joinPkColumns = dtoJoinSpec.dtoTable().getMetaData().primaryKey();
                            final List<DtoSelectSpec.FieldColumn> joinPkFieldColumns = extractPrimaryKeyFieldColumnsFromFieldColumns(joinPkColumns, dtoJoinSpec.table(), dtoJoinSpec.getFieldColumns());
                            final Map<List<@Nullable Object>, Row> relatedDtoRows = new LinkedHashMap<>();

                            for (final Row row : rowGroup) {
                                final List<@Nullable Object> joinPkValues = joinPkFieldColumns.stream()
                                        .filter(joinPkFieldColumn -> joinPkFieldColumn.column().alias() != null)
                                        .map(joinPkFieldColumn -> row.column(joinPkFieldColumn.column())
                                                .map(rowColumn -> {
                                                    final FieldAccessor relatedFieldAccessor = dtoJoinSpec.dtoTable().getFieldForColumnName(rowColumn.column().name());
                                                    return (Object) typeConverter.convert(rowColumn.value(), relatedFieldAccessor.type());
                                                })
                                                .orElseThrow(() -> new IllegalStateException("No primary key column found for join table '%s' in row: %s".formatted(ormTable.getMetaData().name(), row))))
                                        .toList();

                                relatedDtoRows.computeIfAbsent(joinPkValues, k -> row);
                            }

                            relatedDtoRows.forEach(
                                    (pkValues, row) -> blueprint.addJoinedDtoData(dtoJoinSpec, pkValues, row));
                        });
            }

            blueprints.add(blueprint);
        }

        return blueprints;
    }

    @Deprecated
    private List<DtoSelectSpec.FieldColumn> extractPrimaryKeyFieldColumnsFromFieldColumns(final List<ColumnMetaData> pkColumns, final Table table, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        final List<DtoSelectSpec.FieldColumn> pkFieldColumns = new ArrayList<>(pkColumns.size());

        for (ColumnMetaData pkColumnn : pkColumns) {
            fieldColumns.stream()
                    .filter(fieldColumn -> fieldColumn.column().table().equals(table))
                    .filter(fieldColumn -> fieldColumn.column().name().equals(pkColumnn.name()))
                    .findFirst()
                    .ifPresent(pkFieldColumns::add);
        }

        return pkFieldColumns;
    }

    private List<DtoSelectSpec.FieldColumn> extractPrimaryKeyFieldColumns(final List<ColumnMetaData> pkColumns, final Table table, final List<ExpressionSpec> expressionSpecs) {
        final List<DtoSelectSpec.FieldColumn> pkFieldColumns = new ArrayList<>(pkColumns.size());

        for (ColumnMetaData pkColumnn : pkColumns) {
            expressionSpecs.stream()
                    .filter(expression -> expression instanceof SelectFieldSpec)
                    .map(expression -> (SelectFieldSpec) expression)
                    .filter(selectField -> selectField.getColumn().table().equals(table))
                    .filter(selectField -> selectField.getColumn().name().equals(pkColumnn.name()))
                    .map(selectField -> new DtoSelectSpec.FieldColumn(selectField.field(), selectField.getColumn()))
                    .findFirst()
                    .ifPresent(pkFieldColumns::add);
        }

        return pkFieldColumns;
    }

    @SuppressWarnings("unchecked")
    private void updateOneToManyCollectionMappings(final PartiallyConstructedDto partialDto) {
        final Object dto = partialDto.dto();
        final OrmTable table = partialDto.ormTable();

        final List<MappedOneToMany> mappedOneToManyList = table.getOneToManyMappings();

        if (CollectionUtils.isEmpty(mappedOneToManyList)) {
            // No one-to-many collection mappings to update
            return;
        }

        mappedOneToManyList.forEach(mappedOneToMany -> {
            LOGGER.trace("Updating one-to-many mapping for field '{}' of DTO: {}", mappedOneToMany.collection().name(), dto);
            // Get the current value of the mapping
            final FieldAccessor collection = mappedOneToMany.collection();
            final Collection<Object> currentCollection;
            final Collection<Object> dtoCollection = (Collection<Object>) collection.get(dto);

            if (dtoCollection != null) {
                currentCollection = dtoCollection;
            } else {
                currentCollection = (Collection<Object>) ClassUtils.newInstance(collection.type());
                collection.set(dto, currentCollection);
            }

            final Class<?> targetClass = collection.genericType();
            currentCollection.addAll(dtoCache.getDtosByClass(targetClass));
        });
    }

    @SuppressWarnings("unchecked")
    private void updateManyToManyCollectionMappings(final PartiallyConstructedDto partialDto) {
        final Object dto = partialDto.dto();
        final OrmTable table = partialDto.ormTable();

        final List<MappedManyToMany> mappedManyToManyList = table.getManyToManyMappings();

        if (CollectionUtils.isEmpty(mappedManyToManyList)) {
            // No one-to-many collection mappings to update
            return;
        }

        mappedManyToManyList.forEach(mappedOneToMany -> {
            LOGGER.trace("Updating many-to-many mapping for field '{}' of DTO: {}", mappedOneToMany.collection().name(), dto);
            // Get the current value of the mapping
            final FieldAccessor collection = mappedOneToMany.collection();
            final Collection<Object> currentCollection;
            final Collection<Object> dtoCollection = (Collection<Object>) collection.get(dto);

            if (dtoCollection != null) {
                currentCollection = dtoCollection;
            } else {
                currentCollection = (Collection<Object>) ClassUtils.newInstance(collection.type());
                collection.set(dto, currentCollection);
            }

            final Class<?> targetClass = collection.genericType();
            currentCollection.addAll(dtoCache.getDtosByClass(targetClass));
        });
    }

    private record PartiallyConstructedDto(Object dto,
                                           OrmTable ormTable,
                                           DtoBlueprint.DtoData<?> dtoData,
                                           List<DtoSelectSpec.FieldColumn> fieldColumns,
                                           List<DtoConstructor.DtoDependency> dependencies) {
    }

    private static final class PartiallyConstructedDtoCache {
        private final Map<Class<?>, Map<List<Object>, SelectSpecDtoMapper.PartiallyConstructedDto>> cache = new IdentityHashMap<>();
        private final Map<Class<?>, List<Object>> listCache = new IdentityHashMap<>();

        public @Nullable PartiallyConstructedDto get(final Class<?> dtoClass, final List<Object> id) {
            return cache.computeIfAbsent(dtoClass, cls -> new HashMap<>())
                    .get(id);
        }

        public void put(final List<Object> id, final SelectSpecDtoMapper.PartiallyConstructedDto dto) {
            cache.computeIfAbsent(dto.dto().getClass(), cls -> new HashMap<>())
                    .put(id, dto);
            listCache.clear();
        }

        public Stream<PartiallyConstructedDto> stream() {
            return cache.values().stream()
                    .flatMap(pkPcDto -> pkPcDto.values().stream());
        }

        public List<Object> getDtosByClass(final Class<?> dtoClass) {
            return listCache.computeIfAbsent(dtoClass, cls -> stream(cls).map(Object.class::cast).toList());
        }

        public <DTO> Stream<DTO> stream(final Class<DTO> dtoClass) {
            Map<List<Object>, SelectSpecDtoMapper.PartiallyConstructedDto> dtoCache = cache.get(dtoClass);

            if (dtoCache != null) {
                return dtoCache.values().stream()
                        .map(PartiallyConstructedDto::dto)
                        .map(dtoClass::cast);
            } else {
                // Look for interfaces
                return cache.entrySet().stream()
                        .filter(entry -> dtoClass.isAssignableFrom(entry.getKey()))
                        .flatMap(entry -> entry.getValue().values().stream())
                        .map(PartiallyConstructedDto::dto)
                        .map(dtoClass::cast);
            }
        }
    }
}