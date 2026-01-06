package org.litebridge.orm.persistence;

import org.jspecify.annotations.NullMarked;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.TrackedDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NullMarked
public class Table {

    private TableMetaData metaData;
    private final Map<FieldAccessor, ColumnMetaData> fieldColumnMap;
    private final Map<String, ColumnMetaData> columnMap;
    private final Map<String, ColumnMetaData> fieldNameColumnMap;
    private final Map<String, FieldAccessor> columnNameFieldMap;
    private final ChangeTracker changeTracker;

    private final WeakRefSet<Object> persistedDtos = new WeakRefSet<>();

    public Table(final TableMetaData metaData, final Map<FieldAccessor, ColumnMetaData> fieldColumnMap, final ChangeTracker changeTracker) {
        this.metaData = metaData;
        this.fieldColumnMap = fieldColumnMap;
        this.changeTracker = changeTracker;
        final Map<String, ColumnMetaData> columnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, ColumnMetaData> fieldNameColumnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, FieldAccessor> columnNameFieldMap = new HashMap<>(fieldColumnMap.size());

        fieldColumnMap.forEach(((field, column) -> {
            columnMap.put(column.name(), column);
            fieldNameColumnMap.put(field.name(), column);
            columnNameFieldMap.put(column.name(), field);
        }));

        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.fieldNameColumnMap = Collections.unmodifiableMap(fieldNameColumnMap);
        this.columnNameFieldMap = Collections.unmodifiableMap(columnNameFieldMap);
    }

    public TableMetaData getMetaData() {
        return metaData;
    }

    public Map<FieldAccessor, ColumnMetaData> getFieldColumnMap() {
        return fieldColumnMap;
    }

    public ColumnMetaData getColumnForFieldName(final String fieldName) {
        return ObjectUtils.requireNonNull(fieldNameColumnMap.get(fieldName), "No column for field '" + fieldName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    public ColumnMetaData getColumn(final String columnName) {
        return ObjectUtils.requireNonNull(columnMap.get(columnName), "No column '" + columnName + "' in table '" + metaData.name() + "'");
    }

    public <DTO> TrackedDto<DTO> getTrackedDto(final DTO dto) {
        return changeTracker.getTrackedDto(dto);
    }

    public void trackDto(final Object dto) {
        changeTracker.trackDtoFields(dto, fieldColumnMap.keySet());
    }

    public FieldAccessor getFieldForColumnName(final String columnName) {
        return ObjectUtils.requireNonNull(columnNameFieldMap.get(columnName), "No field for column '" + columnName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    public void syncPersistedDto(final Object dto) {
        if (!persistedDtos.contains(dto)) {
            persistedDtos.add(dto);
        }

        final TrackedDto<?> trackedDto = changeTracker.getTrackedDto(dto);
        trackedDto.snapshot(true);
    }

    public boolean isPersistedDto(final Object dto) {
        return persistedDtos.contains(dto);
    }
}
