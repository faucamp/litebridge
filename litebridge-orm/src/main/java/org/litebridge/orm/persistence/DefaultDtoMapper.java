package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.convert.TypeConverter;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DefaultDtoMapper<DTO> implements DtoMapper<DTO> {

    private final Class<DTO> dtoClass;
    private final Table table;
    private final TypeConverter typeConverter;

    public DefaultDtoMapper(final Class<DTO> dtoClass, final Table table, final TypeConverter typeConverter) {
        this.dtoClass = dtoClass;
        this.table = table;
        this.typeConverter = typeConverter;
    }

    @Override
    public @Nullable DTO toDto(final @Nullable LinkedHashMap<String, Object> row) {
        return toDto(row, dtoClass, table, typeConverter);
    }

    private static <DTO> @Nullable DTO toDto(@Nullable final Map<String, Object> row, final Class<DTO> dtoClass, final Table table, final TypeConverter typeConverter) {
        if (row == null) {
            return null;
        }

        final DTO dto;
        try {
            dto = dtoClass.getDeclaredConstructor().newInstance();
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to instantiate DTO: " + dtoClass, ex);
        }

        for (final String column : row.keySet()) {
            final Field field = table.getFieldForColumnName(column);
            final Object convertedValue;

            if (ClassUtils.isBasicType(field.getType())) {
                convertedValue = typeConverter.convert(row.get(column), field.getType());
            } else {
                // Dealing with an embedded DTO
                throw new UnsupportedOperationException("Embedded DTOs are not supported yet");
            }

            try {
                field.set(dto, convertedValue);
            } catch (final IllegalAccessException ex) {
                throw new IllegalStateException("Failed to set field '%s' of DTO: %s".formatted(field.getName(), dto), ex);
            }
        }

        return dto;
    }
}
