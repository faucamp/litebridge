package org.litebridge.orm.persistence;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.TrackedDto;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NullMarked
public class Table {

    private TableMetaData metaData;
    private final Map<Field, ColumnMetaData> fieldColumnMap;
    private final Map<String, ColumnMetaData> columnMap;
    private final Map<String, ColumnMetaData> fieldNameColumnMap;
    private final Map<String, Field> columnNameFieldMap;
    private final ChangeTracker changeTracker;

    private final WeakRefSet<Object> persistedDtos = new WeakRefSet<>();

    public Table(final TableMetaData metaData, final Map<Field, ColumnMetaData> fieldColumnMap, final ChangeTracker changeTracker) {
        this.metaData = metaData;
        this.fieldColumnMap = fieldColumnMap;
        this.changeTracker = changeTracker;
        final Map<String, ColumnMetaData> columnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, ColumnMetaData> fieldNameColumnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, Field> columnNameFieldMap = new HashMap<>(fieldColumnMap.size());

        fieldColumnMap.forEach(((field, column) -> {
            columnMap.put(column.name(), column);
            fieldNameColumnMap.put(field.getName(), column);
            columnNameFieldMap.put(column.name(), field);
        }));

        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.fieldNameColumnMap = Collections.unmodifiableMap(fieldNameColumnMap);
        this.columnNameFieldMap = Collections.unmodifiableMap(columnNameFieldMap);
    }

    public TableMetaData getMetaData() {
        return metaData;
    }

    public Map<Field, ColumnMetaData> getFieldColumnMap() {
        return fieldColumnMap;
    }

    public ColumnMetaData getColumnForFieldName(final String fieldName) {
        return ObjectUtils.requireNonNull(fieldNameColumnMap.get(fieldName), "No column for field '" + fieldName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    public ColumnMetaData getColumn(final String columnName) {
        return ObjectUtils.requireNonNull(fieldNameColumnMap.get(columnName), "No column '" + columnName + "' in table '" + metaData.name() + "'");
    }

    public <DTO> @Nullable TrackedDto<DTO> getTrackedDto(final DTO dto) {
        return changeTracker.getTrackedDto(dto);
    }

    public void trackDto(final Object dto) {
        changeTracker.trackDtoFields(dto, fieldColumnMap.keySet());
    }

    public Field getFieldForColumnName(final String columnName) {
        return ObjectUtils.requireNonNull(columnNameFieldMap.get(columnName), "No field for column '" + columnName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    public void syncPersistedDto(final Object dto) {
        persistedDtos.add(dto);
        final TrackedDto<?> trackedDto = changeTracker.getTrackedDto(dto);
        trackedDto.snapshot(fieldColumnMap.keySet(), true);
    }

    public boolean isPersistedDto(final Object dto) {
        return persistedDtos.contains(dto);
    }
}
