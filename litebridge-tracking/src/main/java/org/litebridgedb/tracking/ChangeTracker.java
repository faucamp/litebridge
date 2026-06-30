package org.litebridgedb.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.commons.type.WeakIdentityMap;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides functionality to track field-level changes of data transfer objects (DTOs)
 * by maintaining a snapshot of their specified fields. It allows identifying changes made to
 * the tracked fields during the object's lifecycle.
 * <p>
 * The {@code ChangeTracker} is particularly useful for scenarios involving change tracking
 * or auditing where fields of certain objects need to be monitored for modifications.
 * <p>
 * This class is thread-safe.
 */
public final class ChangeTracker {

    /**
     * A thread-safe map used to store and manage associations between tracked Data Transfer Objects (DTOs)
     * and their respective {@link TrackedDto} instances. The map serves as the internal state for maintaining
     * change tracking data across multiple DTO objects.
     * <p>
     * This map uses a {@link WeakIdentityMap} to allow garbage collection of DTOs that are no longer in use, thereby
     * preventing memory leaks in cases where the tracked DTOs are no longer referenced elsewhere in the application.
     * The use of {@link Collections#synchronizedMap(Map)} ensures thread-safety when accessing or modifying the map.
     * <p>
     * Keys:
     * - The keys of the map are the DTO objects that are being tracked.
     * <p>
     * Values:
     * - The values of the map are {@link TrackedDto} objects, which encapsulate field-level snapshots and changes
     * for the associated DTO.
     * <p>
     * Purpose:
     * - This map enables efficient lookups for retrieving tracked changes related to any given DTO and provides
     * the underlying infrastructure for supporting functionalities such as change detection, auditing, or monitoring
     * modifications to fields over an object's lifecycle.
     */
    private final Map<Object, TrackedDto<?>> trackedDtos = Collections.synchronizedMap(new WeakIdentityMap<>());
    private final ClassFieldAccessorCache classFieldAccessorCache;

    public ChangeTracker() {
        this(MethodHandles.lookup());
    }

    public ChangeTracker(final MethodHandles.Lookup lookup) {
        this.classFieldAccessorCache = new ClassFieldAccessorCache(lookup);
    }

    /**
     * Tracks the given Data Transfer Object (DTO) for detecting changes.
     * <p>
     * This method initializes tracking for the DTO and ensures that its state can be monitored
     * for modifications during its lifecycle.
     *
     * @param <DTO> the type of the Data Transfer Object (DTO) to be tracked
     * @param dto   the Data Transfer Object (DTO) to be tracked; must not be null
     * @return the tracked instance of the provided DTO
     * @throws NullPointerException if the provided {@code dto} is null
     */
    public <DTO> DTO trackDto(final DTO dto) {
        Objects.requireNonNull(dto, "DTO cannot be null");
        return trackImpl(dto, classFieldAccessorCache.fieldAccessors(dto.getClass()), false);
    }

    /**
     * Tracks the specified fields of a given Data Transfer Object (DTO) for detecting changes.
     * This method identifies and stores a snapshot of the fields provided in the {@code trackedFieldNames} set,
     * enabling monitoring of modifications during the lifecycle of the DTO.
     *
     * @param <DTO>             the type of the DTO being tracked
     * @param dto               the Data Transfer Object (DTO) to be tracked; must not be null
     * @param trackedFieldNames the set of field names in the DTO to be tracked; each field name must exist in the DTO
     * @return the tracked instance of the provided DTO
     * @throws IllegalArgumentException if {@code dto} is null
     * @throws IllegalArgumentException if any field name in {@code trackedFieldNames} does not exist in the DTO
     */
    public <DTO> DTO trackDto(final DTO dto, final Set<String> trackedFieldNames) {
        Objects.requireNonNull(dto, "DTO cannot be null");
        final Map<String, FieldAccessor> allFields = classFieldAccessorCache.fieldAccessors(dto.getClass()).stream()
                .collect(Collectors.toMap(FieldAccessor::name, Function.identity()));
        final List<FieldAccessor> trackedFields = trackedFieldNames.stream()
                .map(fieldName -> ObjectUtils.requireNonNull(allFields.get(fieldName), () -> new IllegalArgumentException("Field '%s' does not exist in DTO '%s'".formatted(fieldName, dto.getClass().getName()))))
                .toList();
        return trackImpl(dto, trackedFields, false);
    }

    /**
     * Tracks the specified fields of a given Data Transfer Object (DTO) for detecting changes.
     * <p>
     * This method allows monitoring of specific fields within the DTO by capturing their initial state.
     *
     * @param <DTO>         the type of the DTO being tracked
     * @param dto           the Data Transfer Object (DTO) to be tracked; must not be null
     * @param trackedFields the set of fields in the DTO to be tracked; each field must belong to the DTO
     * @return the tracked instance of the provided DTO
     * @throws IllegalArgumentException if {@code dto} is null
     */
    public <DTO> DTO trackDtoFields(final DTO dto, final Set<FieldAccessor> trackedFields) {
        return trackDtoFields(dto, trackedFields, false);
    }

    /**
     * Tracks the fields of the given Data Transfer Object (DTO) for detecting changes.
     * <p>
     * This method allows monitoring specific fields within the DTO by capturing their initial state
     * and optionally creating an empty snapshot.
     *
     * @param <DTO>         the type of the DTO being tracked
     * @param dto           the Data Transfer Object (DTO) to be tracked; must not be null
     * @param trackedFields the set of fields in the DTO to be tracked; each field must belong to the DTO
     * @param snapshotEmpty if true, an empty snapshot will be created; otherwise, normal snapshot behavior is applied
     * @return the tracked instance of the provided DTO
     * @throws NullPointerException if the provided {@code dto} is null
     */
    public <DTO> DTO trackDtoFields(final DTO dto, final Set<FieldAccessor> trackedFields, final boolean snapshotEmpty) {
        Objects.requireNonNull(dto, "DTO cannot be null");
        return trackImpl(dto, trackedFields, snapshotEmpty);
    }

    /**
     * Retrieves the tracked version of the specified Data Transfer Object (DTO), if it exists.
     * <p>
     * The method looks up the internal storage for the tracked version of the given DTO and returns it.
     *
     * @param dto the Data Transfer Object (DTO) whose tracked version is to be retrieved; can be null
     * @return the tracked version of the specified DTO, or null if no tracked version exists
     * @throws IllegalArgumentException if the specified DTO is not tracked
     */
    public <DTO> TrackedDto<DTO> getTrackedDto(final DTO dto) {
        return Objects.requireNonNull(getTrackedDtoOrNull(dto), "DTO is not tracked: " + dto);
    }

    /**
     * Retrieves a set of tracked Data Transfer Objects (DTOs) of the specified type.
     *
     * @param <DTO>    the type of the Data Transfer Object (DTO) to be retrieved
     * @param dtoClass the {@code Class} object representing the type of DTO to retrieve; must not be null
     * @return a set of {@code TrackedDto<DTO>} objects that correspond to the specified DTO type;
     * returns an empty set if no tracked DTOs of the specified type exist
     * @throws NullPointerException if {@code dtoClass} is null
     */
    @SuppressWarnings("unchecked")
    public <DTO> Set<TrackedDto<DTO>> getTrackedDtos(final Class<DTO> dtoClass) {
        return trackedDtos.values().stream()
                .filter(trackedDto -> trackedDto.dto().getClass() == dtoClass)
                .map(trackedDto -> (TrackedDto<DTO>) trackedDto)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves the tracked version of the specified Data Transfer Object (DTO), if it exists.
     * <p>
     * The method attempts to fetch the associated tracked DTO from the internal storage
     * and returns it. If no tracked version exists for the provided DTO, the method
     * returns {@code null}.
     *
     * @param <DTO> the type of the Data Transfer Object (DTO)
     * @param dto   the Data Transfer Object (DTO) whose tracked version is to be retrieved; can be null
     * @return the tracked version of the specified DTO, or {@code null} if no tracked version exists
     */
    @SuppressWarnings("unchecked")
    public <DTO> @Nullable TrackedDto<DTO> getTrackedDtoOrNull(final DTO dto) {
        return (TrackedDto<DTO>) trackedDtos.get(dto);
    }

    /**
     * Retrieves the instance of {@code ClassFieldAccessorCache} associated with the {@code ChangeTracker}.
     *
     * @return the {@code ClassFieldAccessorCache} instance used by this {@code ChangeTracker}
     */
    public ClassFieldAccessorCache classFieldAccessorCache() {
        return classFieldAccessorCache;
    }

    private <DTO> DTO trackImpl(final DTO dto, final Collection<FieldAccessor> trackedFields, final boolean snapshotEmpty) {
        if (trackedDtos.containsKey(dto)) {
            return dto;
        }

        final TrackedDto<DTO> trackedDto = new TrackedDto<>(dto, trackedFields, classFieldAccessorCache, this::trackNestedDto);

        if (snapshotEmpty) {
            // Create an empty snapshot (useful for highlighting "all fields are new" in newly created nested DTOs)
            trackedDto.snapshotEmpty();
        } else {
            // Default behaviour
            trackedDto.snapshot(false);
        }

        trackedDtos.put(dto, trackedDto);
        return dto;
    }

    private void trackNestedDto(final Object dto) {
        trackImpl(dto, classFieldAccessorCache.fieldAccessors(Objects.requireNonNull(dto, "Nested DTO is null").getClass()), true);
    }
}
