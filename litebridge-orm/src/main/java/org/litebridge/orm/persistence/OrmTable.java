package org.litebridge.orm.persistence;

import org.jspecify.annotations.NullMarked;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorChain;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A table known by/registered with the ORM, facilitating the relationship between Java objects (DTOs)
 * and database table schema.
 * <p>
 * This class maintains metadata and mappings between object field accessors and database columns.
 * It tracks changes made to objects and their associated database states for ORM operations.
 */
@NullMarked
public class OrmTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrmTable.class);

    private TableMetaData metaData;
    private final Map<FieldAccessor, ColumnMetaData> fieldColumnMap;
    private final Map<String, ColumnMetaData> columnMap;
    private final Map<String, ColumnMetaData> fieldNameColumnMap;
    private final Map<String, FieldAccessorChainLink> fieldAccessorChainLinkMap;
    private final Map<String, FieldAccessor> columnNameFieldMap;
    private final ChangeTracker changeTracker;

    private final WeakRefSet<Object> persistedDtos = new WeakRefSet<>();

    public OrmTable(final TableMetaData metaData, final Map<FieldAccessor, ColumnMetaData> fieldColumnMap, final ChangeTracker changeTracker) {
        this.metaData = metaData;
        this.fieldColumnMap = fieldColumnMap;
        this.changeTracker = changeTracker;
        final Map<String, ColumnMetaData> columnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, ColumnMetaData> fieldNameColumnMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, FieldAccessor> columnNameFieldMap = new HashMap<>(fieldColumnMap.size());
        final Map<String, FieldAccessorChainLink> fieldAccessorChainLinkMap = new HashMap<>();

        fieldColumnMap.forEach(((fieldAccessor, column) -> {
            columnMap.put(column.name(), column);
            columnNameFieldMap.put(column.name(), fieldAccessor);

            if (fieldAccessor instanceof FieldAccessorChain fieldAccessorChain) {
                // Nested DTO structure - add chain information for the deserialiser
                final FieldAccessor firstFieldAccessor = fieldAccessorChain.fieldAccessors().getFirst();
                final FieldAccessorChainLink fieldAccessorChainLink = fieldAccessorChainLinkMap.computeIfAbsent(firstFieldAccessor.name(), k -> new FieldAccessorChainLink());
                fieldAccessorChainLink.add(fieldAccessorChain);
            } else {
                fieldNameColumnMap.put(fieldAccessor.name(), column);
            }
        }));

        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.fieldNameColumnMap = Collections.unmodifiableMap(fieldNameColumnMap);
        this.columnNameFieldMap = Collections.unmodifiableMap(columnNameFieldMap);
        this.fieldAccessorChainLinkMap = Collections.unmodifiableMap(fieldAccessorChainLinkMap);
    }

    public TableMetaData getMetaData() {
        return metaData;
    }

    public ColumnMetaData getColumnForFieldName(final String fieldName) {
        return ObjectUtils.requireNonNull(fieldNameColumnMap.get(fieldName), "No column for field '" + fieldName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    public boolean hasColumnForFieldName(final String fieldName) {
        return fieldNameColumnMap.containsKey(fieldName);
    }

    public boolean hasFieldAccessorChainForFieldName(final String fieldName) {
        return fieldAccessorChainLinkMap.containsKey(fieldName);
    }

    public ColumnMetaData getColumn(final String columnName) {
        return ObjectUtils.requireNonNull(columnMap.get(columnName), "No column '" + columnName + "' in table '" + metaData.name() + "'");
    }

    public <DTO> TrackedDto<DTO> getTrackedDto(final DTO dto) {
        return changeTracker.getTrackedDto(dto);
    }

    public <DTO> TrackedDto<DTO> ensureTrackedDto(final DTO dto) {
        final TrackedDto<DTO> trackedDto = changeTracker.getTrackedDtoOrNull(dto);

        if (trackedDto == null) {
            return changeTracker.getTrackedDto(changeTracker.trackDtoFields(dto, fieldColumnMap.keySet(), true));
        } else {
            return trackedDto;
        }
    }

    public void trackDto(final Object dto) {
        changeTracker.trackDtoFields(dto, fieldColumnMap.keySet());
    }

    public FieldAccessor getFieldForColumnName(final String columnName) {
        return ObjectUtils.requireNonNull(columnNameFieldMap.get(columnName), "No field for column '" + columnName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    public void syncPersistedDto(final Object dto) {
        if (!persistedDtos.contains(dto)) {
            LOGGER.trace("Adding persisted DTO: {}", dto);
            persistedDtos.add(dto);
        }


        final TrackedDto<?> trackedDto = changeTracker.getTrackedDto(dto);
        LOGGER.trace("Creating new snapshot for DTO: {}", dto);
        trackedDto.snapshot(true);
    }

    public boolean isPersistedDto(final Object dto) {
        return persistedDtos.contains(dto);
    }
}
