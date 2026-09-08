package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.math.MathOperator;

public record UpdateColumn(String name,
                           @Nullable ColumnValueGenerator generator,
                           @Nullable MathOperator mathOperator) {

    public UpdateColumn(final String name) {
        this(name, null, null);
    }
}
