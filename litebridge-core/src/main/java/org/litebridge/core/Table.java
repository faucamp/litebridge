package org.litebridge.core;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.litebridge.tracking.TrackedDto;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.db.api.Column;
import org.litebridge.db.api.TableMetaData;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Table {

    private TableMetaData metaData;
    private final Map<Field, Column> fieldColumnMap;
    private final Map<String, Column> columnMap;
    private final Map<String, Column> fieldNameColumnMap;
    private final Map<String, Field> columnNameFieldMap;
    private final ChangeTracker changeTracker;

    private final WeakRefSet<Object> persistedDtos = new WeakRefSet<>();

    public Table(final TableMetaData metaData, final Map<Field, Column> fieldColumnMap, final ChangeTracker changeTracker) {
        this.metaData = metaData;
        this.fieldColumnMap = fieldColumnMap;
        this.changeTracker = changeTracker;
        final Map<String, Column> columnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, Column> fieldNameColumnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, Field> columnNameFieldMap = new HashMap<>(fieldColumnMap.size());

        fieldColumnMap.forEach(((field, column) -> {
            columnMap.put(column.getName(), column);
            fieldNameColumnMap.put(field.getName(), column);
            columnNameFieldMap.put(column.getName(), field);
        }));

        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.fieldNameColumnMap = Collections.unmodifiableMap(fieldNameColumnMap);
        this.columnNameFieldMap = Collections.unmodifiableMap(columnNameFieldMap);
    }

    public TableMetaData getMetaData() {
        return metaData;
    }

    public Map<Field, Column> getFieldColumnMap() {
        return fieldColumnMap;
    }

    public @Nonnull Column getColumnForFieldName(final String fieldName) {
        return Objects.requireNonNull(fieldNameColumnMap.get(fieldName), "No column for field '" + fieldName + "' in table '" + metaData.getTable() + "'");
    }

    public @Nonnull Column getColumn(final String columnName) {
        return Objects.requireNonNull(fieldNameColumnMap.get(columnName), "No column '" + columnName + "' in table '" + metaData.getTable() + "'");
    }

    public @Nullable TrackedDto getTrackedDto(final Object dto) {
        return changeTracker.getTrackedDto(dto);
    }

    public void trackDto(final Object dto) {
        changeTracker.trackDtoFields(dto, fieldColumnMap.keySet());
    }

    public @Nullable Field getFieldForColumnName(final String columnName) {
        return columnNameFieldMap.get(columnName);
    }

    public void syncPersistedDto(final Object dto) {
        persistedDtos.add(dto);
        final TrackedDto trackedDto = changeTracker.getTrackedDto(dto);

        if (trackedDto != null) {
            trackedDto.snapshot(dto, fieldColumnMap.keySet(), true);
        }
    }

    public boolean isPersistedDto(final Object dto) {
        return persistedDtos.contains(dto);
    }
}
