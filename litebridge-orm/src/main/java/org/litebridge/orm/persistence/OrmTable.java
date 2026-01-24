package org.litebridge.orm.persistence;

import org.jspecify.annotations.NullMarked;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.type.WeakIdentitySet;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorChain;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private final Map<FieldAccessor, MappedFieldTarget> fieldTargetMap;
    private final Map<String, ColumnMetaData> columnMap;
    private final Map<String, ColumnMetaData> fieldNameColumnMap;
    private final Map<String, FieldAccessorChainLink> fieldAccessorChainLinkMap;
    private final Map<String, FieldAccessor> columnNameFieldMap;
    private final ChangeTracker changeTracker;
    private final WeakIdentitySet<Object> persistedDtos = new WeakIdentitySet<>();

    /**
     * Constructs a new {@code OrmTable} instance, initializing table metadata, field-to-column mappings,
     * and a change tracker for managing object state.
     *
     * @param metaData       the metadata describing the table structure
     * @param fieldTargetMap a map associating field accessors with their corresponding column metadata
     * @param changeTracker  the change tracker to monitor and track modifications made to the table's data
     */
    public OrmTable(final TableMetaData metaData, final Map<FieldAccessor, MappedFieldTarget> fieldTargetMap, final ChangeTracker changeTracker) {
        this.metaData = metaData;
        this.fieldTargetMap = fieldTargetMap;
        this.changeTracker = changeTracker;
        final Map<String, ColumnMetaData> columnMap = new HashMap<>(fieldTargetMap.size());
        final Map<String, ColumnMetaData> fieldNameColumnMap = new HashMap<>(fieldTargetMap.size());
        final Map<String, FieldAccessor> columnNameFieldMap = new HashMap<>(fieldTargetMap.size());
        final Map<String, FieldAccessorChainLink> fieldAccessorChainLinkMap = new HashMap<>();

        fieldTargetMap.forEach(((fieldAccessor, mappedFieldTarget) -> {
            if (mappedFieldTarget instanceof ColumnMetaData column) {
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
            }
        }));

        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.fieldNameColumnMap = Collections.unmodifiableMap(fieldNameColumnMap);
        this.columnNameFieldMap = Collections.unmodifiableMap(columnNameFieldMap);
        this.fieldAccessorChainLinkMap = Collections.unmodifiableMap(fieldAccessorChainLinkMap);
    }

    /**
     * Get the table metadata.
     *
     * @return the table metadata
     */
    public TableMetaData getMetaData() {
        return metaData;
    }

    /**
     * Get the column metadata for the specified field name.
     *
     * @param fieldName the field name to retrieve the column metadata for
     * @return the column metadata for the specified field name, or null if not found
     */
    public ColumnMetaData getColumnForFieldName(final String fieldName) {
        return ObjectUtils.requireNonNull(fieldNameColumnMap.get(fieldName), "No column for field '" + fieldName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    /**
     * Check if the table has a column for the specified field name.
     *
     * @param fieldName the field name to check
     * @return true if the table has a column for the specified field name, false otherwise
     */
    public boolean hasColumnForFieldName(final String fieldName) {
        return fieldNameColumnMap.containsKey(fieldName);
    }

    /**
     * Check if the table has a field accessor chain for the specified field name.
     *
     * @param fieldName the field name to check
     * @return true if the table has a field accessor chain for the specified field name, false otherwise
     */
    public boolean hasFieldAccessorChainForFieldName(final String fieldName) {
        return fieldAccessorChainLinkMap.containsKey(fieldName);
    }

    /**
     * Get the column metadata for the specified column name.
     *
     * @param columnName the column name to retrieve the metadata for
     * @return the column metadata for the specified column name, or null if not found
     */
    public ColumnMetaData getColumn(final String columnName) {
        return ObjectUtils.requireNonNull(columnMap.get(columnName), "No column '" + columnName + "' in table '" + metaData.name() + "'");
    }

    /**
     * Get the tracked version of the specified DTO.
     *
     * @param dto   the DTO to retrieve the tracked version for
     * @param <DTO> the type of the DTO
     * @return the tracked version of the specified DTO
     * @throws IllegalArgumentException if the specified DTO is not tracked
     */
    public <DTO> TrackedDto<DTO> getTrackedDto(final DTO dto) {
        return changeTracker.getTrackedDto(dto);
    }

    /**
     * Get the tracked version of the specified DTO, tracking its fields if not already tracked.
     *
     * @param dto   the DTO to retrieve the tracked version for
     * @param <DTO> the type of the DTO
     * @return the tracked version of the specified DTO
     */
    public <DTO> TrackedDto<DTO> ensureTrackedDto(final DTO dto) {
        final TrackedDto<DTO> trackedDto = changeTracker.getTrackedDtoOrNull(dto);

        if (trackedDto == null) {
            return changeTracker.getTrackedDto(changeTracker.trackDtoFields(dto, fieldTargetMap.keySet(), true));
        } else {
            return trackedDto;
        }
    }

    /**
     * Track the specified DTO's fields.
     *
     * @param dto the DTO to track
     */
    public void trackDto(final Object dto) {
        changeTracker.trackDtoFields(dto, fieldTargetMap.keySet());
    }

    /**
     * Get the field accessor for the specified column name.
     *
     * @param columnName the column name to retrieve the field accessor for
     * @return the field accessor for the specified column name, or null if not found
     * @throws IllegalArgumentException if the specified column name is null or empty
     */
    public FieldAccessor getFieldForColumnName(final String columnName) {
        return ObjectUtils.requireNonNull(columnNameFieldMap.get(columnName), "No field for column '" + columnName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    /**
     * Mark the specified DTO as persisted, creating a snapshot for tracking changes.
     *
     * @param dto the DTO to mark as persisted
     * @throws IllegalArgumentException if the specified DTO is null
     */
    public void syncPersistedDto(final Object dto) {
        if (!persistedDtos.contains(dto)) {
            LOGGER.trace("Adding persisted DTO: {}", dto);
            persistedDtos.add(dto);
        }

        final TrackedDto<?> trackedDto = changeTracker.getTrackedDto(dto);
        LOGGER.trace("Creating new snapshot for DTO: {}", dto);
        trackedDto.snapshot(true);
    }

    /**
     * Check if the specified DTO is persisted.
     *
     * @param dto the DTO to check
     * @return true if the specified DTO is persisted, false otherwise
     * @throws IllegalArgumentException if the specified DTO is null
     */
    public boolean isPersistedDto(final Object dto) {
        return persistedDtos.contains(dto);
    }

    public final List<MappedOneToMany> getOneToManyMappings() {
        return fieldTargetMap.values().stream()
                .filter(MappedOneToMany.class::isInstance)
                .map(MappedOneToMany.class::cast)
                .toList();
    }

    public boolean hasOneToManyMapping(final FieldAccessor field) {
        return fieldTargetMap.containsKey(field) && fieldTargetMap.get(field) instanceof MappedOneToMany;
    }

    public MappedOneToMany getOneToManyMappingForFiel(final FieldAccessor field) {
        final MappedFieldTarget mappedFieldTarget = fieldTargetMap.get(field);

        if (mappedFieldTarget instanceof MappedOneToMany mappedOneToMany) {
            return mappedOneToMany;
        } else {
            throw new IllegalArgumentException("Field '" + field.name() + "' is not a mapped one-to-many relationship.");
        }
    }
}
