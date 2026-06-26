package org.litebridgedb.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.type.TriFunction;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.NestableExpressionSpec;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.expression.ProtoNestableExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;
import org.litebridgedb.orm.expression.function.scalar.AbsSpec;
import org.litebridgedb.orm.expression.function.scalar.LowerSpec;
import org.litebridgedb.orm.expression.function.scalar.SubstringSpec;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.intent.ConvertSpec;
import org.litebridgedb.orm.expression.intent.ExpressionSpecArray;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class ProtoExpressionResolver {

    private static final Map<Class<? extends ExpressionSpec>, Function<Column, ExpressionSpec>> columnExpressions = Map.of(
            SelectColumnSpec.class, SelectColumnSpec::new,
            SelectFieldSpec.class, SelectColumnSpec::new);

    private static final Map<Class<? extends ExpressionSpec>, Function<ColumnExpressionSpec, NestableExpressionSpec>> nestableColumnExpressions = Map.of(
            UpperSpec.class, UpperSpec::new,
            LowerSpec.class, LowerSpec::new,
            AbsSpec.class, AbsSpec::new);

    private static final Map<Class<? extends ExpressionSpec>, BiFunction<ColumnExpressionSpec, Class<?>, NestableExpressionSpec>> typeOverrideColumnExpressions = Map.of(
            AvgSpec.class, AvgSpec::new,
            MinSpec.class, MinSpec::new,
            MaxSpec.class, MaxSpec::new);

    private static final Map<Class<? extends ExpressionSpec>, TriFunction<ColumnExpressionSpec, Class<?>, @Nullable Object[], NestableExpressionSpec>> argTypeOverrideExpressions = Map.of(
            SubstringSpec.class, (target, type, args) -> new SubstringSpec(target, (int) args[0], (Integer) args[1]));

    /**
     * Resolves a proto-expression into an {@link ExpressionSpec}.
     * <p>
     * If the input expression is not a {@link Resolvable}, it returns the expression as is.
     *
     * @param expressionSpec the proto-expression to resolve
     * @return the resolved {@link ExpressionSpec} corresponding to the provided lhs
     */
    public Stream<ExpressionSpec> resolveExpression(final ExpressionSpec expressionSpec) {
        if (expressionSpec instanceof ExpressionSpecArray(ExpressionSpec[] expressions)) {
            return Arrays.stream(expressions).flatMap(this::resolveExpression);
        } else if (expressionSpec instanceof Resolvable resolvable) {
            return resolveExpression(resolvable);
        } else {
            return Stream.of(expressionSpec);
        }
    }

    /**
     * Resolves a {@link Resolvable} into an {@link ExpressionSpec}.
     * <p>
     * If the input expression is not a {@link Resolvable}, it returns the expression as is.
     *
     * @param resolvable the {@link Resolvable} to resolve
     * @return the resolved {@link ExpressionSpec} corresponding to the provided lhs
     */
    public Stream<ExpressionSpec> resolveExpression(final Resolvable resolvable) {
        final Class<?> targetType = resolvable.type();
        final ExpressionSpec resolvedExpressionSpec;

        if (targetType == SelectFieldSpec.class) {
            resolvedExpressionSpec = resolveSelectField(resolvable);
        } else if (resolvable instanceof ProtoNestableExpressionSpec protoNestableExpressionSpec) {
            resolvedExpressionSpec = resolveNestableExpression(protoNestableExpressionSpec, getColumn(protoNestableExpressionSpec));
        } else if (resolvable instanceof ProtoColumnExpressionSpec protoColumnExpressionSpec) {
            resolvedExpressionSpec = columnExpressions.get(targetType).apply(getColumn(protoColumnExpressionSpec));
        } else if (resolvable instanceof ConvertSpec<?> convertSpec) {
            return resolveConvertSpec(convertSpec);
        } else {
            throw new IllegalStateException("Unsupported expression: " + resolvable);
        }

        return Stream.of(resolvedExpressionSpec);
    }

    protected Stream<ExpressionSpec> resolveConvertSpec(final ConvertSpec<?> convertSpec) {
        return Stream.of(convertSpec.replaceTarget(resolveExpression(convertSpec.target()).findFirst().orElseThrow()));
    }

    public static boolean isSupported(final Class<? extends ExpressionSpec> type) {
        return columnExpressions.containsKey(type)
                || nestableColumnExpressions.containsKey(type)
                || typeOverrideColumnExpressions.containsKey(type)
                || argTypeOverrideExpressions.containsKey(type);
    }

    private ColumnExpressionSpec resolveNestableExpression(final ProtoNestableExpressionSpec expression,
                                                           final Column column) {
        final ExpressionSpec nestedExpressionSpec = expression.target();

        final ColumnExpressionSpec resolvedNestedExpressionSpec = switch (nestedExpressionSpec) {
            case ColumnExpressionSpec columnExpression -> columnExpression;
            case ProtoNestableExpressionSpec protoNestableExpression ->
                    resolveNestableExpression(protoNestableExpression, column);
            case ProtoColumnExpressionSpec protoColumnExpression ->
                    (ColumnExpressionSpec) columnExpressions.get(protoColumnExpression.type()).apply(getColumn(protoColumnExpression));
            default -> throw new IllegalStateException("Unsupported expression: " + expression);
        };

        if (ConvertSpec.class.isAssignableFrom(expression.type())) {
            // Skip over the convert wrapper when dealing with the database; the inferred type is already propagated
            return resolvedNestedExpressionSpec;
        }

        if (expression.args() == null) {
            if (nestableColumnExpressions.containsKey(expression.type())) {
                return nestableColumnExpressions.get(expression.type()).apply(resolvedNestedExpressionSpec);
            }

            return typeOverrideColumnExpressions.get(expression.type()).apply(resolvedNestedExpressionSpec, expression.type());
        }

        return argTypeOverrideExpressions.get(expression.type()).apply(resolvedNestedExpressionSpec, expression.type(), expression.args());
    }

    protected abstract ColumnExpressionSpec resolveSelectField(final Resolvable resolvable);

    protected abstract Column getColumn(final Resolvable resolvable);
}
