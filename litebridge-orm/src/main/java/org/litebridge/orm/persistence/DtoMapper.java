package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Row;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final Pattern FUNCTION_SQL_COLUMN_PATTERN = Pattern.compile("(?i)(?:(\\w+)\\$)?(\\w+)");

    private final DtoCache dtoCache = new DtoCache();
    private final TypeConverter typeConverter;
    private final TableRegistry tableRegistry;
    private final DtoConstructor dtoConstructor;
    private final LitebridgeContext litebridgeContext;

    public DtoMapper(final DtoConstructor dtoConstructor,
                     final LitebridgeContext litebridgeContext) {
        this.typeConverter = litebridgeContext.typeConverter();
        this.tableRegistry = litebridgeContext.tableRegistry();
        this.dtoConstructor = dtoConstructor;
        this.litebridgeContext = litebridgeContext;
    }

    public <DTO> List<DTO> toDtos(final Class<DTO> dtoClass, final @Nullable Class<?> contextDtoClass, final List<Row> rows) {
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        // Compile per-DTO mapping data; key is the table alias or table name (if no alias exists)
        final MappingPlan compilationResult = compileMappingPlan(dtoClass, contextDtoClass, rows);

        if (compilationResult.rootMappingData() == null) {
            return Collections.emptyList();
        }

        // Create DTOs and populate the cache
        final Map<MappingData, List<PartiallyConstructedDto>> createdDtosByMapping = cacheDtos(rows, compilationResult.mappingDataMap());

        // Resolve inter-DTO dependencies
        resolveDependencies(createdDtosByMapping);

        // Return all unique DTOs assignable to the requested type, in order of mappings then rows
        final List<DTO> result = new ArrayList<>();
        final Set<Object> seenDtos = new HashSet<>();

        for (final Row row : rows) {
            final List<MappingData> mappings = new ArrayList<>(compilationResult.mappingDataMap().values());
            Collections.reverse(mappings);

            for (MappingData mappingData : mappings) {
                if (dtoClass.isAssignableFrom(mappingData.dtoClass())) {
                    final Pk pk = getPrimaryKey(mappingData, row);
                    final PartiallyConstructedDto pd = dtoCache.get(mappingData, pk);
                    if (pd != null && pd.getDto() != null) {
                        final Object dto = pd.getDto();
                        if (seenDtos.add(dto)) {
                            result.add((DTO) dto);
                        }
                    }
                }
            }
        }

        return result;
    }

    private MappingPlan compileMappingPlan(final Class<?> dtoClass, final @Nullable Class<?> contextDtoClass, final List<Row> rows) {
        final TableMetaData dtoClassTableMetaData;
        final OrmTable rootOrmTable;

        if (contextDtoClass != null) {
            rootOrmTable = tableRegistry.getTableInContextOrThrow(dtoClass, contextDtoClass);
        } else {
            rootOrmTable = tableRegistry.getOrmTableOrThrow(dtoClass);
        }

        dtoClassTableMetaData = rootOrmTable.getMetaData();

        final Map<String, MappingData> mappingDataMap = new LinkedHashMap<>();
        MappingData rootMappingData = null;
        int columnIndex = 0;

        for (final Row.RowColumn rowColumn : rows.getFirst().columns()) {
            final Column column = rowColumn.column();
            final Table table;

            if (column.hasTable()) {
                table = column.table();
            } else {
                table = parseTargetColumn(column.name(), dtoClassTableMetaData.schema()).table();
            }

            final MappingData mappingData = createMappingDataIfAbsent(mappingDataMap, table, contextDtoClass);

            if (rootMappingData == null && mappingData.ormTable().equals(rootOrmTable)) {
                rootMappingData = mappingData;
            }

            final FieldAccessor fieldAccessor = mappingData.ormTable().getFieldForColumnName(column.name());

            // Check if we already have a mapping for this field
            FieldMapping fieldMapping = null;
            final List<FieldMapping> currentMappings = mappingData.fieldMappings();

            for (int i = 0; i < currentMappings.size(); i++) {
                final FieldMapping mapping = currentMappings.get(i);
                if (mapping.fieldAccessor().equals(fieldAccessor)) {
                    fieldMapping = mapping;
                    break;
                }
            }

            if (fieldMapping != null) {
                fieldMapping.columns().add(column);
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

                            if (target instanceof MappedOneToMany mappedOneToMany && mappedOneToMany.mappedByField().equals(fieldAccessor)) {
                                relatedCollectionField = mappedOneToMany.collection();
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

                fieldMapping = new FieldMapping(fieldAccessor, new ArrayList<>(List.of(column)), basicType, relatedDto, relatedCollectionField, relatedDtoClass);
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
            final Row firstRow = rows.getFirst();

            for (final FieldMapping fieldMapping : fieldMappings) {
                if (fieldMapping.isRelatedDto() && fieldMapping.columns().size() > 1) {
                    sortColumnsForRelatedDto(fieldMapping, mappingData.ormTable());
                }

                final int[] columnIndexes = new int[fieldMapping.columns().size()];

                for (int i = 0; i < columnIndexes.length; i++) {
                    columnIndexes[i] = firstRow.getColumnIndex(fieldMapping.columns().get(i));
                }

                fieldMapping.setColumnIndexes(columnIndexes);
            }

            // Pre-map constructor arguments
            final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(mappingData.dtoClass(), contextDtoClass);
            final int[] constructorArgIndices;

            if (!constructorMappingInfo.defaultConstructorUsed()) {
                final List<FieldAccessor> canonicalAccessors = constructorMappingInfo.canonicalConstructorFieldAccessors();
                constructorArgIndices = new int[canonicalAccessors.size()];

                for (int i = 0; i < constructorArgIndices.length; i++) {
                    final FieldAccessor accessor = canonicalAccessors.get(i);
                    int foundIndex = -1;

                    for (int j = 0; j < fieldMappings.size(); j++) {
                        if (fieldMappings.get(j).fieldAccessor().equals(accessor)) {
                            foundIndex = j;
                            break;
                        }
                    }

                    constructorArgIndices[i] = foundIndex;
                }
            } else {
                constructorArgIndices = new int[0];
            }

            mappingData.setConstructorArgIndices(constructorArgIndices);

            // Check for implicit related DTOs
            final OrmTable ormTable = mappingData.ormTable();

            for (ColumnMetaData mappedColumn : ormTable.mappedColumns()) {
                final List<ForeignKeyConstraint> foreignKeyConstraints = mappedColumn.getForeignKeyConstraints();

                if (foreignKeyConstraints.isEmpty()) {
                    continue;
                }

                for (ForeignKeyConstraint foreignKeyConstraint : foreignKeyConstraints) {
                    final Column fkColumn = foreignKeyConstraint.foreignKey();

                    final FieldMapping targetFieldMapping = mappingDataMap.values().stream()
                            .filter(targetMappingData -> targetMappingData.table().equalsIgnoreAlias(fkColumn.table()))
                            .flatMap(targetMappingData -> targetMappingData.fieldMappings().stream())
                            .filter(fieldMapping -> fieldMapping.columns().stream().anyMatch(c -> c.equalsIgnoreAlias(fkColumn)))
                            .findFirst()
                            .orElse(null);

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
        final OrmTable targetOrmTable = tableRegistry.getOrmTableOrThrow(targetDtoClass);
        final List<FieldAccessor> targetPkFields = targetOrmTable.getPrimaryKeyFields();

        if (targetPkFields.size() != fieldMapping.columns().size()) {
            LOGGER.warn("Number of columns ({}) for field {} does not match target PK size ({}) for DTO {}",
                    fieldMapping.columns().size(), fieldMapping.fieldAccessor().name(), targetPkFields.size(), targetDtoClass.getName());
            return;
        }

        final Column[] sortedColumns = new Column[targetPkFields.size()];

        for (final Column column : fieldMapping.columns()) {
            final ColumnMetaData columnMetaData = ormTable.getColumnMetaData(column.name());
            final ForeignKeyConstraint constraint = columnMetaData.getForeignKeyConstraints().stream()
                    .filter(fk -> fk.foreignKey().table().equalsIgnoreAlias(targetOrmTable.getMetaData().toTable()))
                    .findFirst()
                    .orElse(null);

            if (constraint != null) {
                final FieldAccessor targetPkField = targetOrmTable.getFieldForColumnName(constraint.foreignKey().name());
                final int pkIndex = targetPkFields.indexOf(targetPkField);
                if (pkIndex != -1) {
                    sortedColumns[pkIndex] = column;
                }
            }
        }

        // Verify we found all columns
        for (int i = 0; i < sortedColumns.length; i++) {
            if (sortedColumns[i] == null) {
                LOGGER.warn("Could not find column for PK field {} of DTO {} in columns of field {}",
                        targetPkFields.get(i).name(), targetDtoClass.getName(), fieldMapping.fieldAccessor().name());
                // Don't reorder if incomplete
                return;
            }
        }

        fieldMapping.columns().clear();
        fieldMapping.columns().addAll(Arrays.asList(sortedColumns));
    }

    private MappingData createMappingDataIfAbsent(final Map<String, MappingData> mappingDataMap, final Table table, final @Nullable Class<?> contextDtoClass) {
        final String key = table.alias() != null ? table.alias() : table.qualifiedName();
        return mappingDataMap.computeIfAbsent(key, alias -> {
            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
            final List<FieldAccessor> pkFields = ormTable.getPrimaryKeyFields();
            return new MappingData(ormTable.dtoClass(),
                    contextDtoClass,
                    table,
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
            return new SinglePk(row.getValue(pkColumnIndexes[0]));
        } else {
            final Object[] values = new Object[pkColumnIndexes.length];

            for (int i = 0; i < pkColumnIndexes.length; i++) {
                values[i] = row.getValue(pkColumnIndexes[i]);
            }

            return new CompositePk(values);
        }
    }

    private Map<MappingData, List<PartiallyConstructedDto>> cacheDtos(final List<Row> rows, final Map<String, MappingData> mappingDataMap) {
        final Map<MappingData, List<PartiallyConstructedDto>> createdDtosByMapping = new HashMap<>();

        for (MappingData mappingData : mappingDataMap.values()) {
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
        for (int i = 0; i < fieldMappings.size(); i++) {
            final FieldMapping fieldMapping = fieldMappings.get(i);
            final FieldAccessor fieldAccessor = fieldMapping.fieldAccessor();

            if (fieldMapping.isBasicType()) {
                // Basic field
                final Object dbValue = row.getValue(fieldMapping.columnIndexes()[0]);
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
                    pk = new SinglePk(row.getValue(fieldMapping.columnIndexes()[0]));
                } else {
                    final Object[] pkValues = new Object[fieldMapping.columnIndexes().length];

                    for (int j = 0; j < pkValues.length; j++) {
                        pkValues[j] = row.getValue(fieldMapping.columnIndexes()[j]);
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

            return mappingDataMap.values().stream()
                    .filter(md -> md.dtoClass().equals(fieldMapping.fieldAccessor().type())
                            && Objects.equals(md.ormTable().getMetaData().schema(), targetMeta.schema())
                            && md.ormTable().getMetaData().name().equals(targetMeta.name()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    private void resolveDependencies(final Map<MappingData, List<PartiallyConstructedDto>> createdDtosByMapping) {
        final List<LateReverseCollectionUpdate> lateReverseUpdates = new ArrayList<>();
        final Set<PartiallyConstructedDto> allPartialDtos = createdDtosByMapping.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        // Resolve dependencies and populate DtoData
        for (final PartiallyConstructedDto partialDto : allPartialDtos) {
            final OrmTable ormTable = partialDto.mappingData().ormTable();
            final DtoData dtoData = partialDto.dtoData();
            final List<MappedManyToMany> mappedManyToManyList = ormTable.getManyToManyMappings();

            for (final MappedManyToMany mappedManyToMany : mappedManyToManyList) {
                final OrmTable targetOrmTable = mappedManyToMany.targetOrmTable().get();
                final List<PartiallyConstructedDto> matchingCreatedDtos = allPartialDtos.stream()
                        .filter(pd -> pd.mappingData().dtoClass().equals(targetOrmTable.dtoClass()))
                        .toList();

                if (!matchingCreatedDtos.isEmpty()) {
                    final FieldAccessor collectionFieldAccessor = mappedManyToMany.collection();

                    for (final PartiallyConstructedDto matchingCreatedDto : matchingCreatedDtos) {
                        dtoData.addToCollection(collectionFieldAccessor, matchingCreatedDto);
                    }
                }
            }

            final List<GenericDtoDependency> genericDtoDependencies = partialDto.mappingData().getGenericDtoDependencies();
            final List<SpecificDtoDependency> specificDtoDepencies = partialDto.dependencies();
            final Set<FieldAccessor> specificDependenciesResolved = new HashSet<>();

            for (int i = 0; i < specificDtoDepencies.size(); i++) {
                final SpecificDtoDependency specificDtoDependency = specificDtoDepencies.get(i);
                final Class<?> relatedDtoClass = specificDtoDependency.relatedDtoClass();
                final PartiallyConstructedDto targetDto = specificDtoDependency.targetMappingData() != null ?
                        dtoCache.get(specificDtoDependency.targetMappingData(), specificDtoDependency.primaryKeyValue()) :
                        dtoCache.getByClassAndPk(specificDtoDependency.relatedDtoClass(), specificDtoDependency.primaryKeyValue());
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
                for (GenericDtoDependency genericDtoDependency : genericDtoDependencies) {
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

        // Instantiate all DTOs
        for (final PartiallyConstructedDto partialDto : allPartialDtos) {
            instantiateDto(partialDto);
        }

        // Late reverse updates for already-instantiated DTOs
        for (LateReverseCollectionUpdate update : lateReverseUpdates) {
            updateReverseCollection(update.hostPartialDto.getDto(), update.relatedDto, update.relatedCollectionField);
        }
    }

    private Object instantiateDto(final PartiallyConstructedDto partialDto) {
        if (partialDto.getDto() != null) {
            return partialDto.getDto();
        }

        final MappingData mappingData = partialDto.mappingData();
        final Class<?> dtoClass = mappingData.dtoClass();
        final DtoData dtoData = partialDto.dtoData();
        final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(dtoClass, mappingData.contextDtoClass());

        if (constructorMappingInfo.defaultConstructorUsed()) {
            final Object dto;
            try {
                dto = constructorMappingInfo.constructor().invoke();
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
            }
            partialDto.setDto(dto);

            // Populate fields
            for (Map.Entry<FieldAccessor, Object> entry : dtoData.values().entrySet()) {
                final FieldAccessor accessor = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof PartiallyConstructedDto depPartialDto) {
                    value = instantiateDto(depPartialDto);
                }

                if (value != null || !accessor.type().isPrimitive()) {
                    accessor.set(dto, value);
                }
            }

            for (Map.Entry<FieldAccessor, Collection<Object>> entry : dtoData.collections().entrySet()) {
                final FieldAccessor accessor = entry.getKey();
                final Collection<Object> partialCollection = entry.getValue();
                final Collection<Object> finalCollection = (Collection<Object>) ClassUtils.newInstance(accessor.type());

                for (Object item : partialCollection) {
                    if (item instanceof PartiallyConstructedDto itemPartialDto) {
                        finalCollection.add(instantiateDto(itemPartialDto));
                    } else {
                        finalCollection.add(item);
                    }
                }

                accessor.set(dto, finalCollection);
            }
            return dto;
        } else {
            final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues = new ArrayList<>();
            for (Map.Entry<FieldAccessor, Object> entry : dtoData.values().entrySet()) {
                final FieldAccessor accessor = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof PartiallyConstructedDto depPartialDto) {
                    value = instantiateDto(depPartialDto);
                }

                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(accessor, value));
            }
            for (Map.Entry<FieldAccessor, Collection<Object>> entry : dtoData.collections().entrySet()) {
                final FieldAccessor accessor = entry.getKey();
                final Collection<Object> partialCollection = entry.getValue();
                final Collection<Object> finalCollection = (Collection<Object>) ClassUtils.newInstance(accessor.type());

                for (Object item : partialCollection) {
                    if (item instanceof PartiallyConstructedDto itemPartialDto) {
                        finalCollection.add(instantiateDto(itemPartialDto));
                    } else {
                        finalCollection.add(item);
                    }
                }

                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(accessor, finalCollection));
            }

            // Fill in default values for canonical constructor if missing
            final List<FieldAccessor> canonicalAccessors = constructorMappingInfo.canonicalConstructorFieldAccessors();
            final Set<FieldAccessor> providedAccessors = new HashSet<>();

            for (DtoConstructor.FieldAccessorValue fav : fieldAccessorValues) {
                providedAccessors.add(fav.field());
            }

            for (FieldAccessor canonicalAccessor : canonicalAccessors) {
                if (!providedAccessors.contains(canonicalAccessor)) {
                    fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(canonicalAccessor, ClassUtils.getDefaultValue(canonicalAccessor.type())));
                }
            }

            final Object dto = constructDto(dtoClass, fieldAccessorValues, dtoConstructor);
            partialDto.setDto(dto);
            return dto;
        }
    }

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

    private Object createDtoPrimaryKeyOnly(final Class<?> dtoClass, final @Nullable Class<?> contextDtoClass, final Pk primaryKey) {
        final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(dtoClass, contextDtoClass);
        final Object dto;

        if (constructorMappingInfo.defaultConstructorUsed()) {
            try {
                dto = constructorMappingInfo.constructor().invoke();
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
            }

            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(dtoClass);
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
            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(dtoClass);
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

    private Column parseTargetColumn(String sqlFunction, final String defaultSchema) {
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
                throw new IllegalStateException("Cannot infer target table from label: " + sqlFunction);
            }
        } else {
            throw new IllegalStateException("Cannot infer target column/table from label: " + sqlFunction);
        }
    }

    /**
     * Cache of "under construction" DTOs.
     */
    private static class DtoCache {
        /**
         * Map of OrmTable -> map of primary key -> PartiallyConstructedDtos
         */
        private final Map<OrmTable, Map<Pk, PartiallyConstructedDto>> cache = new HashMap<>();

        public @Nullable PartiallyConstructedDto get(final MappingData mappingData, final Pk primaryKey) {
            final Map<Pk, PartiallyConstructedDto> map = cache.get(mappingData.ormTable());
            return map != null ? map.get(primaryKey) : null;
        }

        public @Nullable PartiallyConstructedDto getByClassAndPk(final Class<?> dtoClass, final Pk primaryKey) {
            for (Map.Entry<OrmTable, Map<Pk, PartiallyConstructedDto>> entry : cache.entrySet()) {
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
            for (Map.Entry<OrmTable, Map<Pk, PartiallyConstructedDto>> entry : cache.entrySet()) {
                final Map<Pk, PartiallyConstructedDto> pkMap = entry.getValue();
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

    private interface Pk {
        int size();

        @Nullable Object get(int index);
    }

    private record SinglePk(@Nullable Object value) implements Pk {
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

    private record CompositePk(Object[] values) implements Pk {
        @Override
        public int size() {
            return values.length;
        }

        @Override
        public @Nullable Object get(int index) {
            return values[index];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompositePk that = (CompositePk) o;
            return Arrays.equals(values, that.values);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }
    }

    private enum EmptyPk implements Pk {
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

    public static <DTO> DTO constructDto(final Class<DTO> dtoClass, final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues, final DtoConstructor dtoConstructor) {
        final DtoConstructor.ConstructionResult<DTO> constructionResult = dtoConstructor.newInstance(dtoClass, fieldAccessorValues);
        final DTO dto = constructionResult.dto();

        if (constructionResult.defaultConstructorUsed()) {
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

    private record MappingPlan(Map<String, MappingData> mappingDataMap, @Nullable MappingData rootMappingData) {
    }

    private static final class DtoData {
        private final Map<FieldAccessor, Object> values = new HashMap<>();
        private final Map<FieldAccessor, Collection<Object>> collections = new HashMap<>();

        public void set(FieldAccessor accessor, Object value) {
            values.put(accessor, value);
        }

        public Object get(FieldAccessor accessor) {
            return values.get(accessor);
        }

        public void addToCollection(FieldAccessor accessor, Object value) {
            collections.computeIfAbsent(accessor, a -> (Collection<Object>) ClassUtils.newInstance(a.type()))
                    .add(value);
        }

        public Map<FieldAccessor, Object> values() {
            return values;
        }

        public Map<FieldAccessor, Collection<Object>> collections() {
            return collections;
        }
    }

    private static final class PartiallyConstructedDto {
        private final DtoData dtoData;
        private @Nullable Object dto;
        private final Pk primaryKey;
        private final List<SpecificDtoDependency> dependencies;
        private final MappingData mappingData;

        private PartiallyConstructedDto(DtoData dtoData,
                                        Pk primaryKey,
                                        List<SpecificDtoDependency> dependencies,
                                        MappingData mappingData) {
            this.dtoData = dtoData;
            this.primaryKey = primaryKey;
            this.dependencies = dependencies;
            this.mappingData = mappingData;
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

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PartiallyConstructedDto) obj;
            return Objects.equals(this.dtoData, that.dtoData) &&
                    Objects.equals(this.dto, that.dto) &&
                    Objects.equals(this.primaryKey, that.primaryKey) &&
                    Objects.equals(this.dependencies, that.dependencies) &&
                    Objects.equals(this.mappingData, that.mappingData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dtoData, dto, primaryKey, dependencies, mappingData);
        }

        @Override
        public String toString() {
            return "PartiallyConstructedDto[" +
                    "dtoData=" + dtoData + ", " +
                    "dto=" + dto + ", " +
                    "primaryKey=" + primaryKey + ", " +
                    "dependencies=" + dependencies + ", " +
                    "mappingData=" + mappingData + ']';
        }

    }

    private static final class FieldMapping {
        private final FieldAccessor fieldAccessor;
        private final List<Column> columns;
        private final boolean isBasicType;
        private final boolean isRelatedDto;
        private final @Nullable FieldAccessor relatedCollectionField;
        private final @Nullable Class<?> relatedDtoClass;
        private int @Nullable [] columnIndexes;

        private FieldMapping(FieldAccessor fieldAccessor,
                             List<Column> columns,
                             boolean isBasicType,
                             boolean isRelatedDto,
                             @Nullable FieldAccessor relatedCollectionField,
                             @Nullable Class<?> relatedDtoClass) {
            this.fieldAccessor = fieldAccessor;
            this.columns = columns;
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

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (FieldMapping) obj;
            return Objects.equals(this.fieldAccessor, that.fieldAccessor) &&
                    Objects.equals(this.columns, that.columns) &&
                    Objects.equals(this.columnIndexes, that.columnIndexes) &&
                    this.isBasicType == that.isBasicType &&
                    this.isRelatedDto == that.isRelatedDto &&
                    Objects.equals(this.relatedCollectionField, that.relatedCollectionField) &&
                    Objects.equals(this.relatedDtoClass, that.relatedDtoClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fieldAccessor, columns, columnIndexes, isBasicType, isRelatedDto, relatedCollectionField, relatedDtoClass);
        }

        @Override
        public String toString() {
            return "FieldMapping[" +
                    "fieldAccessor=" + fieldAccessor + ", " +
                    "columns=" + columns + ", " +
                    "columnIndexes=" + columnIndexes + ", " +
                    "isBasicType=" + isBasicType + ", " +
                    "isRelatedDto=" + isRelatedDto + ", " +
                    "relatedCollectionField=" + relatedCollectionField + ", " +
                    "relatedDtoClass=" + relatedDtoClass + ']';
        }

    }

    private static final class MappingData {
        private final Class<?> dtoClass;
        private final @Nullable Class<?> contextDtoClass;
        private final Table table;
        private final OrmTable ormTable;
        private final int[] pkColumnIndexes;
        private final List<FieldMapping> fieldMappings;
        private @Nullable int[] constructorArgIndices;
        private @Nullable List<GenericDtoDependency> genericDtoDependencies;

        private MappingData(final Class<?> dtoClass,
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

        public Class<?> contextDtoClass() {
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

        public int[] getConstructorArgIndices() {
            return constructorArgIndices;
        }

        public void setConstructorArgIndices(final @Nullable int[] constructorArgIndices) {
            this.constructorArgIndices = constructorArgIndices;
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

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (MappingData) obj;
            return Objects.equals(this.dtoClass, that.dtoClass) &&
                    Objects.equals(this.table, that.table) &&
                    Objects.equals(this.ormTable, that.ormTable) &&
                    Objects.equals(this.pkColumnIndexes, that.pkColumnIndexes) &&
                    Objects.equals(this.fieldMappings, that.fieldMappings) &&
                    Objects.equals(this.constructorArgIndices, that.constructorArgIndices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dtoClass, table, ormTable, pkColumnIndexes, fieldMappings, constructorArgIndices);
        }

        @Override
        public String toString() {
            return "MappingData[" +
                    "dtoClass=" + dtoClass + ", " +
                    "table=" + table + ", " +
                    "ormTable=" + ormTable + ", " +
                    "pkColumnIndexes=" + pkColumnIndexes + ", " +
                    "fieldMappings=" + fieldMappings + ", " +
                    "constructorArgIndices=" + constructorArgIndices + ']';
        }
    }

    private record GenericDtoDependency(FieldAccessor field,
                                        FieldMapping relatedFieldMapping) {
    }

    private record SpecificDtoDependency(FieldAccessor field,
                                         Class<?> relatedDtoClass,
                                         @Nullable MappingData targetMappingData,
                                         Pk primaryKeyValue,
                                         @Nullable FieldAccessor relatedCollectionField,
                                         boolean reverseUpdateOnly) {
    }

    private record LateReverseCollectionUpdate(PartiallyConstructedDto hostPartialDto, Object relatedDto, FieldAccessor relatedCollectionField) {
    }
}
