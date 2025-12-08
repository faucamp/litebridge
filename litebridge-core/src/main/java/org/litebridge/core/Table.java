package org.litebridge.core;

import jakarta.annotation.Nullable;
import org.litebridge.core.dto.TrackedDto;
import org.litebridge.db.api.Column;
import org.litebridge.db.api.TableMetaData;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class Table {

    private TableMetaData metaData;
    private final Map<Field, Column> fieldColumnMap;
    private final Map<String, Column> fieldNameColumnMap;
    private final Map<String, Field> columnNameFieldMap;
    private final Map<Object, TrackedDto> trackedDtos = Collections.synchronizedMap(new WeakHashMap<>());
    private final WeakRefSet<Object> persistedDtos = new WeakRefSet<>();

    public Table(TableMetaData metaData, Map<Field, Column> fieldColumnMap) {
        this.metaData = metaData;
        this.fieldColumnMap = fieldColumnMap;
        this.fieldNameColumnMap = new HashMap<String, Column>(fieldColumnMap.size());
        this.columnNameFieldMap = new HashMap<>(fieldColumnMap.size());

        fieldColumnMap.forEach(((field, column) -> {
            fieldNameColumnMap.put(field.getName(), column);
            columnNameFieldMap.put(column.name(), field);
        }));
    }

    public TableMetaData getMetaData() {
        return metaData;
    }

    public Map<Field, Column> getFieldColumnMap() {
        return fieldColumnMap;
    }

    public Column getColumnForFieldName(final String fieldName) {
        return fieldNameColumnMap.get(fieldName);
    }

    public @Nullable TrackedDto getTrackedDto(Object dto) {
        return trackedDtos.get(dto);
    }

    public TrackedDto trackDto(Object dto) {
        final TrackedDto trackedDto = new TrackedDto();
        trackedDto.snapshot(dto, fieldColumnMap.keySet(), false);
        trackedDtos.put(dto, trackedDto);
        return trackedDto;
    }

    public @Nullable Field getFieldForColumnName(final String columnName) {
        return columnNameFieldMap.get(columnName);
    }

    public void syncPersistedDto(final Object dto) {
        persistedDtos.add(dto);
        trackedDtos.get(dto).snapshot(dto, fieldColumnMap.keySet(), true);
    }

    public boolean isPersistedDto(final Object dto) {
        return persistedDtos.contains(dto);
    }
}
