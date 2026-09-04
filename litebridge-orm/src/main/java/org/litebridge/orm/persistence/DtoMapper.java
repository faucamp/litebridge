package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SelectSpecDtoMapper class is responsible for mapping data from rows of a database query to DTO (Data Transfer Object) instances.
 * It uses a "compiled" MappingPlan to achieve high performance by pre-resolving column indices and constructors.
 */
public class DtoMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtoMapper.class);
    private static final Pattern FUNCTION_SQL_COLUMN_PATTERN = Pattern.compile(
            "\\b[a-zA-Z_]\\w*\\s*\\((?:\\s*\\b[a-zA-Z_]\\w*\\s*\\()*+\\s*(?:([a-zA-Z_]\\w*)\\.)?([a-zA-Z_]\\w*)",
            Pattern.CASE_INSENSITIVE
    );

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

    public <DTO> List<DTO> toDtos(final Class<DTO> dtoClass, final List<Row> rows) {
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        // Compile per-DTO mapping data; key is the table alias or table name (if no alias exists)
        final Map<String, MappingData> mappingDataMap = compileMappingData(dtoClass, rows);

        // Create DTOs and populate the cache
        final Map<Class<?>, List<PartiallyConstructedDto>> createdDtos = cacheDtos(rows, mappingDataMap);

        // Resolve inter-DTO dependencies
        resolveDependencies(createdDtos);

        // Return result
        final List<PartiallyConstructedDto> dtosOfType = createdDtos.get(dtoClass);

        if (dtosOfType == null) {
            return Collections.emptyList();
        }

        return dtosOfType.stream()
                .map(partialDto -> (DTO) partialDto.getDto())
                .toList();
    }

    private Map<String, MappingData> compileMappingData(final Class<?> dtoClass, final List<Row> rows) {
        final TableMetaData dtoClassTableMetaData = tableRegistry.getOrmTableOrThrow(dtoClass).getMetaData();
        final Map<String, MappingData> mappingDataMap = new HashMap<>();
        int columnIndex = 0;

        for (final Row.RowColumn rowColumn : rows.getFirst().columns()) {
            final Column column = rowColumn.column();
            final Table table;

            if (column.hasTable()) {
                table = column.table();
            } else {
                table = parseTargetColumn(column.name(), dtoClassTableMetaData.schema()).table();
            }

            final MappingData mappingData = createMappingDataIfAbsent(mappingDataMap, table);
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

                if (relatedDto) {
                    final OrmTable reverseMappingOrmTable = tableRegistry.getOrmTableOrThrow(fieldAccessor.type());
                    final List<FieldAccessor> oneToManyReverseMappings = mappingData.ormTable().getOneToManyReverseMappings();

                    if (oneToManyReverseMappings != null) {
                        relatedCollectionField = oneToManyReverseMappings.stream()
                                .filter(collectionField -> collectionField.dtoClass() == fieldAccessor.type())
                                .map(reverseMappingOrmTable::mappedFieldTargetForField)
                                .filter(MappedOneToMany.class::isInstance)
                                .map(MappedOneToMany.class::cast)
                                .filter(mappedOneToMany -> mappedOneToMany.mappedByField() == fieldAccessor)
                                .findFirst()
                                .map(MappedOneToMany::collection)
                                .orElse(null);
                    }
                }

                fieldMapping = new FieldMapping(fieldAccessor, new ArrayList<>(List.of(column)), new int[0], basicType, relatedDto, relatedCollectionField);
                mappingData.fieldMappings().add(fieldMapping);
            }

            // Mark index if its a primary key, for faster retrieval later
            int pkIndex = mappingData.ormTable().getPrimaryKeyFields().indexOf(fieldAccessor);

            if (pkIndex != -1) {
                mappingData.pkColumnIndexes()[pkIndex] = columnIndex;
            }

            columnIndex++;
        }

        // Finalize mapping data: sort columns for related DTOs, resolve all column indices, and pre-map constructor arguments
        for (final MappingData mappingData : mappingDataMap.values()) {
            final List<FieldMapping> finalizedMappings = new ArrayList<>();
            final Row firstRow = rows.getFirst();

            for (final FieldMapping fieldMapping : mappingData.fieldMappings()) {
                if (fieldMapping.isRelatedDto() && fieldMapping.columns().size() > 1) {
                    sortColumnsForRelatedDto(fieldMapping, mappingData.ormTable());
                }

                final int[] columnIndexes = new int[fieldMapping.columns().size()];
                for (int i = 0; i < columnIndexes.length; i++) {
                    columnIndexes[i] = firstRow.getColumnIndex(fieldMapping.columns().get(i));
                }

                finalizedMappings.add(new FieldMapping(
                        fieldMapping.fieldAccessor(),
                        fieldMapping.columns(),
                        columnIndexes,
                        fieldMapping.isBasicType(),
                        fieldMapping.isRelatedDto(),
                        fieldMapping.relatedCollectionField()
                ));
            }

            final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(mappingData.dtoClass());
            final int[] constructorArgIndices;

            if (!constructorMappingInfo.defaultConstructorUsed()) {
                final List<FieldAccessor> canonicalAccessors = constructorMappingInfo.canonicalConstructorFieldAccessors();
                constructorArgIndices = new int[canonicalAccessors.size()];
                for (int i = 0; i < constructorArgIndices.length; i++) {
                    final FieldAccessor accessor = canonicalAccessors.get(i);
                    int foundIndex = -1;
                    for (int j = 0; j < finalizedMappings.size(); j++) {
                        if (finalizedMappings.get(j).fieldAccessor().equals(accessor)) {
                            foundIndex = j;
                            break;
                        }
                    }
                    constructorArgIndices[i] = foundIndex;
                }
            } else {
                constructorArgIndices = new int[0];
            }

            mappingData.fieldMappings().clear();
            mappingData.fieldMappings().addAll(finalizedMappings);
            // Replace the record with one containing constructorArgIndices
            mappingDataMap.put(mappingData.table().alias() != null ? mappingData.table().alias() : mappingData.table().qualifiedName(),
                    new MappingData(mappingData.dtoClass(), mappingData.table(), mappingData.ormTable(), mappingData.pkColumnIndexes(), mappingData.fieldMappings(), constructorArgIndices));
        }

        return mappingDataMap;
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
            // Find the constraint that points to the target table
            final ForeignKeyConstraint constraint = columnMetaData.getForeignKeyConstraints().stream()
                    .filter(c -> {
                        final Table targetTable = c.foreignKey().table();
                        return targetOrmTable.getMetaData().name().equalsIgnoreCase(targetTable.name())
                                && (targetOrmTable.getMetaData().schema() == null || targetTable.schema() == null || targetOrmTable.getMetaData().schema().equalsIgnoreCase(targetTable.schema()));
                    })
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

    private MappingData createMappingDataIfAbsent(final Map<String, MappingData> mappingDataMap, final Table table) {
        final String key = table.alias() != null ? table.alias() : table.qualifiedName();
        return mappingDataMap.computeIfAbsent(key, alias -> {
            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
            final List<FieldAccessor> pkFields = ormTable.getPrimaryKeyFields();
            return new MappingData(ormTable.dtoClass(),
                    table,
                    ormTable,
                    new int[pkFields.size()],
                    new ArrayList<>(),
                    new int[0]);
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

    private Map<Class<?>, List<PartiallyConstructedDto>> cacheDtos(final List<Row> rows, final Map<String, MappingData> mappingDataMap) {
        final Map<Class<?>, List<PartiallyConstructedDto>> createdDtos = new HashMap<>();

        for (MappingData mappingData : mappingDataMap.values()) {
            for (final Row row : rows) {
                final PartiallyConstructedDto createdDto = cacheDto(mappingData, row);

                if (createdDto != null) {
                    final Class<?> createdDtoClass = createdDto.mappingData().dtoClass();
                    final List<PartiallyConstructedDto> dtosOfType = createdDtos.computeIfAbsent(createdDtoClass, cls -> new ArrayList<>());
                    dtosOfType.add(createdDto);

                    // Also map interfaces to the created DTO(s)
                    mappingData.ormTable().getDtoClassInterfaces()
                            .forEach(dtoClassInterface -> {
                                createdDtos.put(dtoClassInterface, dtosOfType);
                            });
                }
            }
        }

        return createdDtos;
    }

    private @Nullable PartiallyConstructedDto cacheDto(final MappingData mappingData, final Row row) {
        final Pk primaryKey = getPrimaryKey(mappingData, row);
        final PartiallyConstructedDto cachedDto = dtoCache.get(mappingData.dtoClass(), primaryKey);

        if (cachedDto != null) {
            return null;
        }

        final PartiallyConstructedDto dto = createDto(mappingData, primaryKey, row);
        dtoCache.put(mappingData.dtoClass(), primaryKey, dto);

        return dto;
    }

    private PartiallyConstructedDto createDto(final MappingData mappingData, final Pk primaryKey, final Row row) {
        final OrmTable ormTable = mappingData.ormTable();
        final Class<?> dtoClass = ormTable.dtoClass();
        final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(dtoClass);
        final Object dto;

        final List<FieldMapping> fieldMappings = mappingData.fieldMappings();
        final Object[] values = new Object[fieldMappings.size()];
        final List<RelatedDtoDependency> relatedDtoDependencies = new ArrayList<>();

        // Map DTO field values
        for (int i = 0; i < fieldMappings.size(); i++) {
            final FieldMapping fieldMapping = fieldMappings.get(i);
            final FieldAccessor fieldAccessor = fieldMapping.fieldAccessor();

            if (fieldMapping.isBasicType()) {
                // Basic field
                final Object dbValue = row.getValue(fieldMapping.columnIndexes()[0]);
                values[i] = typeConverter.convert(dbValue, fieldAccessor.type());
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
                relatedDtoDependencies.add(new RelatedDtoDependency(fieldAccessor, fieldAccessor.type(), pk, fieldMapping.relatedCollectionField()));
                values[i] = null;
            }
        }

        // Construct the DTO
        if (constructorMappingInfo.defaultConstructorUsed()) {
            try {
                dto = constructorMappingInfo.constructor().invoke();
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
            }

            for (int i = 0; i < fieldMappings.size(); i++) {
                final FieldMapping fieldMapping = fieldMappings.get(i);
                if (fieldMapping.isBasicType()) {
                    final FieldAccessor fieldAccessor = fieldMapping.fieldAccessor();
                    final Object value = values[i];

                    if (value == null && fieldAccessor.type().isPrimitive()) {
                        // Don't set primitives to null
                        continue;
                    }

                    fieldAccessor.set(dto, value);
                }
            }
        } else {
            final int[] argIndices = mappingData.constructorArgIndices();
            final @Nullable Object[] args = new Object[argIndices.length];

            for (int i = 0; i < args.length; i++) {
                final int mappingIndex = argIndices[i];
                if (mappingIndex != -1) {
                    args[i] = values[mappingIndex];
                } else {
                    args[i] = ClassUtils.getDefaultValue(constructorMappingInfo.canonicalConstructorFieldAccessors().get(i).type());
                }
            }

            try {
                dto = constructorMappingInfo.constructor().invokeWithArguments(args);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
            }
        }

        return new PartiallyConstructedDto(dto, primaryKey, relatedDtoDependencies, mappingData);
    }

    private void resolveDependencies(final Map<Class<?>, List<PartiallyConstructedDto>> createdDtos) {
        final ClassFieldAccessorCache classFieldAccessorCache = litebridgeContext.classFieldAccessorCache();

        for (final List<PartiallyConstructedDto> partialDtosOfType : createdDtos.values()) {
            for (final PartiallyConstructedDto partialDto : partialDtosOfType) {
                final OrmTable ormTable = partialDto.mappingData().ormTable();
                Object dto = partialDto.getDto();
                final List<MappedManyToMany> mappedManyToManyList = ormTable.getManyToManyMappings();

                for (int i = 0; i < mappedManyToManyList.size(); i++) {
                    final MappedManyToMany mappedManyToMany = mappedManyToManyList.get(i);
                    final OrmTable targetOrmTable = mappedManyToMany.targetOrmTable().get();
                    final List<PartiallyConstructedDto> matchingCreatedDtos = createdDtos.get(targetOrmTable.dtoClass());

                    if (matchingCreatedDtos != null) {
                        // Many-to-many match to a created DTO type; create and populate the collection field
                        final FieldAccessor collectionFieldAccessor = mappedManyToMany.collection();
                        final Collection<Object> collection = (Collection<Object>) ClassUtils.newInstance(collectionFieldAccessor.type());
                        for (int j = 0; j < matchingCreatedDtos.size(); j++) {
                            collection.add(matchingCreatedDtos.get(j).getDto());
                        }
                        collectionFieldAccessor.set(dto, collection);
                    }
                }

                if (partialDto.dependencies().isEmpty()) {
                    continue;
                }

                final List<RelatedDtoDependency> dependencies = partialDto.dependencies();
                final List<ReverseCollectionUpdate> reverseUpdates = new ArrayList<>();
                Map<FieldAccessor, Object> recordUpdates = (dto instanceof Record) ? new HashMap<>() : null;

                for (int i = 0; i < dependencies.size(); i++) {
                    final RelatedDtoDependency dependency = dependencies.get(i);
                    // Inject the dependency target DTO into the host DTO
                    final Class<?> relatedDtoClass = dependency.relatedDtoClass();
                    final PartiallyConstructedDto targetDto = dtoCache.get(relatedDtoClass, dependency.primaryKeyValue());
                    final Object relatedDto;

                    if (targetDto == null) {
                        final RelatedDtoStrategy relatedDtoStrategy = litebridgeContext.getRelatedDtoStrategy();
                        LOGGER.trace("Could not find dependency for {} with primary key {}; using related DTO strategy: {}", partialDto.mappingData().dtoClass(), dependency.primaryKeyValue(), relatedDtoStrategy);

                        if (relatedDtoStrategy == RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN) {
                            // Create a DTO with just the primary key set
                            relatedDto = createDtoPrimaryKeyOnly(relatedDtoClass, dependency.primaryKeyValue());
                        } else {
                            relatedDto = null;
                        }
                    } else {
                        relatedDto = targetDto.getDto();
                    }

                    if (recordUpdates != null) {
                        recordUpdates.put(dependency.field(), relatedDto);
                    } else {
                        // Normal class
                        dependency.field().set(dto, relatedDto);
                    }

                    // Collect reverse collection update
                    if (relatedDto != null && dependency.relatedCollectionField() != null) {
                        reverseUpdates.add(new ReverseCollectionUpdate(relatedDto, dependency.relatedCollectionField()));
                    }
                }

                if (recordUpdates != null && !recordUpdates.isEmpty()) {
                    // Reconstruct Record once
                    final List<FieldAccessor> accessors = classFieldAccessorCache.fieldAccessors(dto.getClass());
                    final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues = new ArrayList<>(accessors.size());
                    for (int i = 0; i < accessors.size(); i++) {
                        final FieldAccessor accessor = accessors.get(i);
                        final Object value = recordUpdates.containsKey(accessor) ? recordUpdates.get(accessor) : accessor.get(dto);
                        fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(accessor, value));
                    }
                    dto = constructDto(dto.getClass(), fieldAccessorValues, dtoConstructor);
                    partialDto.setDto(dto);
                }

                // Finalize reverse collection updates with the final DTO instance
                for (int i = 0; i < reverseUpdates.size(); i++) {
                    final ReverseCollectionUpdate update = reverseUpdates.get(i);
                    updateReverseCollection(dto, update.relatedDto, update.relatedCollectionField);
                }
            }
        }
    }

    private void updateReverseCollection(final Object hostDto, final Object relatedDto, final FieldAccessor relatedCollectionField) {
        final Collection<Object> currentCollection;
        final Collection<Object> dtoCollection = (Collection<Object>) relatedCollectionField.get(relatedDto);

        if (dtoCollection != null) {
            currentCollection = dtoCollection;
        } else {
            currentCollection = (Collection<Object>) ClassUtils.newInstance(relatedCollectionField.type());
            relatedCollectionField.set(relatedDto, currentCollection);
        }

        currentCollection.add(hostDto);
    }

    private record ReverseCollectionUpdate(Object relatedDto, FieldAccessor relatedCollectionField) {
    }

    private Object createDtoPrimaryKeyOnly(final Class<?> dtoClass, final Pk primaryKey) {
        final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(dtoClass);
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
         * Map of DTO type -> map of primary key -> PartiallyConstructedDtos
         */
        private final Map<Class<?>, Map<Pk, PartiallyConstructedDto>> cache = new HashMap<>();

        public @Nullable PartiallyConstructedDto get(final Class<?> dtoClass, final Pk primaryKey) {
            final Map<Pk, PartiallyConstructedDto> dtoClassMap = cache.get(dtoClass);

            if (dtoClassMap == null) {
                return null;
            }

            return dtoClassMap.get(primaryKey);
        }

        public void put(final Class<?> dtoClass, final Pk primaryKey, final PartiallyConstructedDto partiallyConstructedDto) {
            cache.computeIfAbsent(dtoClass, cls -> new HashMap<>())
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

    private static final class PartiallyConstructedDto {
        private Object dto;
        private final Pk primaryKey;
        private final List<RelatedDtoDependency> dependencies;
        private final MappingData mappingData;

        private PartiallyConstructedDto(Object dto,
                                        Pk primaryKey,
                                        List<RelatedDtoDependency> dependencies,
                                        MappingData mappingData) {
            this.dto = dto;
            this.primaryKey = primaryKey;
            this.dependencies = dependencies;
            this.mappingData = mappingData;
        }

        public Object getDto() {
            return dto;
        }

        public void setDto(final Object dto) {
            this.dto = dto;
        }

        public Pk primaryKey() {
            return primaryKey;
        }

        public List<RelatedDtoDependency> dependencies() {
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
            return Objects.equals(this.dto, that.dto) &&
                    Objects.equals(this.primaryKey, that.primaryKey) &&
                    Objects.equals(this.dependencies, that.dependencies) &&
                    Objects.equals(this.mappingData, that.mappingData);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dto, primaryKey, dependencies, mappingData);
        }

        @Override
        public String toString() {
            return "PartiallyConstructedDto[" +
                    "dto=" + dto + ", " +
                    "primaryKey=" + primaryKey + ", " +
                    "dependencies=" + dependencies + ", " +
                    "mappingData=" + mappingData + ']';
        }

    }

    private record FieldMapping(FieldAccessor fieldAccessor,
                                List<Column> columns,
                                int[] columnIndexes,
                                boolean isBasicType,
                                boolean isRelatedDto,
                                @Nullable FieldAccessor relatedCollectionField) {
    }

    private record MappingData(Class<?> dtoClass,
                               Table table,
                               OrmTable ormTable,
                               int[] pkColumnIndexes,
                               List<FieldMapping> fieldMappings,
                               int[] constructorArgIndices) {
    }

    private record RelatedDtoDependency(FieldAccessor field,
                                        Class<?> relatedDtoClass,
                                        Pk primaryKeyValue,
                                        @Nullable FieldAccessor relatedCollectionField) {
    }
}
