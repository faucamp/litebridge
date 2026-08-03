package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SelectExpression;

@FunctionalInterface
public interface BindValueExpressionCreator {

    SelectExpression create(@Nullable Object value);
}
