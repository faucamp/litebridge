package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.dto.DtoJoinSpec;
import org.litebridge.orm.api.dto.DtoSelectSpec;
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
import java.util.Set;
import java.util.stream.Stream;

public class SelectSpecDtoMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectSpecDtoMapper.class);
    private final PartiallyConstructedDtoCache dtoCache;
    private final TypeConverter typeConverter;
    private final DtoSelectSpec selectSpec;


    public SelectSpecDtoMapper(final DtoSelectSpec dtoSelectSpec,
                               final TypeConverter typeConverter) {
        this.dtoCache = new PartiallyConstructedDtoCache();
        this.typeConverter = typeConverter;
        this.selectSpec = dtoSelectSpec;
    }

    public <DTO> List<DTO> toDtos(final Class<DTO> dtoClass, final List<Row> rows) {
        final List<DtoBlueprint> blueprints = createDtoBlueprints(rows);

        return blueprints.stream()
                .map(this::toDto)
                .map(dtoClass::cast)
                .toList();
    }

    private Object toDto(final DtoBlueprint blueprint) {
        // Construct the primary DTO
        final PartiallyConstructedDto partialDto = toDto(blueprint.dtoData());

        // Construct related DTOs, caching them
        blueprint.joinedDtoData().forEach(this::toDto);

        // Resolve primary DTO dependencies and return the constructed DTO
        final Set<PartiallyConstructedDto> resolvedDtos = Collections.newSetFromMap(new IdentityHashMap<>());
        final Object dto = resolveRelatedDtoDependencies(partialDto, resolvedDtos);

        // Ensure any other constructed DTOs in the caches are also resolved (setting bidirectional relationships)
        dtoCache.stream()
                .filter(partiallyConstructedDto -> !resolvedDtos.contains(partiallyConstructedDto))
                .forEach(relatedDto -> resolveRelatedDtoDependencies(relatedDto, resolvedDtos));

        return dto;
    }

    private Object resolveRelatedDtoDependencies(final PartiallyConstructedDto partialDto, Set<PartiallyConstructedDto> resolvedDtos) {
        if (resolvedDtos.contains(partialDto)) {
            return partialDto.dto();
        }

        for (final DtoDependency dependency : partialDto.dependencies()) {
            final PartiallyConstructedDto relatedDto = dtoCache.get(dependency.targetDtoClass(), dependency.targetPrimaryKey());

            if (relatedDto != null) {
                dependency.field().set(partialDto.dto(), resolveRelatedDtoDependencies(relatedDto, resolvedDtos));
            } else {
                LOGGER.debug("Unresolved dependency for field '{}' in DTO class '{}' with target key: {}: no matching DTO found", dependency.field().name(), partialDto.dto().getClass(), dependency.targetPrimaryKey());
            }
        }

        updateOneToManyCollectionMappings(partialDto);
        resolvedDtos.add(partialDto);
        return partialDto.dto();
    }

    private PartiallyConstructedDto toDto(final DtoBlueprint.SelectDtoData dtoData) {
        return toDto(dtoData, dtoData.spec().getFieldColumns());
    }

    private PartiallyConstructedDto toDto(final DtoBlueprint.JoinDtoData dtoData) {
        return toDto(dtoData, dtoData.spec().getFieldColumns());
    }

    private PartiallyConstructedDto toDto(final DtoBlueprint.DtoData<?> dtoData, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        return toDto(dtoData.dtoClass(), dtoData.spec().dtoTable(), dtoData, fieldColumns);
    }

    private PartiallyConstructedDto toDto(final Class<?> dtoClass, final OrmTable table, final DtoBlueprint.DtoData<?> dtoData, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        final PartiallyConstructedDto cachedDto = dtoData.primaryKey().isEmpty() ? null : dtoCache.get(dtoClass, dtoData.primaryKey());

        if (cachedDto != null) {
            return cachedDto;
        }

        final PartiallyConstructedDto partialDto = createDto(dtoClass, table, dtoData, fieldColumns);
        dtoCache.put(dtoData.primaryKey(), partialDto);
        return partialDto;
    }

    @SuppressWarnings("unchecked")
    private PartiallyConstructedDto createDto(final Class<?> dtoClass, final OrmTable table, final DtoBlueprint.DtoData<?> dtoData, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        final List<Object> primaryKey = dtoData.primaryKey();
        final Row row = dtoData.row();
        final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues = new ArrayList<>(row.size());
        final List<DtoDependency> dependencies = new ArrayList<>();

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

            if (field == null || field.dtoClass() != dtoClass) {
                return;
            }

            if (sameTableNestedDto) {
                // Nested DTO built up from the same table
                //TODO: may need to filter the field columns; perhaps during pre-processing (which would require explicit support for this scenario, kinda lke here)
                final Object nestedDto = toDto(field.type(), table, dtoData, fieldColumns).dto();
                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(field, nestedDto));
            } else if (ClassUtils.isBasicType(field.type())) {
                // Standard column: find the value, convert it to target DTO's field type, and set the field
                final Row.RowColumn rowColumn = row.columnForAlias(fieldColumn.column().alias())
                        .orElseThrow(() -> new IllegalStateException("No column found for alias '%s' in row: %s".formatted(fieldColumn.column().alias(), row)));
                final Object convertedValue = typeConverter.convert(rowColumn.value(), field.type());
                fieldAccessorValues.add(new DtoConstructor.FieldAccessorValue(field, convertedValue));
            } else {
                // Related DTO: note dependency and allow outer process populate these
                final Row.RowColumn rowColumn = row.columnForAlias(fieldColumn.column().alias())
                        .orElseThrow(() -> new IllegalStateException("No column found for alias '%s' in row: %s".formatted(fieldColumn.column().alias(), row)));
                final Object targetPkValue = rowColumn.value();
                final List<Object> targetPk = targetPkValue instanceof List<?> ? (List<Object>) targetPkValue : Collections.singletonList(targetPkValue);
                dependencies.add(new DtoDependency(field, field.type(), targetPk));
            }
        });

        final DtoConstructor.ConstructionResult<?> constructionResult = DtoConstructor.newInstance(dtoClass, fieldAccessorValues);
        final Object dto = constructionResult.dto();

        if (constructionResult.defaultConstructorUsed()) {
            // Set the fields via field accessors since the default constructor was used
            fieldAccessorValues.forEach(fieldAccessorValue -> fieldAccessorValue.field().set(dto, fieldAccessorValue.value()));
        }

        return new PartiallyConstructedDto(constructionResult.dto(), table, dependencies);
    }

    private List<DtoBlueprint> createDtoBlueprints(final List<Row> rows) {
        final OrmTable table = selectSpec.dtoTable();

        // Group rows by the DTO table's primary key value for DTO assembly
        final List<ColumnMetaData> pkColumns = table.getMetaData().primaryKey();
        final Map<List<Object>, List<Row>> dtoPkGroupedRows = new LinkedHashMap<>();

        for (final Row row : rows) {
            final List<Object> pkValues = pkColumns.stream()
                    .map(pkColumn -> row.column(pkColumn.name())
                            .orElseThrow(() -> new IllegalStateException("No primary key column found for table '%s' in row: %s".formatted(table.getMetaData().name(), row)))
                            .value())
                    .toList();

            dtoPkGroupedRows.computeIfAbsent(pkValues, k -> new ArrayList<>())
                    .add(row);
        }

        // Construct the DTO blueprint by grouping other DTO data contained within each primary key-grouped list of rows
        final boolean hasJoins = !CollectionUtils.isEmpty(selectSpec.getJoins());
        final List<DtoBlueprint> blueprints = new ArrayList<>(dtoPkGroupedRows.size());

        for (final Map.Entry<List<Object>, List<Row>> entry : dtoPkGroupedRows.entrySet()) {
            final List<Row> rowGroup = entry.getValue();
            final DtoBlueprint blueprint = new DtoBlueprint(selectSpec, entry.getKey(), rowGroup.getFirst());

            if (hasJoins) {
                selectSpec.getJoins().stream()
                        .map(DtoJoinSpec.class::cast)
                        .forEach(dtoJoinSpec -> {
                            final List<ColumnMetaData> joinPkColumns = dtoJoinSpec.dtoTable().getMetaData().primaryKey();
                            final Map<List<Object>, Row> relatedDtoRows = new LinkedHashMap<>();

                            for (final Row row : rowGroup) {
                                final List<Object> joinPkValues = joinPkColumns.stream()
                                        .map(pkColumn -> row.column(pkColumn.name())
                                                .orElseThrow(() -> new IllegalStateException("No primary key column found for join table '%s' in row: %s".formatted(table.getMetaData().name(), row)))
                                                .value())
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

    private void updateOneToManyCollectionMappings(final PartiallyConstructedDto partialDto) {
        final Object dto = partialDto.dto();
        final OrmTable table = partialDto.dtoTable();

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
                //TODO: handle immutable collections
                currentCollection = dtoCollection;
            } else {
                currentCollection = (Collection<Object>) ClassUtils.newInstance(collection.type());
                collection.set(dto, currentCollection);
            }

            final Class<?> targetClass = collection.genericType();
            dtoCache.stream(targetClass).forEach(currentCollection::add);
        });
    }

    private record PartiallyConstructedDto(Object dto, OrmTable dtoTable, List<DtoDependency> dependencies) {
    }

    private record DtoDependency(FieldAccessor field, Class<?> targetDtoClass, List<Object> targetPrimaryKey) {
    }

    private static final class PartiallyConstructedDtoCache {
        private final Map<Class<?>, Map<List<Object>, SelectSpecDtoMapper.PartiallyConstructedDto>> cache = new IdentityHashMap<>();

        public @Nullable PartiallyConstructedDto get(final Class<?> dtoClass, final List<Object> id) {
            return cache.computeIfAbsent(dtoClass, cls -> new HashMap<>())
                    .get(id);
        }

        public void put(final List<Object> id, final SelectSpecDtoMapper.PartiallyConstructedDto dto) {
            cache.computeIfAbsent(dto.dto().getClass(), cls -> new HashMap<>())
                    .put(id, dto);
        }

        public Stream<PartiallyConstructedDto> stream() {
            return cache.values().stream()
                    .flatMap(pkPcDto -> pkPcDto.values().stream());
        }

        public <DTO> Stream<DTO> stream(final Class<DTO> dtoClass) {
            Map<List<Object>, SelectSpecDtoMapper.PartiallyConstructedDto> dtoCache = cache.get(dtoClass);

            if (dtoCache != null) {
                return dtoCache.values().stream()
                        .map(PartiallyConstructedDto::dto)
                        .map(dtoClass::cast);
            } else {
                return Stream.empty();
            }
        }
    }
}
