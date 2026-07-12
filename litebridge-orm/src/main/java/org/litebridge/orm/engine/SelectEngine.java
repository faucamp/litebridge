package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.FromClauseStart;
import org.litebridge.orm.api.select.FromClauseStartTypeOverride;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.expression.ExpressionModifier;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.TypeOverride;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;

import java.util.Arrays;

public class SelectEngine implements SelectApi {

    private final FromClauseEngine fromClauseEngine;

    public SelectEngine(final FromClauseEngine fromClauseEngine) {
        this.fromClauseEngine = fromClauseEngine;
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass) {
        return select(dtoClass, (RelatedDtoStrategy) null);
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return new FromClauseStart(fromClauseEngine).from(dtoClass, relatedDtoStrategy);
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return new FromClauseStart(fromClauseEngine).from(dtoClass, contextDtoClass);
    }

    @Override
    public FromClauseStart select(final String... fieldsOrColumns) {
        return new FromClauseStart(Arrays.stream(fieldsOrColumns)
                .map(fieldOrColumn -> new ProtoColumnExpressionSpec(SelectFieldSpec.class, fieldOrColumn, null))
                .toArray(ProtoColumnExpressionSpec[]::new),
                fromClauseEngine);
    }

    @Override
    public FromClauseStart select(final ExpressionSpec... expressions) {
        return new FromClauseStart(expressions, fromClauseEngine);
    }

    @Override
    public <T> FromClauseStartTypeOverride<T> select(final TypeOverride<T> expression) {
        final ExpressionSpec[] expressionSpecs = {switch (expression) {
            case TypeOverrideExpressionSpec<?> typeOverrideExpression -> typeOverrideExpression;
            case ExpressionModifier expressionModifier -> expressionModifier.toExpression();
        }};

        return new FromClauseStartTypeOverride<>(expression.returnType(), expressionSpecs, fromClauseEngine);
    }

    @Override
    public FromClauseStart select() {
        return new FromClauseStart(fromClauseEngine);
    }
}
