package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.RowColumn;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.tracking.FieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps data from rows of a database query to DTO (Data Transfer Object) instances.
 * <p>
 * It uses a "compiled" mapping plan to achieve high performance by pre-resolving column indices and constructors.
 * <p>
 * This implementation is designed to handle generic SQL query results to a limited extent.
 * (results from queries that did not necessarily originate from the ORM).
 */
public class DtoMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtoMapper.class);
    private static final Pattern FUNCTION_SQL_COLUMN_PATTERN = Pattern.compile(
            "\\b[a-zA-Z_]\\w*\\s*\\((?:\\s*\\b[a-zA-Z_]\\w*\\s*\\()*+\\s*(?:([a-zA-Z_]\\w*)\\.)?([a-zA-Z_]\\w*)",
            Pattern.CASE_INSENSITIVE
    );

    private final TypeConverter typeConverter;
    private final TableRegistry tableRegistry;
    private final DtoConstructor dtoConstructor;
    private final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code DtoMapper} instance.
     *
     * @param dtoConstructor    the DTO constructor helper
     * @param litebridgeContext the Litebridge context
     */
    public DtoMapper(final DtoConstructor dtoConstructor,
                     final LitebridgeContext litebridgeContext) {
        this.typeConverter = litebridgeContext.typeConverter();
        this.tableRegistry = litebridgeContext.tableRegistry();
        this.dtoConstructor = dtoConstructor;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Maps database rows to a list of DTO instances of the specified type.
     *
     * @param <DTO>           the target DTO type
     * @param dtoClass        the class of the DTO to map to
     * @param contextDtoClass the context DTO class, or {@code null}
     * @param rows            the database rows to map
     * @return the list of mapped DTO instances
     */
    public <DTO> List<DTO> toDtos(final Class<DTO> dtoClass, final @Nullable Class<?> contextDtoClass, final List<Row> rows) {
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        final Row firstRow = rows.getFirst();
        final int rowHash = firstRow.structureHashCode();
        final MappingPlanKey key = new MappingPlanKey(dtoClass, contextDtoClass, rowHash);
        final MappingPlanCache mappingPlanCache = litebridgeContext.mappingPlanCache();

        MappingPlan compilationResult = mappingPlanCache != null ? mappingPlanCache.get(key) : null;

        if (compilationResult == null) {
            compilationResult = compileMappingPlan(dtoClass, contextDtoClass, firstRow);

            if (mappingPlanCache != null) {
                mappingPlanCache.put(key, compilationResult);
            }
        }

        if (compilationResult.rootMappingData() == null) {
            return Collections.emptyList();
        }

        // Create DTOs and populate the cache
        final DtoCache dtoCache = new DtoCache();
        final Map<MappingData, List<PartiallyConstructedDto>> createdDtosByMapping = cacheDtos(rows, compilationResult.mappingDataMap(), dtoCache);

        // Resolve inter-DTO dependencies
        resolveDependencies(createdDtosByMapping, dtoCache);

        // Return all unique DTOs assignable to the requested type from the root mapping
        final List<DTO> result = new ArrayList<>();
        final Set<Object> seenDtos = Collections.newSetFromMap(new IdentityHashMap<>());
        final MappingData rootMappingData = compilationResult.rootMappingData();

        for (final Row row : rows) {
            final Pk pk = getPrimaryKey(rootMappingData, row);
            final PartiallyConstructedDto pd = dtoCache.get(rootMappingData, pk);
            if (pd != null && pd.getDto() != null) {
                final Object dto = pd.getDto();
                if (seenDtos.add(dto)) {
                    //noinspection unchecked
                    result.add((DTO) dto);
                }
            }
        }

        return result;
    }

    private MappingPlan compileMappingPlan(final Class<?> dtoClass, final @Nullable Class<?> contextDtoClass, final Row firstRow) {
        final TableMetaData dtoClassTableMetaData;
        final OrmTable rootOrmTable;

        if (contextDtoClass != null) {
            rootOrmTable = tableRegistry.getOrmTableInContextOrThrow(dtoClass, contextDtoClass);
        } else {
            rootOrmTable = tableRegistry.getOrmTableOrThrow(dtoClass);
        }

        dtoClassTableMetaData = rootOrmTable.getMetaData();

        final Map<String, MappingData> mappingDataMap = new LinkedHashMap<>();
        MappingData rootMappingData = null;
        int columnIndex = 0;

        for (final RowColumn rowColumn : firstRow.columns()) {
            final Column rawColumn = rowColumn.column();
            final Column column;

            if (rawColumn != null) {
                if (rawColumn.hasTable()) {
                    column = rawColumn;
                } else {
                    column = parseTargetColumn(rawColumn.name(), dtoClassTableMetaData.schema(), dtoClassTableMetaData);
                }
            } else {
                column = parseTargetColumn(rowColumn.label(), dtoClassTableMetaData.schema(), dtoClassTableMetaData);
            }

            final Table table = column.table();
            final MappingData mappingData = createMappingDataIfAbsent(mappingDataMap, table, contextDtoClass, rowColumn);

            if (rootMappingData == null && mappingData.ormTable().equals(rootOrmTable)) {
                rootMappingData = mappingData;
            }

            final FieldAccessor fieldAccessor = mappingData.ormTable().getFieldForColumnName(column.name());

            // Check if we already have a mapping for this field
            FieldMapping fieldMapping = null;
            final List<FieldMapping> currentMappings = mappingData.fieldMappings();

            for (final FieldMapping mapping : currentMappings) {
                if (mapping.fieldAccessor().equals(fieldAccessor)) {
                    fieldMapping = mapping;
                    break;
                }
            }

            if (fieldMapping != null) {
                fieldMapping.columns().add(column);
                fieldMapping.columnLabels().add(rowColumn.label());
            } else {
                final boolean basicType = ClassUtils.isBasicType(fieldAccessor.type());
                final boolean relatedDto = !basicType;
                FieldAccessor relatedCollectionField = null;
                Class<?> relatedDtoClass = null;

                // Check for reverse collection mappings
                final List<FieldAccessor> reverseMappings = mappingData.ormTable().getOneToManyReverseMappings();

                if (reverseMappings != null) {
                    for (FieldAccessor collectionField : reverseMappings) {
                        final OrmTable hostOrmTable = tableRegistry.getOrmTable(collectionField.dtoClass());

                        if (hostOrmTable != null) {
                            final MappedFieldTarget target = hostOrmTable.mappedFieldTargetForField(collectionField);

                            if (target instanceof MappedOneToMany(FieldAccessor mappedByField, FieldAccessor collection)
                                    && mappedByField.equals(fieldAccessor)) {
                                relatedCollectionField = collection;
                                relatedDtoClass = collectionField.dtoClass();
                                break;
                            }
                        }
                    }
                }

                if (relatedDto && relatedDtoClass == null) {
                    final MappedFieldTarget fieldAccessorTarget = mappingData.ormTable().mappedFieldTargetForField(fieldAccessor);
                    final OrmTable reverseMappingOrmTable;

                    if (fieldAccessorTarget instanceof ColumnAndInlineTable cit) {
                        reverseMappingOrmTable = cit.tableSpec();
                    } else {
                        reverseMappingOrmTable = tableRegistry.getOrmTable(fieldAccessor.type());
                    }

                    if (reverseMappingOrmTable != null) {
                        relatedDtoClass = reverseMappingOrmTable.dtoClass();
                    }
                }

                fieldMapping = new FieldMapping(fieldAccessor,
                        new ArrayList<>(List.of(column)),
                        new ArrayList<>(List.of(rowColumn.label())),
                        basicType,
                        relatedDto,
                        relatedCollectionField, relatedDtoClass);
                mappingData.fieldMappings().add(fieldMapping);
            }

            // Mark index if its a primary key, for faster retrieval later
            int pkIndex = mappingData.ormTable().getPrimaryKeyFields().indexOf(fieldAccessor);

            if (pkIndex != -1) {
                mappingData.pkColumnIndexes()[pkIndex] = columnIndex;
            }

            columnIndex++;
        }

        // Finalise mapping data
        // Resolve all column indices
        for (final MappingData mappingData : mappingDataMap.values()) {
            final List<FieldMapping> fieldMappings = mappingData.fieldMappings();

            for (final FieldMapping fieldMapping : fieldMappings) {
                if (fieldMapping.isRelatedDto() && fieldMapping.columns().size() > 1) {
                    sortColumnsForRelatedDto(fieldMapping, mappingData.ormTable());
                }

                final int[] columnIndexes = new int[fieldMapping.columns().size()];

                for (int i = 0; i < columnIndexes.length; i++) {
                    columnIndexes[i] = firstRow.indexOf(fieldMapping.columnLabels().get(i));
                }

                fieldMapping.setColumnIndexes(columnIndexes);
            }

            // Check for implicit related DTOs
            final OrmTable ormTable = mappingData.ormTable();

            for (final ColumnMetaData mappedColumn : ormTable.mappedColumns()) {
                final List<ForeignKeyConstraint> foreignKeyConstraints = mappedColumn.getForeignKeyConstraints();

                if (foreignKeyConstraints.isEmpty()) {
                    continue;
                }

                for (final ForeignKeyConstraint foreignKeyConstraint : foreignKeyConstraints) {
                    final Column fkColumn = foreignKeyConstraint.foreignKey();
                    FieldMapping targetFieldMapping = null;

                    for (final MappingData targetMappingData : mappingDataMap.values()) {
                        if (targetMappingData != mappingData && targetMappingData.table().equals(fkColumn.table())) {
                            for (final FieldMapping mapping : targetMappingData.fieldMappings()) {
                                if (mapping.columns().contains(fkColumn)) {
                                    targetFieldMapping = mapping;
                                    break;
                                }
                            }
                            if (targetFieldMapping != null) {
                                break;
                            }
                        }
                    }

                    if (targetFieldMapping == null) {
                        continue;
                    }

                    final FieldAccessor fieldAccessor = ormTable.getFieldForColumnName(mappedColumn.name());
                    final GenericDtoDependency genericDtoDependency = new GenericDtoDependency(fieldAccessor, targetFieldMapping);
                    mappingData.addGenericDtoDependency(genericDtoDependency);
                }
            }
        }

        return new MappingPlan(mappingDataMap, rootMappingData);
    }

    private void sortColumnsForRelatedDto(final FieldMapping fieldMapping, final OrmTable ormTable) {
        final Class<?> targetDtoClass = fieldMapping.fieldAccessor().type();
        final MappedFieldTarget target = ormTable.mappedFieldTargetForFieldOrNull(fieldMapping.fieldAccessor());
        final OrmTable targetOrmTable;

        if (target instanceof ColumnAndInlineTable cit) {
            targetOrmTable = cit.tableSpec();
        } else if (target instanceof MappedCompositeKey mck) {
            targetOrmTable = mck.targetOrmTable().get();
        } else {
            targetOrmTable = tableRegistry.getOrmTable(targetDtoClass);
        }

        if (targetOrmTable == null) {
            return;
        }

        final List<FieldAccessor> targetPkFields = targetOrmTable.getPrimaryKeyFields();

        if (targetPkFields.size() != fieldMapping.columns().size()) {
            LOGGER.warn("Number of columns ({}) for field {} does not match target PK size ({}) for DTO {}",
                    fieldMapping.columns().size(), fieldMapping.fieldAccessor().name(), targetPkFields.size(), targetDtoClass.getName());
            return;
        }

        final Column[] sortedColumns = new Column[targetPkFields.size()];
        final String[] sortedColumnLabels = new String[targetPkFields.size()];

        for (int i = 0; i < fieldMapping.columns().size(); i++) {
            final Column column = fieldMapping.columns().get(i);
            final String label = fieldMapping.columnLabels().get(i);
            final ColumnMetaData columnMetaData = ormTable.getColumnMetaData(column.name());
            final Column targetCol;

            if (columnMetaData.getJoinColumn() != null && columnMetaData.getJoinColumn().table().equals(targetOrmTable.getMetaData().table())) {
                targetCol = columnMetaData.getJoinColumn().column();
            } else {
                ForeignKeyConstraint constraint = null;

                for (final ForeignKeyConstraint fk : columnMetaData.getForeignKeyConstraints()) {
                    if (fk.foreignKey().table().equals(targetOrmTable.getMetaData().table())) {
                        constraint = fk;
                        break;
                    }
                }

                targetCol = constraint != null ? constraint.foreignKey() : null;
            }

            if (targetCol != null) {
                final FieldAccessor targetPkField = targetOrmTable.getFieldForColumnName(targetCol.name());
                final int pkIndex = targetPkFields.indexOf(targetPkField);
                if (pkIndex != -1) {
                    sortedColumns[pkIndex] = column;
                    sortedColumnLabels[pkIndex] = label;
                }
            }
        }

        fieldMapping.columns().clear();
        fieldMapping.columns().addAll(Arrays.asList(sortedColumns));
        fieldMapping.columnLabels().clear();
        fieldMapping.columnLabels().addAll(Arrays.asList(sortedColumnLabels));
    }

    private MappingData createMappingDataIfAbsent(final Map<String, MappingData> mappingDataMap, final Table table, final @Nullable Class<?> contextDtoClass, final RowColumn rowColumn) {
        final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
        final Table canonicalTable = ormTable.getMetaData().table();
        final String key = rowColumn.tableAlias() != null ? rowColumn.tableAlias() : canonicalTable.qualifiedName();
        return mappingDataMap.computeIfAbsent(key, alias -> {
            final List<FieldAccessor> pkFields = ormTable.getPrimaryKeyFields();
            final Class<?> effectiveContextDtoClass = ormTable.contextDtoClass() != null ? ormTable.contextDtoClass() : contextDtoClass;
            return new MappingData(ormTable.dtoClass(),
                    effectiveContextDtoClass,
                    canonicalTable,
                    ormTable,
                    new int[pkFields.size()],
                    new ArrayList<>());
        });
    }

    private static Pk getPrimaryKey(final MappingData mappingData, final Row row) {
        final int[] pkColumnIndexes = mappingData.pkColumnIndexes();

        if (pkColumnIndexes.length == 0) {
            return EmptyPk.INSTANCE;
        } else if (pkColumnIndexes.length == 1) {
            return new SinglePk(row.value(pkColumnIndexes[0]));
        } else {
            final Object[] values = new Object[pkColumnIndexes.length];

            for (int i = 0; i < pkColumnIndexes.length; i++) {
                values[i] = Objects.requireNonNull(row.value(pkColumnIndexes[i]));
            }

            return new CompositePk(values);
        }
    }

    private Map<MappingData, List<PartiallyConstructedDto>> cacheDtos(final List<Row> rows, final Map<String, MappingData> mappingDataMap, final DtoCache dtoCache) {
        final Map<MappingData, List<PartiallyConstructedDto>> createdDtosByMapping = new HashMap<>();

        for (final MappingData mappingData : mappingDataMap.values()) {
            for (final Row row : rows) {
                final Pk primaryKey = getPrimaryKey(mappingData, row);
                PartiallyConstructedDto partialDto = dtoCache.get(mappingData, primaryKey);

                if (partialDto == null) {
                    partialDto = createPartiallyConstructedDto(mappingData, primaryKey, row, mappingDataMap);
                    dtoCache.put(mappingData, primaryKey, partialDto);
                    createdDtosByMapping.computeIfAbsent(mappingData, k -> new ArrayList<>()).add(partialDto);
                    LOGGER.trace("Cached DTO {} with PK {} in mapping {}", mappingData.dtoClass().getSimpleName(), primaryKey, mappingData.table());
                }
            }
        }

        return createdDtosByMapping;
    }

    private PartiallyConstructedDto createPartiallyConstructedDto(final MappingData mappingData, final Pk primaryKey, final Row row, final Map<String, MappingData> mappingDataMap) {
        final DtoData dtoData = new DtoData();
        final List<FieldMapping> fieldMappings = mappingData.fieldMappings();
        final List<SpecificDtoDependency> relatedDtoDependencies = new ArrayList<>();

        // Map DTO field values
        for (final FieldMapping fieldMapping : fieldMappings) {
            final FieldAccessor fieldAccessor = fieldMapping.fieldAccessor();

            if (fieldMapping.isBasicType()) {
                // Basic field
                final Object dbValue = row.value(fieldMapping.columnIndexes()[0]);
                final Object convertedValue = typeConverter.convert(dbValue, fieldAccessor.type());
                dtoData.set(fieldAccessor, convertedValue);

                if (fieldMapping.relatedCollectionField() != null) {
                    final Pk pk = new SinglePk(dbValue);
                    relatedDtoDependencies.add(new SpecificDtoDependency(fieldAccessor, Objects.requireNonNull(fieldMapping.relatedDtoClass()), null, pk, fieldMapping.relatedCollectionField(), true));
                }
            } else {
                // Related DTO
                final Pk pk;
                if (fieldMapping.columnIndexes().length == 1) {
                    pk = new SinglePk(row.value(fieldMapping.columnIndexes()[0]));
                } else {
                    final Object[] pkValues = new Object[fieldMapping.columnIndexes().length];

                    for (int j = 0; j < pkValues.length; j++) {
                        pkValues[j] = Objects.requireNonNull(row.value(fieldMapping.columnIndexes()[j]));
                    }

                    pk = new CompositePk(pkValues);
                }

                final MappingData targetMappingData = findTargetMappingData(mappingData, fieldMapping, mappingDataMap);
                relatedDtoDependencies.add(new SpecificDtoDependency(fieldAccessor, fieldAccessor.type(), targetMappingData, pk, fieldMapping.relatedCollectionField(), false));
            }
        }

        return new PartiallyConstructedDto(dtoData, primaryKey, relatedDtoDependencies, mappingData);
    }

    private @Nullable MappingData findTargetMappingData(final MappingData sourceMappingData, final FieldMapping fieldMapping, final Map<String, MappingData> mappingDataMap) {
        final MappedFieldTarget target = sourceMappingData.ormTable().mappedFieldTargetForFieldOrNull(fieldMapping.fieldAccessor());

        if (target instanceof ColumnAndInlineTable cit) {
            final TableMetaData targetMeta = cit.tableSpec().getMetaData();

            for (final MappingData md : mappingDataMap.values()) {
                if (md != sourceMappingData
                        && md.dtoClass().equals(fieldMapping.fieldAccessor().type())
                        && Objects.equals(md.ormTable().getMetaData().schema(), targetMeta.schema())
                        && md.ormTable().getMetaData().name().equals(targetMeta.name())) {
                    return md;
                }
            }
        }

        return null;
    }

    private void resolveDependencies(final Map<MappingData, List<PartiallyConstructedDto>> createdDtosByMapping, final DtoCache dtoCache) {
        final List<LateReverseCollectionUpdate> lateReverseUpdates = new ArrayList<>();
        final List<PartiallyConstructedDto> allPartialDtos = new ArrayList<>(createdDtosByMapping.values().size());

        for (final List<PartiallyConstructedDto> list : createdDtosByMapping.values()) {
            allPartialDtos.addAll(list);
        }

        // Resolve dependencies and populate DtoData
        for (final PartiallyConstructedDto partialDto : allPartialDtos) {
            final OrmTable ormTable = partialDto.mappingData().ormTable();
            final DtoData dtoData = partialDto.dtoData();
            final List<MappedManyToMany> mappedManyToManyList = ormTable.getManyToManyMappings();

            for (final MappedManyToMany mappedManyToMany : mappedManyToManyList) {
                final OrmTable targetOrmTable = mappedManyToMany.targetOrmTable().get();
                final FieldAccessor collectionFieldAccessor = mappedManyToMany.collection();

                for (final Map.Entry<MappingData, List<PartiallyConstructedDto>> entry : createdDtosByMapping.entrySet()) {
                    if (entry.getKey() != partialDto.mappingData() && entry.getKey().dtoClass().equals(targetOrmTable.dtoClass())) {
                        for (final PartiallyConstructedDto matchingCreatedDto : entry.getValue()) {
                            dtoData.addToCollection(collectionFieldAccessor, matchingCreatedDto);
                        }
                    }
                }
            }

            final List<GenericDtoDependency> genericDtoDependencies = partialDto.mappingData().getGenericDtoDependencies();
            final List<SpecificDtoDependency> specificDtoDependencies = partialDto.dependencies();
            final Set<FieldAccessor> specificDependenciesResolved = new HashSet<>();

            for (final SpecificDtoDependency specificDtoDependency : specificDtoDependencies) {
                final Class<?> relatedDtoClass = specificDtoDependency.relatedDtoClass();
                final PartiallyConstructedDto targetDto = specificDtoDependency.targetMappingData() != null
                        ? dtoCache.get(specificDtoDependency.targetMappingData(), specificDtoDependency.primaryKeyValue())
                        : dtoCache.getByClassAndPk(specificDtoDependency.relatedDtoClass(), specificDtoDependency.primaryKeyValue());
                final Object resolvedDependency;

                if (targetDto == null) {
                    final RelatedDtoStrategy relatedDtoStrategy = litebridgeContext.getRelatedDtoStrategy();

                    if (relatedDtoStrategy == RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN) {
                        resolvedDependency = createDtoPrimaryKeyOnly(relatedDtoClass, null, specificDtoDependency.primaryKeyValue());
                    } else {
                        resolvedDependency = null;
                    }
                } else {
                    resolvedDependency = targetDto;
                }

                if (!specificDtoDependency.reverseUpdateOnly()) {
                    dtoData.set(specificDtoDependency.field(), resolvedDependency);
                }

                if (resolvedDependency != null && specificDtoDependency.relatedCollectionField() != null) {
                    if (resolvedDependency instanceof PartiallyConstructedDto relatedPartialDto) {
                        relatedPartialDto.dtoData().addToCollection(specificDtoDependency.relatedCollectionField(), partialDto);
                    } else {
                        lateReverseUpdates.add(new LateReverseCollectionUpdate(partialDto, resolvedDependency, specificDtoDependency.relatedCollectionField()));
                    }
                }

                specificDependenciesResolved.add(specificDtoDependency.field());
            }

            if (genericDtoDependencies != null) {
                for (final GenericDtoDependency genericDtoDependency : genericDtoDependencies) {
                    if (specificDependenciesResolved.contains(genericDtoDependency.field())) {
                        continue;
                    }

                    final PartiallyConstructedDto targetDto = dtoCache.get(genericDtoDependency.relatedFieldMapping());

                    if (targetDto != null) {
                        dtoData.set(genericDtoDependency.field(), targetDto);
                    }
                }
            }
        }

        final List<DeferredCollectionAddition> deferredAdditions = new ArrayList<>();

        // Instantiate all DTOs
        for (final PartiallyConstructedDto partialDto : allPartialDtos) {
            instantiateDto(partialDto, deferredAdditions);
        }

        // Process deferred collection additions for cyclic dependencies
        for (final DeferredCollectionAddition addition : deferredAdditions) {
            final Object itemDto = addition.itemPartialDto().getDto();
            if (itemDto != null && !addition.collection().contains(itemDto)) {
                addition.collection().add(itemDto);
            }
        }

        // Late reverse updates for already-instantiated DTOs
        for (final LateReverseCollectionUpdate update : lateReverseUpdates) {
            updateReverseCollection(Objects.requireNonNull(update.hostPartialDto.getDto()), update.relatedDto, update.relatedCollectionField);
        }
    }

    @SuppressWarnings("unchecked")
    private Object instantiateDto(final PartiallyConstructedDto partialDto, final List<DeferredCollectionAddition> deferredAdditions) {
        if (partialDto.getDto() != null) {
            return partialDto.getDto();
        }

        if (partialDto.isInstantiating()) {
            return partialDto;
        }

        partialDto.setInstantiating(true);

        try {
            final MappingData mappingData = partialDto.mappingData();
            final Class<?> dtoClass = mappingData.dtoClass();
            final DtoData dtoData = partialDto.dtoData();
            final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(mappingData.ormTable());

            final Map<FieldAccessor, Collection<Object>> instantiatedCollections = new HashMap<>();

            for (final Map.Entry<FieldAccessor, Collection<Object>> entry : dtoData.collections().entrySet()) {
                final FieldAccessor accessor = entry.getKey();
                final Collection<Object> finalCollection = (Collection<Object>) ClassUtils.newInstance(accessor.type());
                instantiatedCollections.put(accessor, finalCollection);
            }

            final Object dto;

            if (constructorMappingInfo.defaultConstructorUsed()) {
                try {
                    dto = constructorMappingInfo.constructor().invoke();
                } catch (Throwable e) {
                    throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
                }

                partialDto.setDto(dto);

                // Populate scalar fields
                for (final Map.Entry<FieldAccessor, @Nullable Object> entry : dtoData.values().entrySet()) {
                    final FieldAccessor accessor = entry.getKey();
                    Object value = entry.getValue();

                    if (value instanceof PartiallyConstructedDto depPartialDto) {
                        value = instantiateDto(depPartialDto, deferredAdditions);
                    }

                    if (value != null || !accessor.type().isPrimitive()) {
                        accessor.set(dto, value);
                    }
                }

                for (final Map.Entry<FieldAccessor, Collection<Object>> entry : instantiatedCollections.entrySet()) {
                    entry.getKey().set(dto, entry.getValue());
                }
            } else {
                final List<FieldAccessor> canonicalAccessors = constructorMappingInfo.canonicalConstructorFieldAccessors();
                final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues = new ArrayList<>(canonicalAccessors.size());

                for (final FieldAccessor canonicalAccessor : canonicalAccessors) {
                    Object value = dtoData.values().get(canonicalAccessor);

                    if (value == null && instantiatedCollections.containsKey(canonicalAccessor)) {
                        value = instantiatedCollections.get(canonicalAccessor);
                    } else if (value instanceof PartiallyConstructedDto depPartialDto) {
                        value = instantiateDto(depPartialDto, deferredAdditions);
                    } else if (value == null && !dtoData.values().containsKey(canonicalAccessor)) {
                        value = ClassUtils.getDefaultValue(canonicalAccessor.type());
                    }

                    fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(canonicalAccessor, value));
                }

                dto = constructDto(dtoClass, fieldAccessorValues, dtoConstructor);
                partialDto.setDto(dto);
            }

            // Populate collection elements after dto instance is created & cached
            for (final Map.Entry<FieldAccessor, Collection<Object>> entry : dtoData.collections().entrySet()) {
                final FieldAccessor accessor = entry.getKey();
                final Collection<Object> partialCollection = entry.getValue();
                final Collection<Object> finalCollection = instantiatedCollections.get(accessor);

                for (final Object item : partialCollection) {
                    if (item instanceof PartiallyConstructedDto itemPartialDto) {
                        if (itemPartialDto.isInstantiating() && itemPartialDto.getDto() == null) {
                            deferredAdditions.add(new DeferredCollectionAddition(finalCollection, itemPartialDto));
                        } else {
                            finalCollection.add(instantiateDto(itemPartialDto, deferredAdditions));
                        }
                    } else {
                        finalCollection.add(item);
                    }
                }
            }

            return dto;
        } finally {
            partialDto.setInstantiating(false);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateReverseCollection(final Object hostDto, final Object relatedDto, final FieldAccessor relatedCollectionField) {
        if (relatedDto instanceof Record) {
            // Records are immutable; we can only populate their collections during construction if they are part of the join.
            return;
        }

        final Collection<Object> currentCollection;
        final Object fieldValue = relatedCollectionField.get(relatedDto);
        final Collection<Object> dtoCollection = (Collection<Object>) fieldValue;

        if (dtoCollection != null) {
            currentCollection = dtoCollection;
        } else {
            currentCollection = (Collection<Object>) ClassUtils.newInstance(relatedCollectionField.type());
            relatedCollectionField.set(relatedDto, currentCollection);
        }

        currentCollection.add(hostDto);
    }

    Object createDtoPrimaryKeyOnly(final Class<?> dtoClass, final @Nullable Class<?> contextDtoClass, final Pk primaryKey) {
        final OrmTable ormTable;
        if (contextDtoClass != null) {
            ormTable = Objects.requireNonNullElseGet(tableRegistry.getOrmTableInContext(dtoClass, contextDtoClass), () -> tableRegistry.getOrmTableOrThrow(dtoClass));
        } else {
            ormTable = tableRegistry.getOrmTableOrThrow(dtoClass);
        }
        final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(ormTable);
        final Object dto;

        if (constructorMappingInfo.defaultConstructorUsed()) {
            try {
                dto = constructorMappingInfo.constructor().invoke();
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
            }

            final List<FieldAccessor> primaryKeyFields = ormTable.getPrimaryKeyFields();

            if (primaryKeyFields.size() != primaryKey.size()) {
                LOGGER.error("Input primary key values {} do not match expect PK size: {}", primaryKey, primaryKeyFields.size());
                throw new IllegalStateException("DTO primary key size mismatch: %s; expected %d values, but got: %d".formatted(dtoClass, primaryKeyFields.size(), primaryKey.size()));
            }

            for (int i = 0; i < primaryKeyFields.size(); i++) {
                final FieldAccessor fieldAccessor = primaryKeyFields.get(i);
                final Object dbPkValue = primaryKey.get(i);
                final Object convertedPkValue = typeConverter.convert(dbPkValue, fieldAccessor.type());
                fieldAccessor.set(dto, convertedPkValue);
            }
        } else {
            final @Nullable Object[] args = new Object[constructorMappingInfo.canonicalConstructorFieldAccessors().size()];
            final List<FieldAccessor> primaryKeyFields = ormTable.getPrimaryKeyFields();

            for (int i = 0; i < args.length; i++) {
                final FieldAccessor fieldAccessor = constructorMappingInfo.canonicalConstructorFieldAccessors().get(i);
                final int pkIndex = primaryKeyFields.indexOf(fieldAccessor);
                if (pkIndex != -1) {
                    final Object dbPkValue = primaryKey.get(pkIndex);
                    args[i] = typeConverter.convert(dbPkValue, fieldAccessor.type());
                } else {
                    args[i] = ClassUtils.getDefaultValue(fieldAccessor.type());
                }
            }

            try {
                dto = constructorMappingInfo.constructor().invokeWithArguments(args);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
            }
        }

        return dto;
    }

    Column parseTargetColumn(String sqlFunction, final @Nullable String defaultSchema, final TableMetaData rootTableMetaData) {
        final Matcher matcher = FUNCTION_SQL_COLUMN_PATTERN.matcher(sqlFunction);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            final String columnName = matcher.group(2);

            if (tableName != null) {
                if (!StringUtils.isEmpty(defaultSchema)) {
                    tableName = defaultSchema + "." + tableName;
                }

                final Table table = tableRegistry.getOrCreateSpiTable(tableName);
                return new Column(table, columnName);
            } else {
                if (rootTableMetaData.hasColumn(columnName)) {
                    return new Column(rootTableMetaData.table(), columnName);
                }

                throw new IllegalStateException("Cannot infer target table from label: " + sqlFunction);
            }
        } else if (rootTableMetaData.hasColumn(sqlFunction)) {
            return new Column(rootTableMetaData.table(), sqlFunction);
        } else {
            throw new IllegalStateException("Cannot infer target column/table from label: " + sqlFunction);
        }
    }

    /**
     * Cache of "under construction" DTOs.
     */
    static class DtoCache {
        /**
         * Map of OrmTable -> map of primary key -> PartiallyConstructedDtos
         */
        private final Map<OrmTable, Map<Pk, PartiallyConstructedDto>> cache = new HashMap<>();

        public @Nullable PartiallyConstructedDto get(final MappingData mappingData, final Pk primaryKey) {
            final Map<Pk, PartiallyConstructedDto> map = cache.get(mappingData.ormTable());
            return map != null ? map.get(primaryKey) : null;
        }

        public @Nullable PartiallyConstructedDto getByClassAndPk(final Class<?> dtoClass, final Pk primaryKey) {
            for (final Map.Entry<OrmTable, Map<Pk, PartiallyConstructedDto>> entry : cache.entrySet()) {
                if (entry.getKey().dtoClass().equals(dtoClass)) {
                    final PartiallyConstructedDto dto = entry.getValue().get(primaryKey);
                    if (dto != null) {
                        return dto;
                    }
                }
            }
            return null;
        }

        public @Nullable PartiallyConstructedDto get(final FieldMapping fieldMapping) {
            for (final Map<Pk, PartiallyConstructedDto> pkMap : cache.values()) {
                if (!pkMap.isEmpty()) {
                    final PartiallyConstructedDto first = pkMap.values().iterator().next();
                    if (first.mappingData().fieldMappings().contains(fieldMapping)) {
                        return first;
                    }
                }
            }
            return null;
        }

        public void put(final MappingData mappingData, final Pk primaryKey, final PartiallyConstructedDto partiallyConstructedDto) {
            cache.computeIfAbsent(mappingData.ormTable(), k -> new HashMap<>())
                    .put(primaryKey, partiallyConstructedDto);
        }
    }

    interface Pk {
        int size();

        @Nullable Object get(int index);
    }

    static record SinglePk(@Nullable Object value) implements Pk {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public @Nullable Object get(int index) {
            if (index != 0) throw new IndexOutOfBoundsException();
            return value;
        }
    }

    static record CompositePk(Object[] values) implements Pk {
        @Override
        public int size() {
            return values.length;
        }

        @Override
        public Object get(int index) {
            return values[index];
        }

        @Override
        public boolean equals(final @Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final CompositePk that = (CompositePk) o;
            return Arrays.equals(values, that.values);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }
    }

    enum EmptyPk implements Pk {
        INSTANCE;

        @Override
        public int size() {
            return 0;
        }

        @Override
        public @Nullable Object get(int index) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Constructs a DTO instance from the provided field accessor values.
     *
     * @param <DTO>               the target DTO type
     * @param dtoClass            the class of the DTO to construct
     * @param fieldAccessorValues the list of field accessor values
     * @param dtoConstructor      the DTO constructor helper
     * @return the constructed DTO instance
     */
    public static <DTO> DTO constructDto(final Class<DTO> dtoClass, final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues, final DtoConstructor dtoConstructor) {
        final DtoConstructor.ConstructionResult<DTO> constructionResult = dtoConstructor.newInstance(dtoClass, fieldAccessorValues);
        final DTO dto = constructionResult.dto();

        if (constructionResult.defaultConstructorUsed()) {
            for (final DtoConstructor.FieldAccessorValue fieldAccessorValue : fieldAccessorValues) {
                final FieldAccessor fieldAccessor = fieldAccessorValue.field();
                final Object rawValue = fieldAccessorValue.value();
                final Object value;

                if (rawValue == null) {
                    value = ClassUtils.getDefaultValue(fieldAccessor.type());
                } else if (rawValue instanceof DtoConstructor.DtoDependency) {
                    value = null;
                } else {
                    value = rawValue;
                }

                fieldAccessor.set(dto, value);
            }
        }

        return dto;
    }

    record MappingPlan(Map<String, MappingData> mappingDataMap, @Nullable MappingData rootMappingData) {
    }

    static final class DtoData {
        private final Map<FieldAccessor, @Nullable Object> values = new HashMap<>();
        private final Map<FieldAccessor, Collection<Object>> collections = new HashMap<>();

        public void set(final FieldAccessor accessor, final @Nullable Object value) {
            values.put(accessor, value);
        }

        @SuppressWarnings("unchecked")
        public void addToCollection(final FieldAccessor accessor, final Object value) {
            collections.computeIfAbsent(accessor, a -> (Collection<Object>) ClassUtils.newInstance(a.type()))
                    .add(value);
        }

        public Map<FieldAccessor, @Nullable Object> values() {
            return values;
        }

        public Map<FieldAccessor, Collection<Object>> collections() {
            return collections;
        }
    }

    static final class PartiallyConstructedDto {
        private final DtoData dtoData;
        private @Nullable Object dto;
        private final Pk primaryKey;
        private final List<SpecificDtoDependency> dependencies;
        private final MappingData mappingData;
        private boolean instantiating;

        PartiallyConstructedDto(final DtoData dtoData,
                                final Pk primaryKey,
                                final List<SpecificDtoDependency> dependencies,
                                final MappingData mappingData) {
            this.dtoData = dtoData;
            this.primaryKey = primaryKey;
            this.dependencies = dependencies;
            this.mappingData = mappingData;
        }

        public boolean isInstantiating() {
            return instantiating;
        }

        public void setInstantiating(final boolean instantiating) {
            this.instantiating = instantiating;
        }

        public DtoData dtoData() {
            return dtoData;
        }

        public @Nullable Object getDto() {
            return dto;
        }

        public void setDto(final Object dto) {
            this.dto = dto;
        }

        public Pk primaryKey() {
            return primaryKey;
        }

        public List<SpecificDtoDependency> dependencies() {
            return dependencies;
        }

        public MappingData mappingData() {
            return mappingData;
        }
    }

    static final class FieldMapping {
        private final FieldAccessor fieldAccessor;
        private final List<Column> columns;
        private final List<String> columnLabels;
        private final boolean isBasicType;
        private final boolean isRelatedDto;
        private final @Nullable FieldAccessor relatedCollectionField;
        private final @Nullable Class<?> relatedDtoClass;
        private int @Nullable [] columnIndexes;

        FieldMapping(final FieldAccessor fieldAccessor,
                     final List<Column> columns,
                     final List<String> columnLabels,
                     final boolean isBasicType,
                     final boolean isRelatedDto,
                     final @Nullable FieldAccessor relatedCollectionField,
                     final @Nullable Class<?> relatedDtoClass) {
            this.fieldAccessor = fieldAccessor;
            this.columns = columns;
            this.columnLabels = columnLabels;
            this.isBasicType = isBasicType;
            this.isRelatedDto = isRelatedDto;
            this.relatedCollectionField = relatedCollectionField;
            this.relatedDtoClass = relatedDtoClass;
        }

        public FieldAccessor fieldAccessor() {
            return fieldAccessor;
        }

        public List<Column> columns() {
            return columns;
        }

        public List<String> columnLabels() {
            return columnLabels;
        }

        public int[] columnIndexes() {
            return Objects.requireNonNull(columnIndexes);
        }

        public boolean isBasicType() {
            return isBasicType;
        }

        public boolean isRelatedDto() {
            return isRelatedDto;
        }

        public @Nullable FieldAccessor relatedCollectionField() {
            return relatedCollectionField;
        }

        public @Nullable Class<?> relatedDtoClass() {
            return relatedDtoClass;
        }

        public void setColumnIndexes(final int[] columnIndexes) {
            this.columnIndexes = columnIndexes;
        }
    }

    static final class MappingData {
        private final Class<?> dtoClass;
        private final @Nullable Class<?> contextDtoClass;
        private final Table table;
        private final OrmTable ormTable;
        private final int[] pkColumnIndexes;
        private final List<FieldMapping> fieldMappings;
        private @Nullable List<GenericDtoDependency> genericDtoDependencies;

        MappingData(final Class<?> dtoClass,
                    final @Nullable Class<?> contextDtoClass,
                    final Table table,
                    final OrmTable ormTable,
                    final int[] pkColumnIndexes,
                    final List<FieldMapping> fieldMappings) {
            this.dtoClass = dtoClass;
            this.contextDtoClass = contextDtoClass;
            this.table = table;
            this.ormTable = ormTable;
            this.pkColumnIndexes = pkColumnIndexes;
            this.fieldMappings = fieldMappings;
        }

        public Class<?> dtoClass() {
            return dtoClass;
        }

        public @Nullable Class<?> contextDtoClass() {
            return contextDtoClass;
        }

        public Table table() {
            return table;
        }

        public OrmTable ormTable() {
            return ormTable;
        }

        public int[] pkColumnIndexes() {
            return pkColumnIndexes;
        }

        public List<FieldMapping> fieldMappings() {
            return fieldMappings;
        }

        public void addGenericDtoDependency(final GenericDtoDependency genericDtoDependency) {
            if (genericDtoDependencies == null) {
                genericDtoDependencies = new ArrayList<>();
            }

            genericDtoDependencies.add(genericDtoDependency);
        }

        public @Nullable List<GenericDtoDependency> getGenericDtoDependencies() {
            return genericDtoDependencies;
        }
    }

    static record GenericDtoDependency(FieldAccessor field,
                                       FieldMapping relatedFieldMapping) {
    }

    static record SpecificDtoDependency(FieldAccessor field,
                                        Class<?> relatedDtoClass,
                                        @Nullable MappingData targetMappingData,
                                        Pk primaryKeyValue,
                                        @Nullable FieldAccessor relatedCollectionField,
                                        boolean reverseUpdateOnly) {
    }

    static record LateReverseCollectionUpdate(PartiallyConstructedDto hostPartialDto, Object relatedDto,
                                              FieldAccessor relatedCollectionField) {
    }

    static record DeferredCollectionAddition(Collection<Object> collection,
                                             PartiallyConstructedDto itemPartialDto) {
    }
}
