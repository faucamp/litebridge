package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.convert.TypeConverter;
import org.litebridge.orm.Table;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The Selector class provides a mechanism to retrieve data from a database table
 * and map it to Data Transfer Objects (DTOs). It performs selection operations
 * with optional filtering conditions and supports retrieving results as individual
 * objects, collections, or streams. This class is generic and operates on the
 * specified DTO class type.
 *
 * @param <T> The type of DTO that this selector will operate on.
 */
public final class DtoSelector<T> extends AbstractSelector<T> {

    private final Class<T> dtoClass;
    private final Table table;

    public DtoSelector(final Class<T> dtoClass, final Table table, final DatabaseProvider databaseProvider) {
        super(List.copyOf(table.getMetaData().getColumns().keySet()), table.getMetaData(), databaseProvider);
        this.dtoClass = dtoClass;
        this.table = table;
    }

    /**
     * Creates a new query condition based on the specified field name.
     * This method retrieves the corresponding column name for the given field name
     * and initializes a new {@link Condition} instance which can be used to build query conditions.
     *
     * @param field The name of the DTO field for which the condition is being created.
     * @return A new {@link Condition} instance representing the condition on the specified field.
     * @throws IllegalArgumentException if there is no column mapped to the given field name in the table.
     */
    @Override
    public Condition<T> where(final String field) {
        final String column = table.getColumnForFieldName(field).getName();
        return super.where(column);
    }

    @Override
    public Selector<T> orderBy(final String field) {
        final String column = table.getColumnForFieldName(field).getName();
        return super.orderBy(column);
    }

    @Override
    public @Nullable T oneOrNull() {
        return mapToDto(super.getOneRecord(false), databaseProvider.getTypeConverter());
    }

    @Override
    public @Nullable T firstOrNull() {
        return mapToDto(super.getOneRecord(true), databaseProvider.getTypeConverter());
    }

    /**
     * Eagerly retrieves all matching DTOs from the query result as a list.
     *
     * @return an unmodifiable list of DTOs matching the query conditions
     */
    @Override
    public List<T> list() {
        return stream().toList();
    }

    /**
     * Provides a sequential stream of DTOs derived from the query results.
     *
     * @return a {@link Stream} of DTOs corresponding to the query results mapped from the database.
     * @throws IllegalStateException if the query execution or DTO mapping fails
     */
    @Override
    public Stream<T> stream() {
        return super.streamRecords()
                .map(row -> mapToDto(row, databaseProvider.getTypeConverter()));
    }

    private T mapToDto(final Map<String, Object> row, final TypeConverter typeConverter) {
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
