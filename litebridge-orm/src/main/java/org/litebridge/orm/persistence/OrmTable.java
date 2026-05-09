package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.type.WeakIdentitySet;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.FieldAccessorChain;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A table known by/registered with the ORM, facilitating the relationship between Java objects (DTOs)
 * and database table schema.
 * <p>
 * This class maintains metadata and mappings between object field accessors and database columns.
 * It tracks changes made to objects and their associated database states for ORM operations.
 */
public class OrmTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrmTable.class);

    private final Class<?> dtoClass;
    private final TableMetaData metaData;
    private final Map<FieldAccessor, MappedFieldTarget> fieldTargetMap;
    private final List<Map.Entry<FieldAccessor, MappedFieldTarget>> fieldTargetEntries;
    private final Map<String, ColumnMetaData> columnMap;
    private final Map<String, ColumnMetaData> fieldNameColumnMap;
    private final Map<String, FieldAccessor> columnNameFieldMap;
    private final ChangeTracker changeTracker;
    private final WeakIdentitySet<Object> persistedDtos = new WeakIdentitySet<>();
    private final List<Class<?>> nestedDtoClasses;
    private final TableRegistry contextTableRegistry = new TableRegistry();
    private final ClassFieldAccessorCache classFieldAccessorCache;
    @Nullable
    private List<FieldAccessor> oneToManyReverseMappings;

    /**
     * Constructs a new {@code OrmTable} instance, initializing table metadata, field-to-column mappings,
     * and a change tracker for managing object state.
     *
     * @param dtoClass       the DTO class associated with the table
     * @param metaData       the metadata describing the table structure
     * @param fieldTargetMap a map associating field accessors with their corresponding column metadata
     * @param changeTracker  the change tracker to monitor and track modifications made to the table's data
     */
    public OrmTable(final Class<?> dtoClass,
                    final TableMetaData metaData,
                    final Map<FieldAccessor, MappedFieldTarget> fieldTargetMap,
                    final ChangeTracker changeTracker,
                    final ClassFieldAccessorCache classFieldAccessorCache) {
        this.dtoClass = dtoClass;
        this.metaData = metaData;
        this.classFieldAccessorCache = classFieldAccessorCache;

        this.changeTracker = changeTracker;
        final Map<String, ColumnMetaData> columnMap = new HashMap<>(fieldTargetMap.size());
        final Map<String, ColumnMetaData> fieldNameColumnMap = new HashMap<>(fieldTargetMap.size());
        final Map<String, FieldAccessor> columnNameFieldMap = new HashMap<>(fieldTargetMap.size());
        final Map<FieldAccessor, MappedFieldTarget> processedFieldTargetMap = new HashMap<>(fieldTargetMap.size());
        final List<Class<?>> nestedDtoClasses = new ArrayList<>();
        final List<Map.Entry<FieldAccessor, MappedFieldTarget>> orderedFieldTargetEntries = new ArrayList<>(fieldTargetMap.size());

        fieldTargetMap.forEach(((fieldAccessor, mappedFieldTarget) -> {
            final MappedFieldTarget preprocessedTarget;

            if (mappedFieldTarget instanceof ColumnAndInlineTable(ColumnMetaData column, OrmTable tableSpec)) {
                contextTableRegistry.addTable(fieldAccessor.type(), tableSpec);
                preprocessedTarget = column;
            } else {
                preprocessedTarget = mappedFieldTarget;
            }

            processedFieldTargetMap.put(fieldAccessor, preprocessedTarget);

            if (preprocessedTarget instanceof ColumnMetaData column) {
                columnMap.put(column.name(), column);
                columnNameFieldMap.put(column.name(), fieldAccessor);

                if (fieldAccessor instanceof FieldAccessorChain fieldAccessorChain) {
                    // Nested DTO structure - add chain information for the deserialiser
                    fieldAccessorChain.fieldAccessors().stream()
                            .filter(field -> field.dtoClass() != dtoClass)
                            .forEach(field -> nestedDtoClasses.add(field.dtoClass()));
                } else {
                    fieldNameColumnMap.put(fieldAccessor.name(), column);
                }
            }
        }));

        // Add mapped field-target entries in the order of the db columns
        this.metaData.columns().forEach(column -> {
            processedFieldTargetMap.entrySet().stream()
                    .filter(entry ->
                            entry.getValue() instanceof ColumnMetaData columnMetaData
                                    && columnMetaData.equals(column))
                    .findFirst()
                    .ifPresent(orderedFieldTargetEntries::add);
        });

        // Append remaining entries to the end of the list
        if (orderedFieldTargetEntries.size() < fieldTargetMap.size()) {
            fieldTargetMap.entrySet().stream()
                    .filter(entry -> !orderedFieldTargetEntries.contains(entry))
                    .forEach(orderedFieldTargetEntries::add);
        }

        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.fieldNameColumnMap = Collections.unmodifiableMap(fieldNameColumnMap);
        this.columnNameFieldMap = Collections.unmodifiableMap(columnNameFieldMap);
        this.fieldTargetMap = Collections.unmodifiableMap(processedFieldTargetMap);
        this.nestedDtoClasses = nestedDtoClasses.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(nestedDtoClasses);
        this.fieldTargetEntries = Collections.unmodifiableList(orderedFieldTargetEntries);
    }

    /**
     * Get the DTO class associated with the table.
     *
     * @return the DTO class associated with the table
     */
    public Class<?> dtoClass() {
        return dtoClass;
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
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(dtoClass, fieldName);

        if (fieldAccessor instanceof FieldAccessorChain fieldAccessorChain) {
            return Objects.requireNonNull(fieldNameColumnMap.get(fieldAccessorChain.fieldAccessors().getFirst().name()), "No parent column for field path '" + fieldAccessorChain.fieldPath() + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
        } else {
            return Objects.requireNonNull(fieldNameColumnMap.get(fieldName), "No column for field '" + fieldName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
        }
    }

    public List<Class<?>> getNestedDtoClasses() {
        return nestedDtoClasses;
    }

    /**
     * Get the column metadata for the specified column name.
     *
     * @param columnName the column name to retrieve the metadata for
     * @return the column metadata for the specified column name, or null if not found
     */
    public ColumnMetaData getColumn(final String columnName) {
        return ObjectUtils.requireNonNull(columnMap.get(columnName), () -> new IllegalArgumentException("No column '" + columnName + "' in table '" + metaData.name() + "'"));
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
        return ObjectUtils.requireNonNull(fieldForColumnNameOrNull(columnName), () -> new IllegalArgumentException("No field for column '" + columnName + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'"));
    }

    public @Nullable FieldAccessor fieldForColumnNameOrNull(final String columnName) {
        return columnNameFieldMap.get(columnName);
    }

    public Stream<FieldAccessor> fieldAcessorStream() {
        return fieldTargetMap.keySet().stream();
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

    public final List<MappedManyToMany> getManyToManyMappings() {
        return fieldTargetMap.values().stream()
                .filter(MappedManyToMany.class::isInstance)
                .map(MappedManyToMany.class::cast)
                .toList();
    }

    public Optional<MappedOneToMany> getOneToManyMappingForField(final FieldAccessor field) {
        final MappedFieldTarget mappedFieldTarget = fieldTargetMap.get(field);

        if (mappedFieldTarget instanceof MappedOneToMany mappedOneToMany) {
            return Optional.of(mappedOneToMany);
        } else {
            return Optional.empty();
        }
    }

    public Optional<MappedManyToMany> getManyToManyMappingForField(final FieldAccessor field) {
        final MappedFieldTarget mappedFieldTarget = fieldTargetMap.get(field);

        if (mappedFieldTarget instanceof MappedManyToMany mappedManyToMany) {
            return Optional.of(mappedManyToMany);
        } else {
            return Optional.empty();
        }
    }

    public List<Map.Entry<FieldAccessor, MappedFieldTarget>> mappedFieldTargets() {
        return fieldTargetEntries;
    }

    public TableRegistry getContextTableRegistry() {
        return contextTableRegistry;
    }

    public void addOneToManyReverseMapping(final FieldAccessor fieldAccessor) {
        if (oneToManyReverseMappings == null) {
            oneToManyReverseMappings = new ArrayList<>();
        }

        oneToManyReverseMappings.add(fieldAccessor);
    }

    public @Nullable List<FieldAccessor> getOneToManyReverseMappings() {
        return oneToManyReverseMappings;
    }
}
