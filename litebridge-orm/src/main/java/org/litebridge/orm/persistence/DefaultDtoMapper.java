package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.convert.TypeConverter;

import java.lang.reflect.Field;

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


    @Override
    public <DTO> @Nullable DTO toDto(final @Nullable Row row, final Class<DTO> dtoClass) {
        if (row == null) {
            return null;
        }

        final DTO dto;
        try {
            dto = dtoClass.getDeclaredConstructor().newInstance();
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to instantiate DTO: " + dtoClass, ex);
        }

        // Filter results for this DTO
        final Table table = tableRegistry.getTableOrThrow(dtoClass);
        final String tableAlias = dtoAliasRegistry.aliasOrNull(table.getMetaData());

        row.columnStream()
                .filter(rowColumn -> tableAlias == null || dtoAliasRegistry.belongsTo(tableAlias, rowColumn.column()))
                .forEach(rowColumn -> {
                    final Field field = table.getFieldForColumnName(rowColumn.column().name());
                    final Object convertedValue;

                    if (ClassUtils.isBasicType(field.getType())) {
                        convertedValue = typeConverter.convert(rowColumn.value(), field.getType());
                    } else {
                        convertedValue = toDto(row, field.getType());
                    }

                    try {
                        field.set(dto, convertedValue);
                    } catch (final IllegalAccessException ex) {
                        throw new IllegalStateException("Failed to set field '%s' of DTO: %s".formatted(field.getName(), dto), ex);
                    }
                });

        return dto;
    }
}
