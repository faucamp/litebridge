package org.litebridge.dto;

import jakarta.annotation.Nullable;

import java.util.Map;

public record ChangedField(String fieldName, TrackedField trackedField, Object value, @Nullable Map<?, Integer> originalMapSnapshot) {

}
