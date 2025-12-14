package org.litebridge.orm.persistence;

import org.litebridge.db.api.convert.TypeConverter;

import java.lang.reflect.Field;
import java.util.Map;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static <T> T mapToDto(final Map<String, Object> row, final Class<T> dtoClass, final Table table, final TypeConverter typeConverter) {
        if (row == null) {
            return null;
        }

        final T dto;
        try {
            dto = dtoClass.getDeclaredConstructor().newInstance();
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to instantiate DTO: " + dtoClass, ex);
        }

        for (final String column : row.keySet()) {
            final Field field = table.getFieldForColumnName(column);
            final Object convertedValue = typeConverter.convert(row.get(column), field.getType());

            try {
                field.set(dto, convertedValue);
            } catch (final IllegalAccessException ex) {
                throw new IllegalStateException("Failed to set field '%s' of DTO: %s".formatted(field.getName(), dto), ex);
            }
        }

        return dto;
    }
}
