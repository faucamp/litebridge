package org.litebridge.orm.persistence;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.api.convert.TypeConverter;
import org.litebridge.orm.Table;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Selector<T> {

    private final Class<T> dtoClass;
    private final Table table;
    private final DatabaseProvider databaseProvider;
    private final List<org.litebridge.db.api.query.Condition> conditions = new ArrayList<>();

    public Selector(final Class<T> dtoClass, final Table table, final DatabaseProvider databaseProvider) {
        this.dtoClass = dtoClass;
        this.table = table;
        this.databaseProvider = databaseProvider;
    }

    public Condition<T> where(final String field) {
        final String column = table.getColumnForFieldName(field).getName();
        return new Condition<>(column, new SelectorStack());
    }

    private T get() {
        final List<Map<String, Object>> resultList = executeQuery();

        // Map result set to DTO
        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return mapToDto(resultList.getFirst(), databaseProvider.getTypeConverter());
    }

    public List<T> getAll() {
        return stream().toList();
    }

    public Stream<T> stream() {
        final List<Map<String, Object>> resultList = executeQuery();
        return resultList.stream()
                .map(row -> mapToDto(row, databaseProvider.getTypeConverter()));
    }

    private List<Map<String, Object>> executeQuery() {
        // Execute SQL query
        final List<String> columns = List.copyOf(table.getMetaData().getColumns().keySet());
        final List<Map<String, Object>> resultList;

        try {
            resultList = databaseProvider.select(table.getMetaData(), columns, conditions);
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        return resultList;
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
            field.setAccessible(true);
            final Object convertedValue = typeConverter.convert(row.get(column), field.getType());

            try {
                field.set(dto, convertedValue);
            } catch (final IllegalAccessException ex) {
                throw new IllegalStateException("Failed to set field '%s' of DTO: %s".formatted(field.getName(), dto), ex);
            }
        }

        return dto;
    }

    public final class SelectorStack {

        public Condition<T> where(final String field) {
            return Selector.this.where(field);
        }

        public void push(final Condition<T> condition) {
            conditions.add(condition);
        }

        public T get() {
            return Selector.this.get();
        }

        public List<T> getAll() {
            return Selector.this.getAll();
        }

        public Stream<T> stream() {
            return Selector.this.stream();
        }
    }

}
