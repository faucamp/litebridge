package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A table known by/registered with the ORM, facilitating the relationship between Java objects (DTOs)
 * and database table schema.
 * <p>
 * This class maintains metadata and mappings between object field accessors and database expressions.
 * It tracks changes made to objects and their associated database states for ORM operations.
 */
public class OrmTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrmTable.class);

    private final Class<?> dtoClass;
    private final TableMetaData metaData;
    private final Map<FieldAccessor, MappedFieldTarget> fieldAccessorTargetMap;
    private final Map<String, MappedFieldTarget> fieldNameTargetMap;
    private final List<Map.Entry<FieldAccessor, MappedFieldTarget>> fieldTargetEntries;
    private final Map<String, ColumnMetaData> columnMap;
    private final Map<String, ColumnMetaData> fieldNameColumnMap;
    private final Map<String, FieldAccessor> columnNameFieldMap;
    private final ChangeTracker changeTracker;
    private final WeakIdentitySet<Object> persistedDtos = new WeakIdentitySet<>();
    private final List<Class<?>> nestedDtoClasses;
    private final TableRegistry contextTableRegistry = new TableRegistry();
    private final ClassFieldAccessorCache classFieldAccessorCache;
    final boolean manyToManyJoinTable;
    private @Nullable List<FieldAccessor> oneToManyReverseMappings;
    private Set<Class<?>> dtoClassInterfaces = Collections.emptySet();
    private Set<Class<?>> relatedDtoClasses = new HashSet<>();

    /**
     * Constructs a new {@code OrmTable} instance, initializing table metadata, field-to-column mappings,
     * and a change tracker for managing object state.
     *
     * @param dtoClass                the DTO class associated with the table
     * @param metaData                the metadata describing the table structure
     * @param fieldAccessorTargetMap  a map associating field accessors with their corresponding column metadata
     * @param changeTracker           the change tracker to monitor and track modifications made to the table's data
     * @param classFieldAccessorCache the cache for field accessors
     */
    public OrmTable(final Class<?> dtoClass,
                    final TableMetaData metaData,
                    final Map<FieldAccessor, MappedFieldTarget> fieldAccessorTargetMap,
                    final ChangeTracker changeTracker,
                    final ClassFieldAccessorCache classFieldAccessorCache) {
        this.dtoClass = dtoClass;
        this.manyToManyJoinTable = Proxy.isProxyClass(dtoClass);
        this.metaData = metaData;
        this.classFieldAccessorCache = classFieldAccessorCache;

        this.changeTracker = changeTracker;
        final Map<String, ColumnMetaData> columnMap = new HashMap<>(fieldAccessorTargetMap.size());
        final Map<String, ColumnMetaData> fieldNameColumnMap = new HashMap<>(fieldAccessorTargetMap.size());
        final Map<String, FieldAccessor> columnNameFieldMap = new HashMap<>(fieldAccessorTargetMap.size());
        final Map<FieldAccessor, MappedFieldTarget> processedFieldTargetMap = new HashMap<>(fieldAccessorTargetMap.size());
        final Map<String, MappedFieldTarget> fieldNameTargetMap = new HashMap<>(fieldAccessorTargetMap.size());
        final List<Class<?>> nestedDtoClasses = new ArrayList<>();
        final List<Map.Entry<FieldAccessor, MappedFieldTarget>> orderedFieldTargetEntries = new ArrayList<>(fieldAccessorTargetMap.size());

        fieldAccessorTargetMap.forEach(((fieldAccessor, mappedFieldTarget) -> {
            final MappedFieldTarget preprocessedTarget;

            if (mappedFieldTarget instanceof ColumnAndInlineTable(ColumnMetaData column, OrmTable tableSpec)) {
                contextTableRegistry.addTable(fieldAccessor.type(), tableSpec);
                preprocessedTarget = column;
            } else {
                preprocessedTarget = mappedFieldTarget;
            }

            processedFieldTargetMap.put(fieldAccessor, preprocessedTarget);
            final String fieldName = (fieldAccessor instanceof FieldAccessorChain chain) ? chain.fieldPath() : fieldAccessor.name();
            fieldNameTargetMap.put(fieldName, preprocessedTarget);

            if (preprocessedTarget instanceof ColumnMetaData column) {
                columnMap.put(column.name(), column);
                columnNameFieldMap.put(column.name(), fieldAccessor);

                if (fieldAccessor instanceof FieldAccessorChain fieldAccessorChain) {
                    // Nested DTO structure - add chain information for the deserialiser
                    fieldAccessorChain.fieldAccessors().stream()
                            .filter(field -> field.dtoClass() != dtoClass)
                            .forEach(field -> nestedDtoClasses.add(field.dtoClass()));
                }

                fieldNameColumnMap.put(fieldName, column);

                if (!(fieldAccessor instanceof FieldAccessorChain) && !ClassUtils.isBasicType(fieldAccessor.type())) {
                    // Related DTO - mark for partial creation later if necessary (e.g. when no JOINs are specified)
                    relatedDtoClasses.add(fieldAccessor.type());
                }
            }
        }));

        // Add mapped field-target entries in the order of the db expressions
        this.metaData.columns().forEach(column -> {
            processedFieldTargetMap.entrySet().stream()
                    .filter(entry ->
                            entry.getValue() instanceof ColumnMetaData columnMetaData
                                    && columnMetaData.equals(column))
                    .findFirst()
                    .ifPresent(orderedFieldTargetEntries::add);
        });

        // Append remaining entries to the end of the list
        if (orderedFieldTargetEntries.size() < fieldAccessorTargetMap.size()) {
            fieldAccessorTargetMap.entrySet().stream()
                    .filter(entry -> !orderedFieldTargetEntries.contains(entry))
                    .forEach(orderedFieldTargetEntries::add);
        }

        this.columnMap = Collections.unmodifiableMap(columnMap);
        this.fieldNameColumnMap = Collections.unmodifiableMap(fieldNameColumnMap);
        this.columnNameFieldMap = Collections.unmodifiableMap(columnNameFieldMap);
        this.fieldAccessorTargetMap = Collections.unmodifiableMap(processedFieldTargetMap);
        this.fieldNameTargetMap = Collections.unmodifiableMap(fieldNameTargetMap);
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

    public List<FieldAccessor> getPrimaryKeyFields() {
        final List<ColumnMetaData> pkColumns = getMetaData().primaryKey();

        if (pkColumns.isEmpty()) {
            throw new IllegalStateException("Table '%s' has no primary key".formatted(getMetaData().name()));
        }

        return pkColumns.stream()
                .map(ColumnMetaData::name)
                .map(this::getFieldForColumnName)
                .toList();
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
    public ColumnMetaData columnMetaDataForField(final String fieldName) {
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(dtoClass, fieldName);
        return columnMetaDataForField(fieldAccessor);
    }

    public ColumnMetaData columnMetaDataForField(final FieldAccessor fieldAccessor) {
        final String key = (fieldAccessor instanceof FieldAccessorChain chain) ? chain.fieldPath() : fieldAccessor.name();
        return Objects.requireNonNull(fieldNameColumnMap.get(key), "No column for field path '" + key + "' in schema '" + metaData.schema() + "', table '" + metaData.name() + "'");
    }

    /**
     * Get the nested DTO classes.
     *
     * @return the list of nested DTO classes
     */
    public List<Class<?>> getNestedDtoClasses() {
        return nestedDtoClasses;
    }

    /**
     * Get the column metadata for the specified column name.
     *
     * @param columnName the column name to retrieve the metadata for
     * @return the column metadata for the specified column name, or null if not found
     */
    public ColumnMetaData getColumnMetaData(final String columnName) {
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
            return changeTracker.getTrackedDto(changeTracker.trackDtoFields(dto, fieldAccessorTargetMap.keySet(), true));
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
        changeTracker.trackDtoFields(dto, fieldAccessorTargetMap.keySet());
    }

    public @Nullable MappedFieldTarget mappedFieldTargetForFieldOrNull(final String fieldName) {
        return fieldNameTargetMap.get(fieldName);
    }

    public @Nullable MappedFieldTarget mappedFieldTargetForFieldOrNull(final FieldAccessor fieldAccessor) {
        return fieldAccessorTargetMap.get(fieldAccessor);
    }

    public MappedFieldTarget mappedFieldTargetForField(final String fieldName) {
        return ObjectUtils.requireNonNull(fieldNameTargetMap.get(fieldName), () -> new IllegalArgumentException("No field '" + fieldName + "' in DTO class: " + dtoClass));
    }

    public MappedFieldTarget mappedFieldTargetForField(final FieldAccessor fieldAccessor) {
        return ObjectUtils.requireNonNull(fieldAccessorTargetMap.get(fieldAccessor), () -> new IllegalArgumentException("No field '" + fieldAccessor.name() + "' in DTO class: " + dtoClass));
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

    /**
     * Get the field accessor for the specified column name, or null if not found.
     *
     * @param columnName the column name to retrieve the field accessor for
     * @return the field accessor for the specified column name, or null if not found
     */
    public @Nullable FieldAccessor fieldForColumnNameOrNull(final String columnName) {
        return columnNameFieldMap.get(columnName);
    }

    /**
     * Get a stream of field accessors for the table.
     *
     * @return a stream of field accessors
     */
    public Stream<FieldAccessor> fieldAcessorStream() {
        return fieldAccessorTargetMap.keySet().stream();
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

        final TrackedDto<?> trackedDto = changeTracker.getTrackedDtoOrNull(dto);

        if (trackedDto != null) {
            LOGGER.trace("Creating new snapshot for DTO: {}", dto);
            trackedDto.snapshot(true);
        } else {
            LOGGER.trace("Tracking DTO: {}", dto);
            changeTracker.trackDto(dto);
        }
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

    /**
     * Get the one-to-many mappings for the table.
     *
     * @return the list of one-to-many mappings
     */
    public final List<MappedOneToMany> getOneToManyMappings() {
        return fieldAccessorTargetMap.values().stream()
                .filter(MappedOneToMany.class::isInstance)
                .map(MappedOneToMany.class::cast)
                .toList();
    }

    /**
     * Get the many-to-many mappings for the table.
     *
     * @return the list of many-to-many mappings
     */
    public final List<MappedManyToMany> getManyToManyMappings() {
        return fieldAccessorTargetMap.values().stream()
                .filter(MappedManyToMany.class::isInstance)
                .map(MappedManyToMany.class::cast)
                .toList();
    }

    /**
     * Get the one-to-many mapping for the specified field.
     *
     * @param field the field accessor to retrieve the mapping for
     * @return the one-to-many mapping for the specified field, or empty if not found
     */
    public Optional<MappedOneToMany> getOneToManyMappingForField(final FieldAccessor field) {
        final MappedFieldTarget mappedFieldTarget = fieldAccessorTargetMap.get(field);

        if (mappedFieldTarget instanceof MappedOneToMany mappedOneToMany) {
            return Optional.of(mappedOneToMany);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the many-to-many mapping for the specified field.
     *
     * @param field the field accessor to retrieve the mapping for
     * @return the many-to-many mapping for the specified field, or empty if not found
     */
    public Optional<MappedManyToMany> getManyToManyMappingForField(final FieldAccessor field) {
        final MappedFieldTarget mappedFieldTarget = fieldAccessorTargetMap.get(field);

        if (mappedFieldTarget instanceof MappedManyToMany mappedManyToMany) {
            return Optional.of(mappedManyToMany);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the mapped field targets.
     *
     * @return the list of field accessor and mapped field target entries
     */
    public List<Map.Entry<FieldAccessor, MappedFieldTarget>> mappedFieldTargets() {
        return fieldTargetEntries;
    }

    public List<ColumnMetaData> mappedColumns() {
        return fieldTargetEntries.stream()
                .map(Map.Entry::getValue)
                .filter(ColumnMetaData.class::isInstance)
                .map(ColumnMetaData.class::cast)
                .toList();
    }

    /**
     * Get the context table registry.
     *
     * @return the context table registry
     */
    public TableRegistry getContextTableRegistry() {
        return contextTableRegistry;
    }

    /**
     * Add a one-to-many reverse mapping for the specified field.
     *
     * @param fieldAccessor the field accessor to add as a reverse mapping
     */
    public void addOneToManyReverseMapping(final FieldAccessor fieldAccessor) {
        if (oneToManyReverseMappings == null) {
            oneToManyReverseMappings = new ArrayList<>();
        }

        oneToManyReverseMappings.add(fieldAccessor);
    }

    /**
     * Get the one-to-many reverse mappings for the table.
     *
     * @return the list of one-to-many reverse mappings, or null if none
     */
    public @Nullable List<FieldAccessor> getOneToManyReverseMappings() {
        return oneToManyReverseMappings;
    }

    /**
     * Get the DTO class interfaces.
     *
     * @return the set of DTO class interfaces
     */
    public Set<Class<?>> getDtoClassInterfaces() {
        return dtoClassInterfaces;
    }

    /**
     * Set the DTO class interfaces.
     *
     * @param dtoClassInterfaces the set of DTO class interfaces
     */
    public void setDtoClassInterfaces(final Set<Class<?>> dtoClassInterfaces) {
        this.dtoClassInterfaces = dtoClassInterfaces;
    }

    /**
     * Get the related DTO classes.
     *
     * @return the set of related DTO classes
     */
    public Set<Class<?>> getRelatedDtoClasses() {
        return relatedDtoClasses;
    }

    public boolean isManyToManyJoinTable() {
        return manyToManyJoinTable;
    }
}
