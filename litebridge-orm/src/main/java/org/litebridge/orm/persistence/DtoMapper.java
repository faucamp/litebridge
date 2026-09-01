package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
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
import java.util.List;
import java.util.Map;
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
    private MappingPlan mappingPlan;

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

        // Compile per-DTO mapping data
        final Map<Table, MappingData> mappingDataMap = compileMappingData(dtoClass, rows);

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
                .map(partialDto -> (DTO) partialDto.dto())
                .toList();
    }

    private Map<Table, MappingData> compileMappingData(final Class<?> dtoClass, final List<Row> rows) {
        final TableMetaData dtoClassTableMetaData = tableRegistry.getOrmTableOrThrow(dtoClass).getMetaData();
        final Map<Table, MappingData> mappingDataMap = new HashMap<>();
        int columnIndex = 0;

        for (final Row.RowColumn rowColumn : rows.getFirst().columns()) {
            final Column column = rowColumn.column();
            final Column targetColumn;

            if (column.hasTable()) {
                targetColumn = column;
            } else {
                targetColumn = parseTargetColumn(column.name(), dtoClassTableMetaData.schema());
            }

            final MappingData mappingData = createMappingDataIfAbsent(mappingDataMap, targetColumn.table());
            final FieldAccessor fieldAccessor = mappingData.ormTable().getFieldForColumnName(targetColumn.name());
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

            mappingData.fieldMappings().add(new FieldMapping(fieldAccessor, column, basicType, relatedDto, relatedCollectionField));

            // Mark index if its a primary key, for faster retrieval later
            int pkIndex = mappingData.ormTable().getPrimaryKeyFields().indexOf(fieldAccessor);

            if (pkIndex != -1) {
                mappingData.pkColumnIndexes()[pkIndex] = columnIndex;
            }

            columnIndex++;
        }

        return mappingDataMap;
    }

    private MappingData createMappingDataIfAbsent(final Map<Table, MappingData> mappingDataMap, final Table table) {
        return mappingDataMap.computeIfAbsent(table, tbl -> {
            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(tbl);
            final List<FieldAccessor> pkFields = ormTable.getPrimaryKeyFields();
            return new MappingData(ormTable.dtoClass(),
                    tbl,
                    ormTable,
                    new int[pkFields.size()],
                    new ArrayList<>());
        });
    }

    private static List<@Nullable Object> getPrimaryKey(final MappingData mappingData, final Row row) {
        return Arrays.stream(mappingData.pkColumnIndexes())
                .mapToObj(row::column)
                .map(Row.RowColumn::value)
                .toList();
    }

    private Map<Class<?>, List<PartiallyConstructedDto>> cacheDtos(final List<Row> rows, final Map<Table, MappingData> mappingDataMap) {
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
        final List<@Nullable Object> primaryKey = getPrimaryKey(mappingData, row);
        final PartiallyConstructedDto cachedDto = dtoCache.get(mappingData.dtoClass(), primaryKey);

        if (cachedDto != null) {
            return null;
        }

        final PartiallyConstructedDto dto = createDto(mappingData, primaryKey, row);
        dtoCache.put(mappingData.dtoClass(), primaryKey, dto);

        return dto;
    }

    private PartiallyConstructedDto createDto(final MappingData mappingData, final List<@Nullable Object> primaryKey, final Row row) {
        final OrmTable ormTable = mappingData.ormTable();
        final Class<?> dtoClass = ormTable.dtoClass();
        final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(dtoClass);
        final Object dto;

        final Map<FieldAccessor, @Nullable Object> valuesByField = new HashMap<>();
        final List<RelatedDtoDependency> relatedDtoDependencies = new ArrayList<>();

        // Map DTO field values
        for (FieldMapping fieldMapping : mappingData.fieldMappings()) {
            final FieldAccessor fieldAccessor = fieldMapping.fieldAccessor();
            final Object dbValue = row.column(fieldMapping.column()).orElseThrow().value();

            if (fieldMapping.isBasicType()) {
                // Basic field
                final Object convertedValue = typeConverter.convert(dbValue, fieldAccessor.type());
                valuesByField.put(fieldAccessor, convertedValue);
            } else {
                // Related DTO
                //TODO: composite PK support - remove Collections.singletonList() and base it on the target DTO's primary keys
                final RelatedDtoDependency relatedDtoDependency = new RelatedDtoDependency(fieldAccessor, fieldAccessor.type(), Collections.singletonList(dbValue), fieldMapping.relatedCollectionField());
                relatedDtoDependencies.add(relatedDtoDependency);
            }
        }

        // Construct the DTO
        if (constructorMappingInfo.defaultConstructorUsed()) {
            try {
                dto = constructorMappingInfo.constructor().invoke();
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to construct DTO: " + dtoClass, e);
            }

            mappingData.fieldMappings().stream()
                    .map(FieldMapping::fieldAccessor)
                    .forEach(fieldAccessor -> {
                        final Object value = valuesByField.get(fieldAccessor);

                        if (value == null && fieldAccessor.type().isPrimitive()) {
                            // Don't set primitives to null
                            return;
                        }

                        fieldAccessor.set(dto, valuesByField.get(fieldAccessor));
                    });
        } else {
            final @Nullable Object[] args = new Object[constructorMappingInfo.canonicalConstructorFieldAccessors().size()];

            for (int i = 0; i < args.length; i++) {
                args[i] = valuesByField.get(constructorMappingInfo.canonicalConstructorFieldAccessors().get(i));
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
        for (List<PartiallyConstructedDto> partialDtosOfType : createdDtos.values()) {
            for (PartiallyConstructedDto partialDto : partialDtosOfType) {
                final OrmTable ormTable = partialDto.mappingData().ormTable();
                final Object dto = partialDto.dto();
                final List<MappedManyToMany> mappedManyToManyList = ormTable.getManyToManyMappings();

                for (MappedManyToMany mappedManyToMany : mappedManyToManyList) {
                    final OrmTable targetOrmTable = mappedManyToMany.targetOrmTable().get();
                    final List<PartiallyConstructedDto> matchingCreatedDtos = createdDtos.get(targetOrmTable.dtoClass());

                    if (matchingCreatedDtos != null) {
                        // Many-to-many match to a created DTO type; create and populate the collection field
                        final FieldAccessor collectionFieldAccessor = mappedManyToMany.collection();
                        final Collection<Object> collection = (Collection<Object>) ClassUtils.newInstance(collectionFieldAccessor.type());
                        matchingCreatedDtos.stream()
                                .map(PartiallyConstructedDto::dto)
                                .forEach(collection::add);
                        collectionFieldAccessor.set(dto, collection);
                    }
                }

                if (partialDto.dependencies().isEmpty()) {
                    continue;
                }

                for (RelatedDtoDependency dependency : partialDto.dependencies()) {
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
                        relatedDto = targetDto.dto();
                    }

                    dependency.field().set(dto, relatedDto);

                    // Update the reverse collection, if any
                    if (relatedDto != null && dependency.relatedCollectionField() != null) {
                        final FieldAccessor relatedCollectionField = dependency.relatedCollectionField();
                        final Collection<Object> currentCollection;
                        final Collection<Object> dtoCollection = (Collection<Object>) relatedCollectionField.get(relatedDto);

                        if (dtoCollection != null) {
                            currentCollection = dtoCollection;
                        } else {
                            currentCollection = (Collection<Object>) ClassUtils.newInstance(relatedCollectionField.type());
                            relatedCollectionField.set(relatedDto, currentCollection);
                        }

                        currentCollection.add(dto);
                    }
                }
            }
        }
    }

    private Object createDtoPrimaryKeyOnly(final Class<?> dtoClass, final List<Object> primaryKey) {
        final DtoConstructor.MappingInfo constructorMappingInfo = dtoConstructor.getMappingInfo(dtoClass);

        if (!constructorMappingInfo.defaultConstructorUsed()) {
            throw new IllegalStateException("Cannot construct partial object without default constructor: " + dtoClass);
        }

        final Object dto;

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
         * Map of DTO type -> map of primary key values -> PartiallyConstructedDtos
         */
        private final Map<Class<?>, Map<List<@Nullable Object>, PartiallyConstructedDto>> cache = new HashMap<>();

        public @Nullable PartiallyConstructedDto get(final Class<?> dtoClass, final List<@Nullable Object> primaryKey) {
            final Map<List<@Nullable Object>, PartiallyConstructedDto> dtoClassMap = cache.get(dtoClass);

            if (dtoClassMap == null) {
                return null;
            }

            return dtoClassMap.get(primaryKey);
        }

        public void put(final Class<?> dtoClass, final List<@Nullable Object> primaryKey, final PartiallyConstructedDto partiallyConstructedDto) {
            cache.computeIfAbsent(dtoClass, cls -> new HashMap<>())
                    .put(primaryKey, partiallyConstructedDto);
        }
    }

    private record PartiallyConstructedDto(Object dto,
                                           List<@Nullable Object> primaryKey,
                                           List<RelatedDtoDependency> dependencies,
                                           MappingData mappingData) {
    }

    private record FieldMapping(FieldAccessor fieldAccessor,
                                Column column,
                                boolean isBasicType,
                                boolean isRelatedDto,
                                @Nullable FieldAccessor relatedCollectionField) {
    }

    private record MappingData(Class<?> dtoClass,
                               Table table,
                               OrmTable ormTable,
                               int[] pkColumnIndexes,
                               List<FieldMapping> fieldMappings) {
    }

    private record RelatedDtoDependency(FieldAccessor field,
                                        Class<?> relatedDtoClass,
                                        List<Object> primaryKeyValue,
                                        @Nullable FieldAccessor relatedCollectionField) {
    }
}
