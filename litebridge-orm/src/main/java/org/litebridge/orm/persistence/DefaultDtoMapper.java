package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Result;
import org.litebridge.orm.api.dto.DtoRows;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorChain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Default implementation of the {@link DtoMapper} interface. This class provides mechanisms
 * to map database rows to Data Transfer Object (DTO) instances using metadata from the ORM framework.
 * <p>
 * The mapping process includes handling nested DTO relationships, type conversion between database
 * values and DTO properties, and support for DTO caching to improve performance and maintain referential integrity.
 */
public final class DefaultDtoMapper implements DtoMapper {

    private static final String NO_ALIAS = "<NO ALIAS>";
    private final TableRegistry tableRegistry;
    private final TypeConverter typeConverter;
    private final DtoAliasRegistry dtoAliasRegistry;

    public DefaultDtoMapper(final TableRegistry tableRegistry,
                            final TypeConverter typeConverter,
                            final DtoAliasRegistry dtoAliasRegistry) {
        this.tableRegistry = tableRegistry;
        this.typeConverter = typeConverter;
        this.dtoAliasRegistry = dtoAliasRegistry;
    }

    @Override
    public <DTO, R extends Result> @Nullable DTO toDto(final @Nullable R result, final Class<DTO> dtoClass, final @Nullable DtoCache dtoCache) {
        final DTO dto;

        if (result instanceof DtoRows dtoRows) {
            dto = toDtos(dtoRows, dtoClass, dtoCache, new TableAliasIndexer()).getFirst();
        } else if (result instanceof Row row) {
            dto = toDto(row, dtoClass, dtoCache, new TableAliasIndexer());
        } else {
            throw new IllegalArgumentException("Unsupported result type: " + result.getClass());
        }

        // Populate reverse mappings for one-to-many relationships
        populateReverseOneToManyMappings(dto, dtoCache);

        return dto;
    }

    private <DTO> @Nullable List<DTO> toDtos(final @Nullable DtoRows dtoRows,
                                             final Class<DTO> dtoClass,
                                             final DtoCache dtoCache,
                                             final TableAliasIndexer tableAliasIndexer) {
        if (dtoRows == null) {
            return null;
        }

        // Map the requested DTOs (will be picked up from cache)
        final List<DTO> result = dtoRows.rows(dtoClass).stream()
                .map(row -> toDto(row, dtoClass, dtoCache, tableAliasIndexer))
                .toList();

        // Map rows to other/related DTOs (caching them in the process, making them usable for populating one-to-many mappings)
        dtoRows.streamOmit(dtoClass)
                .forEach(dtoClassRows -> dtoClassRows.rows()
                        .forEach(row -> toDto(row, dtoClassRows.dtoClass(), dtoCache, tableAliasIndexer)));

        return result;
    }


    @SuppressWarnings("unchecked")
    private <DTO> @Nullable DTO toDto(final Row row,
                                      final Class<DTO> dtoClass,
                                      final DtoCache dtoCache,
                                      final TableAliasIndexer tableAliasIndexer) {

        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);

        // Extract the primary key and re-use already-created DTOs if possible
        final List<Object> primaryKeyValues = table.getMetaData().primaryKey().stream()
                .map(ColumnMetaData::name)
                .map(row::column)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Row.RowColumn::value)
                .toList();

        final DTO cachedDto = !primaryKeyValues.isEmpty() ? dtoCache.get(dtoClass, primaryKeyValues) : null;

        if (cachedDto != null) {
            return cachedDto;
        }

        // Filter results for this DTO
        final String tableAlias;

        if (dtoAliasRegistry.isEmpty()) {
            tableAlias = NO_ALIAS;
        } else {
            tableAlias = dtoAliasRegistry.aliasOrNull(table.getMetaData(), tableAliasIndexer.nextIndex(table.getMetaData()));

            if (tableAlias == null) {
                // Aliases for other DTOs exist, but not for this one, so results will not contain any data for this DTO
                return null;
            }
        }

        // Create a new DTO instance
        final DTO dto = createDto(dtoClass, row, table, tableAlias, dtoCache, new HashSet<>(), tableAliasIndexer);
        dtoCache.put(primaryKeyValues, dto);
        return dto;
    }

    private <DTO> DTO createDto(final Class<DTO> dtoClass,
                                final Row row,
                                final OrmTable table,
                                final String tableAlias,
                                final @Nullable DtoCache dtoCache,
                                final Set<Row.RowColumn> mappedColumns,
                                final TableAliasIndexer tableAliasIndexer) {
        final DTO dto;

        try {
            dto = ClassUtils.newInstance(dtoClass);
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to instantiate DTO: " + dtoClass, ex);
        }

        // Map results to DTO
        row.columnStream()
                .filter(rowColumn -> !mappedColumns.contains(rowColumn))
                .filter(rowColumn -> NO_ALIAS.equals(tableAlias) || dtoAliasRegistry.belongsTo(tableAlias, rowColumn.column()))
                .forEach(rowColumn -> {
                    final FieldAccessor tableField = table.getFieldForColumnName(rowColumn.column().name());
                    final FieldAccessor field;
                    final boolean sameTableNestedDto;

                    if (tableField instanceof FieldAccessorChain fieldAccessorChain) {
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
                        field = tableField;
                        sameTableNestedDto = false;
                    }

                    if (field == null) {
                        return;
                    }

                    final Object convertedValue;

                    if (sameTableNestedDto) {
                        // Nested DTO built up from the same table
                        convertedValue = createDto(field.type(), row, table, tableAlias, dtoCache, mappedColumns, tableAliasIndexer);
                    } else {
                        if (!ClassFieldAccessorCache.fieldAccessors(dtoClass).contains(field)) {
                            // Skip fields that are not mapped by the DTO
                            return;
                        }

                        mappedColumns.add(rowColumn);

                        if (ClassUtils.isBasicType(field.type())) {
                            convertedValue = typeConverter.convert(rowColumn.value(), field.type());
                        } else {
                            convertedValue = toDto(row, field.type(), dtoCache, tableAliasIndexer);
                        }
                    }

                    field.set(dto, convertedValue);
                });

        return dto;
    }

    private <DTO> void populateReverseOneToManyMappings(@Nullable final DTO dto, @Nullable final DtoCache dtoCache) {
        if (dto == null || dtoCache == null) {
            return;
        }

        final Class<DTO> dtoClass = (Class<DTO>) dto.getClass();
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);

        ClassFieldAccessorCache.fieldAccessors(dtoClass).forEach(field -> {
            if (ClassFieldAccessorCache.isNestedDtoField(dtoClass, field)) {
                // Populate reverse mappings for nested DTOs
                populateReverseOneToManyMappings(field.get(dto), dtoCache);
            } else if (Collection.class.isAssignableFrom(field.type()) && table.hasOneToManyMapping(field)) {
                final MappedOneToMany mappedOneToMany = table.getOneToManyMappingForField(field);
                //TODO: support for multiple relationships to the same DTO type
                final List<?> reverseMappingCollection = dtoCache.getAll(mappedOneToMany.mappedByField().dtoClass());
                mappedOneToMany.collection().set(dto, reverseMappingCollection);
            }
        });
    }

    private static final class TableAliasIndexer {
        private final Map<Table, Integer> tableAliasCurrentIndex = new HashMap<>();

        public int nextIndex(final Table table) {
            return tableAliasCurrentIndex.merge(table, 0, (key, value) -> value + 1);
        }

        public int currentIndex(final Table table) {
            return tableAliasCurrentIndex.getOrDefault(table, 0);
        }
    }
}
