package org.litebridge.tracking;

import jakarta.annotation.Nullable;

import java.util.Map;

public record ChangedField(String fieldName, Object value, @Nullable Map<?, Integer> originalMapSnapshot) {

}
