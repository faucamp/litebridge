package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.math.MathOperation;

public record UpdateColumn(String name, @Nullable Object generatedValue, @Nullable MathOperation mathOperation) {

    public UpdateColumn(final String name, final Object generatedValue) {
        this(name, generatedValue, null);
    }

    public UpdateColumn(final String name) {
        this(name, null, null);
    }
}
