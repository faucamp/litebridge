package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

public final class BooleanUtils {

    private BooleanUtils() {
    }

    public static boolean toBoolean(final @Nullable Boolean value) {
        return Boolean.TRUE.equals(value);
    }

}
