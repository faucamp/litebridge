//package org.litebridge.orm.persistence;
//
//import org.jspecify.annotations.Nullable;
//import org.litebridge.commons.ClassUtils;
//import org.litebridge.commons.CollectionUtils;
//import org.litebridge.db.spi.ColumnMetaData;
//import org.litebridge.db.spi.Row;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.convert.TypeConverter;
//import org.litebridge.orm.api.dto.DtoJoinSpec;
//import org.litebridge.orm.api.dto.DtoSelectSpec;
//import org.litebridge.orm.api.select.model.JoinSpec;
//import org.litebridge.orm.config.RelatedDtoStrategy;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.persistence.DtoConstructor.FieldAccessorValue;
//import org.litebridge.tracking.FieldAccessor;
//import org.litebridge.tracking.FieldAccessorChain;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.IdentityHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//import java.util.stream.Stream;
//
///**
// * The SelectSpecDtoMapper class is responsible for mapping data from rows of a database query to DTO (Data Transfer Object) instances.
// * It uses a "compiled" MappingPlan to achieve high performance by pre-resolving column indices and constructors.
// */
//public class SelectSpecDtoMapper {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSpecDtoMapper.class);
//    private final PartiallyConstructedDtoCache dtoCache;
//    private final TypeConverter typeConverter;
//    private final DtoSelectSpec selectSpec;
//    private final TableRegistry tableRegistry;
//    private final DtoConstructor dtoConstructor;
//    private final LitebridgeContext litebridgeContext;
//    private MappingPlan mappingPlan;
//
//    public SelectSpecDtoMapper(final DtoSelectSpec dtoSelectSpec,
//                               final TypeConverter typeConverter,
//                               final TableRegistry tableRegistry,
//                               final DtoConstructor dtoConstructor,
//                               final LitebridgeContext litebridgeContext) {
//        this.dtoCache = new PartiallyConstructedDtoCache();
//        this.typeConverter = typeConverter;
//        this.selectSpec = dtoSelectSpec;
//        this.tableRegistry = tableRegistry;
//        this.dtoConstructor = dtoConstructor;
//        this.litebridgeContext = litebridgeContext;
//    }
//
//    /**
//     * Safely converts a given value to a specified target type using a type conversion mechanism.
//     *
//     * @param value      The input value to be converted. Can be {@code null}.
//     * @param targetType The target class type to convert the value into. Must not be {@code null}.
//     * @return The converted value if conversion is applicable and successful; otherwise, the original value.
//     */
//    private @Nullable Object safeConvert(final @Nullable Object value, final Class<?> targetType) {
//        if (value == null || targetType == Object.class || targetType.isInstance(value)) {
//            return value;
//        }
//
//        if (!ClassUtils.isBasicType(targetType)) {
//            return value;
//        }
//
//        return typeConverter.convert(value, targetType);
//    }
//
//    public <DTO> List<DTO> toDtos(final Class<DTO> dtoClass, final List<Row> rows) {
//        if (rows.isEmpty() || (selectSpec.getFieldColumns().isEmpty() && !ClassUtils.isBasicType(dtoClass))) {
//            return Collections.emptyList();
//        }
//
//        if (ClassUtils.isBasicType(dtoClass) && selectSpec.getFieldColumns().isEmpty()) {
//            return rows.stream()
//                    .map(row -> {
//                        final Object value = row.getValue(0);
//                        return dtoClass.cast(safeConvert(value, dtoClass));
//                    })
//                    .filter(Objects::nonNull)
//                    .toList();
//        }
//
//        this.mappingPlan = compileMappingPlan(selectSpec.dtoClass(), selectSpec.dtoTable(), selectSpec.getTable(), selectSpec.getFieldColumns(), selectSpec.getJoins(), rows.getFirst());
//
//        final List<PartiallyConstructedDto> rootDtos = new ArrayList<>(rows.size());
//        final Set<List<Object>> seenRootPks = new HashSet<>();
//
//        // Phase 1: Populate Cache
//        for (final Row row : rows) {
//            final PartiallyConstructedDto rootDto = toDto(mappingPlan, row, false);
//
//            if (rootDto != null && seenRootPks.add(rootDto.primaryKey())) {
//                rootDtos.add(rootDto);
//            }
//        }
//
//        // Phase 2: Batch Resolve
//        final Set<Object> resolvedDtoInstances = Collections.newSetFromMap(new IdentityHashMap<>());
//        boolean hasNewResolved;
//        do {
//            hasNewResolved = false;
//            final List<PartiallyConstructedDto> currentCacheSnapshot = dtoCache.stream().toList();
//
//            for (final PartiallyConstructedDto partialDto : currentCacheSnapshot) {
//                if (!resolvedDtoInstances.contains(partialDto.dto())) {
//                    resolveRelatedDtoDependencies(partialDto, resolvedDtoInstances);
//                    hasNewResolved = true;
//                }
//            }
//        } while (hasNewResolved);
//
//        // Phase 3: Extract Results
//        return rootDtos.stream()
//                .map(pdto -> {
//                    final PartiallyConstructedDto latest = dtoCache.get(pdto.ormTable().dtoClass(), pdto.primaryKey());
//                    final Object dto = latest != null ? latest.dto() : pdto.dto();
//                    return dtoClass.cast(dto);
//                })
//                .toList();
//    }
//
//    private MappingPlan compileMappingPlan(final Class<?> dtoClass, final OrmTable ormTable, final Table table, final List<DtoSelectSpec.FieldColumn> fieldColumns, final @Nullable List<JoinSpec> joins, final Row referenceRow) {
//        // Primary Key Indices and Types
//        final List<ColumnMetaData> pkColumns = ormTable.getMetaData().primaryKey();
//        final int[] pkIndices = new int[pkColumns.size()];
//        final Class<?>[] pkTypes = new Class<?>[pkColumns.size()];
//
//        for (int i = 0; i < pkColumns.size(); i++) {
//            final ColumnMetaData pkCol = pkColumns.get(i);
//            final FieldAccessor field = ormTable.getFieldForColumnName(pkCol.name());
//            pkTypes[i] = ClassUtils.isBasicType(field.type()) ? field.type() : Object.class;
//            int index = -1;
//
//            for (final DtoSelectSpec.FieldColumn fc : fieldColumns) {
//                if (fc.column().table().equals(table) && fc.column().name().equals(pkCol.name())) {
//                    index = referenceRow.getColumnIndex(fc.column());
//                    break;
//                }
//            }
//
//            pkIndices[i] = index;
//        }
//
//        // Field Mappings and Nested Plans
//        final List<MappingPlan.FieldMapping> mappings = new ArrayList<>();
//        final Map<FieldAccessor, MappingPlan> nestedPlans = new HashMap<>();
//
//        // Group fields by their first accessor part for nested DTO handling
//        final Map<FieldAccessor, List<DtoSelectSpec.FieldColumn>> groupedByRoot = new HashMap<>();
//        for (final DtoSelectSpec.FieldColumn fc : fieldColumns) {
//            final FieldAccessor accessor = fc.fieldAccessor();
//
//            if (!fc.column().table().equals(table)) {
//                continue;
//            }
//
//            final FieldAccessor rootAccessor = (accessor instanceof FieldAccessorChain chain) ? chain.fieldAccessors().getFirst() : accessor;
//            if (rootAccessor.dtoClass().isAssignableFrom(dtoClass)) {
//                groupedByRoot.computeIfAbsent(rootAccessor, k -> new ArrayList<>()).add(fc);
//            }
//        }
//
//        for (final Map.Entry<FieldAccessor, List<DtoSelectSpec.FieldColumn>> entry : groupedByRoot.entrySet()) {
//            final FieldAccessor accessor = entry.getKey();
//            final List<DtoSelectSpec.FieldColumn> groupedFc = entry.getValue();
//            final Class<?> fieldType = accessor.type();
//
//            boolean isRelatedDto = false;
//            boolean isNestedDto = false;
//            FieldAccessor relatedPkAccessor = null;
//
//            // Check if it's a nested DTO (has multiple fields or the accessor is a chain in the first fc)
//            final boolean hasChains = groupedFc.stream().anyMatch(fc -> fc.fieldAccessor() instanceof FieldAccessorChain);
//
//            if (hasChains && !ClassUtils.isBasicType(fieldType)) {
//                isNestedDto = true;
//                if (!nestedPlans.containsKey(accessor)) {
//                    // Shift field columns for nested plan
//                    final List<DtoSelectSpec.FieldColumn> shiftedFc = groupedFc.stream()
//                            .map(fc -> {
//                                if (fc.fieldAccessor() instanceof FieldAccessorChain chain && chain.fieldAccessors().size() > 1) {
//                                    return new DtoSelectSpec.FieldColumn(chain.subChain(), fc.column());
//                                }
//                                return fc;
//                            })
//                            .toList();
//                    nestedPlans.put(accessor, compileMappingPlan(fieldType, tableRegistry.getOrmTableOrThrow(fieldType), table, shiftedFc, null, referenceRow));
//                }
//            } else if (!ClassUtils.isBasicType(accessor.type())) {
//                isRelatedDto = true;
//                final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(accessor.name());
//
//                if (columnMetaData.getJoinColumn() != null) {
//                    final OrmTable relatedOrmTable = tableRegistry.getTableInContext(accessor.type(), dtoClass)
//                            .orElseGet(() -> tableRegistry.getOrmTableOrThrow(accessor.type()));
//                    relatedPkAccessor = relatedOrmTable.getFieldForColumnName(columnMetaData.getJoinColumn());
//                }
//            }
//
//            // For regular fields or related DTOs, we take the index from the (first) fc
//            final int index = referenceRow.getColumnIndex(groupedFc.getFirst().column());
//            mappings.add(new MappingPlan.FieldMapping(index, accessor, fieldType, isRelatedDto, isNestedDto, relatedPkAccessor));
//        }
//
//        final DtoConstructor.MappingInfo mappingInfo = dtoConstructor.getMappingInfo(dtoClass);
//        final Map<DtoJoinSpec, MappingPlan> joinPlans = new HashMap<>();
//
//        if (joins != null) {
//            for (final JoinSpec join : joins) {
//                if (join instanceof DtoJoinSpec djs) {
//                    final Class<?> sourceClass;
//
//                    if (djs.sourceDtoClass() != null) {
//                        sourceClass = djs.sourceDtoClass();
//                    } else {
//                        sourceClass = selectSpec.dtoClass();
//                    }
//
//                    if (dtoClass.equals(sourceClass)) {
//                        joinPlans.put(djs, compileMappingPlan(djs.dtoClass(), djs.dtoTable(), djs.table(), djs.getFieldColumns(), null, referenceRow));
//                    }
//                }
//            }
//        }
//
//        final List<MappedOneToMany> requestedOneToMany = new ArrayList<>();
//        final List<MappedManyToMany> requestedManyToMany = new ArrayList<>();
//
//        if (joins != null) {
//            for (final JoinSpec join : joins) {
//                if (join instanceof DtoJoinSpec djs && djs.collectionField() != null) {
//                    final Class<?> sourceClass;
//
//                    if (djs.sourceDtoClass() != null) {
//                        sourceClass = djs.sourceDtoClass();
//                    } else {
//                        sourceClass = selectSpec.dtoClass();
//                    }
//
//                    if (dtoClass.equals(sourceClass)) {
//                        ormTable.getOneToManyMappingForField(djs.collectionField())
//                                .ifPresent(requestedOneToMany::add);
//                        ormTable.getManyToManyMappingForField(djs.collectionField())
//                                .ifPresent(requestedManyToMany::add);
//                    }
//                }
//            }
//        }
//
//        return new MappingPlan(
//                dtoClass,
//                ormTable,
//                pkIndices,
//                pkTypes,
//                mappings.toArray(new MappingPlan.FieldMapping[0]),
//                mappingInfo.constructor(),
//                mappingInfo.defaultConstructorUsed(),
//                mappingInfo.canonicalConstructorFieldAccessors(),
//                (Map) joinPlans,
//                nestedPlans,
//                requestedOneToMany,
//                requestedManyToMany
//        );
//    }
//
//    private @Nullable PartiallyConstructedDto toDto(final MappingPlan plan, final Row row, final boolean isJoin) {
//        final List<@Nullable Object> pk = new ArrayList<>(plan.primaryKeyIndices().length);
//        boolean pkNull = true;
//        boolean allPkIndicesMissing = true;
//
//        for (int i = 0; i < plan.primaryKeyIndices().length; i++) {
//            int index = plan.primaryKeyIndices()[i];
//            if (index != -1) {
//                allPkIndicesMissing = false;
//                final Object value = row.getValue(index);
//                if (value != null) {
//                    pkNull = false;
//                    pk.add(safeConvert(value, plan.primaryKeyTypes()[i]));
//                } else {
//                    pk.add(null);
//                }
//            } else {
//                pk.add(null);
//            }
//        }
//
//        if (pkNull && allPkIndicesMissing) {
//            pk.clear();
//            pk.add(row.hashCode());
//            pkNull = false;
//        }
//
//        if (pkNull && isJoin) {
//            return null;
//        }
//
//        PartiallyConstructedDto currentDto = dtoCache.get(plan.dtoClass(), pk);
//
//        if (currentDto == null) {
//            final List<FieldAccessorValue> fieldValues = new ArrayList<>(plan.fieldMappings().length);
//            final List<DtoConstructor.DtoDependency> dependencies = new ArrayList<>();
//
//            for (final MappingPlan.FieldMapping mapping : plan.fieldMappings()) {
//                if (mapping.isNestedDto()) {
//                    final MappingPlan nestedPlan = plan.nestedPlans().get(mapping.accessor());
//                    final PartiallyConstructedDto nestedDto = toDto(nestedPlan, row, false);
//
//                    if (nestedDto != null) {
//                        fieldValues.add(new FieldAccessorValue(mapping.accessor(), nestedDto.dto()));
//                    }
//                } else {
//                    final Object value = mapping.index() != -1 ? row.getValue(mapping.index()) : null;
//
//                    if (mapping.isRelatedDto()) {
//                        if (mapping.relatedPkAccessor() != null) {
//                            final Object relatedPkValue = safeConvert(value, mapping.relatedPkAccessor().type());
//
//                            if (relatedPkValue != null) {
//                                final List<FieldAccessorValue> pkValues = Collections.singletonList(new FieldAccessorValue(mapping.relatedPkAccessor(), relatedPkValue));
//                                final DtoConstructor.DtoDependency dependency = new DtoConstructor.DtoDependency(mapping.accessor(), mapping.fieldType(), pkValues);
//                                dependencies.add(dependency);
//                                fieldValues.add(new FieldAccessorValue(mapping.accessor(), null));
//                            }
//                        }
//                    } else {
//                        fieldValues.add(new FieldAccessorValue(mapping.accessor(), safeConvert(value, mapping.fieldType())));
//                    }
//                }
//            }
//
//            final Object dto;
//
//            if (plan.defaultConstructorUsed()) {
//                try {
//                    dto = plan.constructor().invoke();
//                } catch (Throwable ex) {
//                    throw new IllegalStateException("Failed to instantiate DTO: " + plan.dtoClass(), ex);
//                }
//
//                for (final FieldAccessorValue fav : fieldValues) {
//                    if (fav.value() != null) {
//                        fav.field().set(dto, fav.value());
//                    }
//                }
//            } else {
//                final Map<FieldAccessor, @Nullable Object> valuesByField = new HashMap<>(fieldValues.size());
//
//                for (final FieldAccessorValue fav : fieldValues) {
//                    valuesByField.put(fav.field(), fav.value());
//                }
//
//                final @Nullable Object[] args = new Object[plan.canonicalConstructorFieldAccessors().size()];
//
//                for (int i = 0; i < args.length; i++) {
//                    args[i] = valuesByField.get(plan.canonicalConstructorFieldAccessors().get(i));
//                }
//
//                try {
//                    dto = plan.constructor().invokeWithArguments(args);
//                } catch (Throwable e) {
//                    throw new RuntimeException("Failed to instantiate DTO: " + plan.dtoClass(), e);
//                }
//            }
//
//            currentDto = new PartiallyConstructedDto(dto, pk, dependencies, plan.ormTable());
//            dtoCache.put(pk, currentDto);
//        }
//
//        for (final Map.Entry<?, MappingPlan> entry : plan.joinPlans().entrySet()) {
//            final PartiallyConstructedDto joinedDto = toDto(entry.getValue(), row, true);
//
////            if (joinedDto != null) {
////                if (entry.getKey().collectionField() != null) {
////                    dtoCache.addLink(currentDto.dto(), entry.getKey().collectionField(), joinedDto.dto());
////                }
////                if (entry.getKey().reverseCollectionField() != null) {
////                    dtoCache.addLink(joinedDto.dto(), entry.getKey().reverseCollectionField(), currentDto.dto());
////                }
////            }
//            throw new UnsupportedOperationException("Deprecated");
//        }
//
//        return currentDto;
//    }
//
//    private Object resolveRelatedDtoDependencies(final PartiallyConstructedDto partialDto, final Set<Object> resolvedDtoInstances) {
//        if (resolvedDtoInstances.contains(partialDto.dto())) {
//            return partialDto.dto();
//        }
//
//        PartiallyConstructedDto currentDto = partialDto;
//        Map<FieldAccessor, Object> recordOverrides = null;
//
//        for (final DtoConstructor.DtoDependency dependency : partialDto.dependencies()) {
//            PartiallyConstructedDto relatedDto = dtoCache.get(dependency.targetDtoClass(), dependency.targetPrimaryKeyValue());
//
//            if (relatedDto == null && litebridgeContext.getRelatedDtoStrategy() == RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN) {
//                final Object partial = constructDto(dependency.targetDtoClass(), dependency.targetPrimaryKey(), dtoConstructor);
//                relatedDto = new PartiallyConstructedDto(partial, dependency.targetPrimaryKeyValue(), Collections.emptyList(), tableRegistry.getOrmTableOrThrow(dependency.targetDtoClass()));
//                dtoCache.put(relatedDto.primaryKey(), relatedDto);
//            }
//
//            if (relatedDto != null) {
//                final Object resolvedRelatedInstance = resolveRelatedDtoDependencies(relatedDto, resolvedDtoInstances);
//
//                if (currentDto.dto() instanceof Record) {
//                    if (recordOverrides == null) {
//                        recordOverrides = new HashMap<>();
//                    }
//                    recordOverrides.put(dependency.field(), resolvedRelatedInstance);
//                } else {
//                    dependency.field().set(currentDto.dto(), resolvedRelatedInstance);
//                }
//            }
//        }
//
//        if (recordOverrides != null) {
//            final Object oldDto = currentDto.dto();
//            currentDto = recreateDto(currentDto, recordOverrides);
//            dtoCache.updateInstance(oldDto, currentDto.dto());
//            dtoCache.put(currentDto.primaryKey(), currentDto);
//        }
//
//        updateOneToManyCollectionMappings(currentDto);
//        updateManyToManyCollectionMappings(currentDto);
//        resolvedDtoInstances.add(currentDto.dto());
//        return currentDto.dto();
//    }
//
//    private PartiallyConstructedDto recreateDto(final PartiallyConstructedDto partialDto, final Map<FieldAccessor, Object> valueOverrides) {
//        final Class<?> dtoClass = partialDto.dto().getClass();
//        final DtoConstructor.MappingInfo mappingInfo = dtoConstructor.getMappingInfo(dtoClass);
//        final List<FieldAccessorValue> fieldValues = new ArrayList<>();
//
//        final OrmTable table = tableRegistry.getOrmTableOrThrow(dtoClass);
//        table.fieldAcessorStream().forEach(accessor -> {
//            Object value = valueOverrides.get(accessor);
//
//            if (value == null) {
//                try {
//                    value = accessor.get(partialDto.dto());
//                } catch (Exception e) {
//                    // Ignore
//                }
//            }
//            fieldValues.add(new FieldAccessorValue(accessor, value));
//        });
//
//        final Object dto;
//
//        if (mappingInfo.defaultConstructorUsed()) {
//            try {
//                dto = mappingInfo.constructor().invoke();
//            } catch (Throwable e) {
//                throw new RuntimeException("Failed to recreate DTO: " + dtoClass, e);
//            }
//            for (final FieldAccessorValue fav : fieldValues) {
//                fav.field().set(dto, fav.value());
//            }
//        } else {
//            final Map<FieldAccessor, @Nullable Object> valuesByField = new HashMap<>();
//
//            for (final FieldAccessorValue fav : fieldValues) {
//                valuesByField.put(fav.field(), fav.value());
//            }
//
//            final @Nullable Object[] args = new Object[mappingInfo.canonicalConstructorFieldAccessors().size()];
//
//            for (int i = 0; i < args.length; i++) {
//                args[i] = valuesByField.get(mappingInfo.canonicalConstructorFieldAccessors().get(i));
//            }
//
//            try {
//                dto = mappingInfo.constructor().invokeWithArguments(args);
//            } catch (Throwable e) {
//                throw new RuntimeException("Failed to recreate DTO: " + dtoClass, e);
//            }
//        }
//
//        return new PartiallyConstructedDto(dto, partialDto.primaryKey(), partialDto.dependencies(), partialDto.ormTable());
//    }
//
//    @SuppressWarnings("unchecked")
//    private void updateOneToManyCollectionMappings(final PartiallyConstructedDto partialDto) {
//        final Object dto = partialDto.dto();
//        final OrmTable table = partialDto.ormTable();
//        final List<MappedOneToMany> mappedOneToManyList = table.getOneToManyMappings();
//
//        if (CollectionUtils.isEmpty(mappedOneToManyList)) {
//            return;
//        }
//
//        final boolean isRoot = table.dtoClass().equals(selectSpec.dtoClass());
//
//        mappedOneToManyList.forEach(mappedOneToMany -> {
//            final FieldAccessor collection = mappedOneToMany.collection();
//            final Collection<Object> links = dtoCache.getLinks(dto, collection);
//
//            if (links.isEmpty() && !isRoot) {
//                return;
//            }
//
//            final Collection<Object> currentCollection;
//            final Collection<Object> dtoCollection = (Collection<Object>) collection.get(dto);
//
//            if (dtoCollection != null) {
//                currentCollection = dtoCollection;
//            } else {
//                currentCollection = (Collection<Object>) ClassUtils.newInstance(collection.type());
//                collection.set(dto, currentCollection);
//            }
//
//            for (final Object targetDto : links) {
//                if (!currentCollection.contains(targetDto)) {
//                    currentCollection.add(targetDto);
//                }
//            }
//        });
//    }
//
//    @SuppressWarnings("unchecked")
//    private void updateManyToManyCollectionMappings(final PartiallyConstructedDto partialDto) {
//        final Object dto = partialDto.dto();
//        final OrmTable table = partialDto.ormTable();
//        final List<MappedManyToMany> mappedManyToManyList = table.getManyToManyMappings();
//
//        if (CollectionUtils.isEmpty(mappedManyToManyList)) {
//            return;
//        }
//
//        final boolean isRoot = table.dtoClass().equals(selectSpec.dtoClass());
//
//        mappedManyToManyList.forEach(mappedManyToMany -> {
//            final FieldAccessor collection = mappedManyToMany.collection();
//            final Collection<Object> links = dtoCache.getLinks(dto, collection);
//
//            if (links.isEmpty() && !isRoot) {
//                return;
//            }
//
//            final Collection<Object> currentCollection;
//            final Collection<Object> dtoCollection = (Collection<Object>) collection.get(dto);
//
//            if (dtoCollection != null) {
//                currentCollection = dtoCollection;
//            } else {
//                currentCollection = (Collection<Object>) ClassUtils.newInstance(collection.type());
//                collection.set(dto, currentCollection);
//            }
//
//            for (final Object targetDto : links) {
//                if (!currentCollection.contains(targetDto)) {
//                    currentCollection.add(targetDto);
//                }
//            }
//        });
//    }
//
//    public static <DTO> DTO constructDto(final Class<DTO> dtoClass, final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues, final DtoConstructor dtoConstructor) {
//        final DtoConstructor.ConstructionResult<DTO> constructionResult = dtoConstructor.newInstance(dtoClass, fieldAccessorValues);
//        final DTO dto = constructionResult.dto();
//
//        if (constructionResult.defaultConstructorUsed()) {
//            fieldAccessorValues.forEach(fieldAccessorValue -> {
//                final FieldAccessor fieldAccessor = fieldAccessorValue.field();
//                final Object rawValue = fieldAccessorValue.value();
//                final Object value;
//
//                if (rawValue == null) {
//                    value = ClassUtils.getDefaultValue(fieldAccessor.type());
//                } else if (fieldAccessorValue.value() instanceof DtoConstructor.DtoDependency dependency) {
//                    value = null;
//                } else {
//                    value = fieldAccessorValue.value();
//                }
//
//                fieldAccessorValue.field().set(dto, value);
//            });
//        }
//
//        return dto;
//    }
//
//    private record PartiallyConstructedDto(Object dto,
//                                           List<Object> primaryKey,
//                                           List<DtoConstructor.DtoDependency> dependencies,
//                                           OrmTable ormTable) {
//    }
//
//    private static final class PartiallyConstructedDtoCache {
//        private final Map<Class<?>, Map<List<Object>, SelectSpecDtoMapper.PartiallyConstructedDto>> cache = new IdentityHashMap<>();
//        private final Map<Object, Map<FieldAccessor, Set<Object>>> links = new IdentityHashMap<>();
//
//        public @Nullable PartiallyConstructedDto get(final Class<?> dtoClass, final List<Object> id) {
//            final Map<List<Object>, PartiallyConstructedDto> classCache = cache.get(dtoClass);
//
//            if (classCache != null) {
//                final PartiallyConstructedDto dto = classCache.get(id);
//                if (dto != null) return dto;
//            }
//
//            for (Map.Entry<Class<?>, Map<List<Object>, PartiallyConstructedDto>> entry : cache.entrySet()) {
//                if (dtoClass.isAssignableFrom(entry.getKey())) {
//                    PartiallyConstructedDto dto = entry.getValue().get(id);
//                    if (dto != null) {
//                        return dto;
//                    }
//                }
//            }
//
//            return null;
//        }
//
//        private void put(final List<Object> id, final SelectSpecDtoMapper.PartiallyConstructedDto dto) {
//            cache.computeIfAbsent(dto.ormTable().dtoClass(), cls -> new HashMap<>())
//                    .put(id, dto);
//        }
//
//        private void addLink(final Object source, final FieldAccessor collection, final Object target) {
//            links.computeIfAbsent(source, k -> new IdentityHashMap<>())
//                    .computeIfAbsent(collection, k -> Collections.newSetFromMap(new IdentityHashMap<>()))
//                    .add(target);
//        }
//
//        private Collection<Object> getLinks(final Object source, final FieldAccessor collection) {
//            final Map<FieldAccessor, Set<Object>> sourceLinks = links.get(source);
//
//            if (sourceLinks == null) {
//                return Collections.emptyList();
//            }
//
//            final Set<Object> collectionLinks = sourceLinks.get(collection);
//            return collectionLinks != null ? collectionLinks : Collections.emptyList();
//        }
//
//        private void updateInstance(final Object oldInstance, final Object newInstance) {
//            final Map<FieldAccessor, Set<Object>> oldLinks = links.remove(oldInstance);
//
//            if (oldLinks != null) {
//                links.put(newInstance, oldLinks);
//            }
//
//            // Also need to update where this instance was a TARGET
//            for (final Map<FieldAccessor, Set<Object>> sourceMap : links.values()) {
//                for (final Set<Object> targets : sourceMap.values()) {
//                    if (targets.remove(oldInstance)) {
//                        targets.add(newInstance);
//                    }
//                }
//            }
//        }
//
//        private Stream<PartiallyConstructedDto> stream() {
//            return cache.values().stream()
//                    .flatMap(pkPcDto -> pkPcDto.values().stream());
//        }
//    }
//}
