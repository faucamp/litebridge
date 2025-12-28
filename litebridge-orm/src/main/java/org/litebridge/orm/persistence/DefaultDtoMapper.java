package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.tracking.FieldAccessor;

import java.util.Optional;

public final class DefaultDtoMapper implements DtoMapper {

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

    @SuppressWarnings("unchecked")
    public <DTO> @Nullable DTO toDto(final @Nullable Row row, final Class<DTO> dtoClass, final @Nullable DtoCache dtoCache) {
        if (row == null) {
            return null;
        }

        final Table table = tableRegistry.getTableOrThrow(dtoClass);

        // Extract the primary key and re-use already-created DTOs if possible
        final Object[] primaryKeyValues = table.getMetaData().primaryKey().stream()
                .map(row::column)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Row.RowColumn::value)
                .toArray(Object[]::new);

        final DTO cachedDto = primaryKeyValues.length > 0 ? dtoCache.get(dtoClass, primaryKeyValues) : null;

        if (cachedDto != null) {
            return cachedDto;
        }

        final DTO dto;
        try {
            dto = dtoClass.getDeclaredConstructor().newInstance();
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to instantiate DTO: " + dtoClass, ex);
        }

        // Filter results for this DTO
        final String tableAlias = dtoAliasRegistry.aliasOrNull(table.getMetaData());

        row.columnStream()
                .filter(rowColumn -> tableAlias == null || dtoAliasRegistry.belongsTo(tableAlias, rowColumn.column()))
                .forEach(rowColumn -> {
                    final FieldAccessor field = table.getFieldForColumnName(rowColumn.column().name());
                    final Object convertedValue;

                    if (ClassUtils.isBasicType(field.type())) {
                        convertedValue = typeConverter.convert(rowColumn.value(), field.type());
                    } else {
                        convertedValue = toDto(row, field.type(), dtoCache);
                    }

                    field.set(dto, convertedValue);
                });

        dtoCache.put(primaryKeyValues, dto);
        return dto;
    }
}
