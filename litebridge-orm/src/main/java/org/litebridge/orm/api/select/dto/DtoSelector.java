package org.litebridge.orm.api.select.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.SelectField;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.persistence.Table;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class DtoSelector<DTO> extends AbstractSelector<DTO> {

    private final Class<DTO> dtoClass;
    private final Table table;

    public DtoSelector(final Class<DTO> dtoClass, final Table table, final DatabaseProvider databaseProvider) {
        super(databaseProvider);
        this.dtoClass = dtoClass;
        this.table = table;
    }

    public DtoFromClauseTerminal<DTO> select(final String... fields) {
        return select(Arrays.stream(fields).map(SelectField::new));
    }

    public DtoFromClauseTerminal<DTO> select(final SelectField... fields) {
        return select(Arrays.stream(fields));
    }

    private DtoFromClauseTerminal<DTO> select(final Stream<SelectField> fields) {
        selectSpec.setColumns(fields.map(selectField -> {
                    // Map the input DTO field names to database column names
                    final Column column = table.getColumnForFieldName(selectField.name());
                    return new SelectField(column.getName(), selectField.alias());
                })
                .toList());
        return new DtoFromClauseTerminal<>(selectSpec, this);
    }

    @Override
    protected final @Nullable DTO toDto(final @Nullable Map<String, Object> row) {
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
                convertedValue = databaseProvider.getTypeConverter().convert(row.get(column), field.getType());
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
