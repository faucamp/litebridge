package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ChangedFields {

    private final Map<String, ChangedField> changedFieldMap;

    public ChangedFields(final Map<String, ChangedField> changedFieldMap) {
        this.changedFieldMap = Collections.unmodifiableMap(ObjectUtils.requireNonNull(changedFieldMap, "Changed fields map cannot be null"));
    }

    public Optional<ChangedField> get(final String fieldName) {
        return Optional.ofNullable(changedFieldMap.get(fieldName));
    }

    public @Nullable ChangedField getOrNull(final String fieldName) {
        return changedFieldMap.get(fieldName);
    }

    public Stream<ChangedField> stream() {
        return changedFieldMap.values().stream();
    }

    public void forEach(final Consumer<ChangedField> action) {
        changedFieldMap.values().forEach(action);
    }

    public boolean isEmpty() {
        return changedFieldMap.isEmpty();
    }

    public boolean contains(final String fieldName) {
        return changedFieldMap.containsKey(fieldName);
    }

    public int size() {
        return changedFieldMap.size();
    }
}
