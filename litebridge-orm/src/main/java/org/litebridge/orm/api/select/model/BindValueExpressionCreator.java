package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SelectExpression;

/**
 * Functional interface for creating bind value expressions.
 */
@FunctionalInterface
public interface BindValueExpressionCreator {

    /**
     * Creates a select expression for the specified value.
     *
     * @param value the value to bind
     * @return the select expression
     */
    SelectExpression create(@Nullable Object value);
}
