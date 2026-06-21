package org.litebridgedb.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.api.select.FromClauseStart;
import org.litebridgedb.orm.api.select.FromClauseStartTypeOverride;
import org.litebridgedb.orm.api.select.SelectApi;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.ExpressionModifier;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.expression.TypeOverride;
import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

import java.util.Arrays;
import java.util.stream.Stream;

public class Subselect implements SelectApi {

    private final FromClauseEngine fromClauseEngine;

    public Subselect(final FromClauseEngine fromClauseEngine) {
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
    public FromClauseStart select(final ExpressionSpec... expressionSpecs) {
        return new FromClauseStart(expressionSpecs, fromClauseEngine);
    }

    @Override
    public <T> FromClauseStartTypeOverride<T> select(final TypeOverride<T> expression, final ExpressionSpec... otherExpressionSpecs) {
        if (otherExpressionSpecs.length == 0) {
            final ExpressionSpec[] expressionSpecs = {switch (expression) {
                case TypeOverrideExpressionSpec<?> typeOverrideExpression -> typeOverrideExpression;
                case ExpressionModifier expressionModifier -> expressionModifier.toExpression();
            }};

            return new FromClauseStartTypeOverride<>(expression.returnType(), expressionSpecs, fromClauseEngine);
        } else {
            final ExpressionSpec[] allExpressionSpecs = switch (expression) {
                case TypeOverrideExpressionSpec<?> typeOverrideExpression ->
                        Stream.concat(Stream.of(typeOverrideExpression), Arrays.stream(otherExpressionSpecs)).toArray(ExpressionSpec[]::new);
                case ExpressionModifier expressionModifier ->
                        Stream.concat(Stream.of(expressionModifier.toExpression()), Arrays.stream(otherExpressionSpecs)).toArray(ExpressionSpec[]::new);
            };

            return new FromClauseStartTypeOverride<>(expression.returnType(), allExpressionSpecs, fromClauseEngine);
        }
    }

    @Override
    public <T> FromClauseStartTypeOverride<T> select(final TypeOverrideExpressionSpec<T> expression, final ExpressionSpec... otherExpressionSpecs) {
        return select((TypeOverride<T>) expression, otherExpressionSpecs);
    }

    @Override
    public FromClauseStart select() {
        return new FromClauseStart(fromClauseEngine);
    }
}
