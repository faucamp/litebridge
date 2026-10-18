package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.math.MathOperator;

public record UpdateColumn(String name,
                           @Nullable ColumnValueGenerator generator,
                           @Nullable MathOperator mathOperator,
                           @Nullable Integer bindValueIndex,
                           @Nullable SelectExpression staticValue) {

    public UpdateColumn(final String name, @Nullable final ColumnValueGenerator generator, @Nullable final MathOperator mathOperator, @Nullable final Integer bindValueIndex) {
        this(name, generator, mathOperator, bindValueIndex, null);
    }

    public UpdateColumn(final String name, @Nullable final SelectExpression staticValue) {
        this(name, null, null, null, staticValue);
    }

    public UpdateColumn(final String name) {
        this(name, null, null, null, null);
    }

    public UpdateColumn(final String name,
                        final @Nullable ColumnValueGenerator generator,
                        final @Nullable MathOperator mathOperator) {
        this(name, generator, mathOperator, null, null);
    }
}
