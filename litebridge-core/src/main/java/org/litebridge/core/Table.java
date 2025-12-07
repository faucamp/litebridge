package org.litebridge.core;

import jakarta.annotation.Nullable;
import org.litebridge.core.dto.TrackedDto;
import org.litebridge.db.api.Column;
import org.litebridge.db.api.TableMetaData;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class Table {

    private TableMetaData metaData;
    private Map<Field, Column> fieldColumnMap;
    private final Map<Object, TrackedDto> trackedDtos = Collections.synchronizedMap(new WeakHashMap<>());

    public Table(TableMetaData metaData, Map<Field, Column> fieldColumnMap) {
        this.metaData = metaData;
        this.fieldColumnMap = fieldColumnMap;
    }

    public TableMetaData getMetaData() {
        return metaData;
    }

    public Map<Field, Column> getFieldColumnMap() {
        return fieldColumnMap;
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
}
