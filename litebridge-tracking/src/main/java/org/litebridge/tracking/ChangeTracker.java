package org.litebridge.tracking;

import jakarta.annotation.Nonnull;
import org.litebridge.commons.ObjectUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides functionality to track field-level changes of Data Transfer Objects (DTOs)
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
     * This map uses a {@link WeakHashMap} to allow garbage collection of DTOs that are no longer in use, thereby
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
    private final Map<Object, TrackedDto<?>> trackedDtos = Collections.synchronizedMap(new WeakHashMap<>());

    public <T> T trackDto(final T dto) {
        ObjectUtils.requireNonNull(dto, "DTO cannot be null");
        return trackImpl(dto, ClassFieldCache.getFields(dto), false);
    }

    /**
     * Tracks the specified fields of a given Data Transfer Object (DTO) for detecting changes.
     * This method identifies and stores a snapshot of the fields provided in the {@code trackedFieldNames} set,
     * enabling monitoring of modifications during the lifecycle of the DTO.
     *
     * @param <T>               the type of the DTO being tracked
     * @param dto               the Data Transfer Object (DTO) to be tracked; must not be null
     * @param trackedFieldNames the set of field names in the DTO to be tracked; each field name must exist in the DTO
     * @return the tracked instance of the provided DTO
     * @throws IllegalArgumentException if {@code dto} is null
     * @throws IllegalArgumentException if any field name in {@code trackedFieldNames} does not exist in the DTO
     */
    public <T> T trackDto(final T dto, final Set<String> trackedFieldNames) {
        ObjectUtils.requireNonNull(dto, "DTO cannot be null");
        final Map<String, Field> allFields = ClassFieldCache.getFields(dto).stream()
                .collect(Collectors.toMap(Field::getName, Function.identity()));
        final Set<Field> trackedFields = trackedFieldNames.stream()
                .map(fieldName -> ObjectUtils.requireNonNull(allFields.get(fieldName), "Field '%s' does not exist in DTO '%s'".formatted(fieldName, dto.getClass().getName())))
                .collect(Collectors.toSet());
        return trackImpl(dto, trackedFields, false);
    }

    /**
     * Tracks the specified fields of a given Data Transfer Object (DTO) for detecting changes.
     * This method allows monitoring of specific fields within the DTO by capturing their initial state.
     *
     * @param <T>           the type of the DTO being tracked
     * @param dto           the Data Transfer Object (DTO) to be tracked; must not be null
     * @param trackedFields the set of fields in the DTO to be tracked; each field must belong to the DTO
     * @return the tracked instance of the provided DTO
     * @throws IllegalArgumentException if {@code dto} is null
     */
    public <T> T trackDtoFields(final T dto, final Set<Field> trackedFields) {
        ObjectUtils.requireNonNull(dto, "DTO cannot be null");
        return trackImpl(dto, trackedFields, false);
    }

    /**
     * Retrieves the tracked version of the specified Data Transfer Object (DTO), if it exists.
     * The method looks up the internal storage for the tracked version of the given DTO and returns it.
     *
     * @param dto the Data Transfer Object (DTO) whose tracked version is to be retrieved; can be null
     * @return the tracked version of the specified DTO, or null if no tracked version exists
     */
    public <T> @Nonnull TrackedDto<T> getTrackedDto(final T dto) {
        return (TrackedDto<T>) ObjectUtils.requireNonNull(trackedDtos.get(dto), "DTO is not tracked: " + dto);
    }

    private <T> T trackImpl(@Nonnull final T dto, @Nonnull final Set<Field> trackedFields, final boolean snapshotEmpty) {
        if (trackedDtos.containsKey(dto)) {
            return dto;
        }

        final TrackedDto<T> trackedDto = new TrackedDto<>(dto, this::trackNestedDto);

        if (snapshotEmpty) {
            // Create an empty snapshot (useful for highlighting "all fields are new" in newly created nested DTOs)
            trackedDto.snapshotEmpty(trackedFields);
        } else {
            // Default behaviour
            trackedDto.snapshot(trackedFields, false);
        }

        trackedDtos.put(dto, trackedDto);
        return dto;
    }

    private void trackNestedDto(final Object dto) {
        ObjectUtils.requireNonNull(dto, () -> new IllegalStateException("trackNestedDto() called with null value"));
        trackImpl(dto, ClassFieldCache.getFields(dto), true);
    }
}
