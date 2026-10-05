package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.math.MathOperator;

public record UpdateColumn(String name,
                           @Nullable ColumnValueGenerator generator,
                           @Nullable MathOperator mathOperator,
                           @Nullable Integer bindValueIndex) {

    public UpdateColumn(final String name) {
        this(name, null, null, null);
    }

    public UpdateColumn(final String name,
                        final @Nullable ColumnValueGenerator generator,
                        final @Nullable MathOperator mathOperator) {
        this(name, generator, mathOperator, null);
    }
}
