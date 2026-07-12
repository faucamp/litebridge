package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.type.TriFunction;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.DelegateExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoNestableExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.function.aggregate.AvgSpec;
import org.litebridge.orm.expression.function.aggregate.MaxSpec;
import org.litebridge.orm.expression.function.aggregate.MinSpec;
import org.litebridge.orm.expression.function.scalar.AbsSpec;
import org.litebridge.orm.expression.function.scalar.LowerSpec;
import org.litebridge.orm.expression.function.scalar.SubstringSpec;
import org.litebridge.orm.expression.function.scalar.UpperSpec;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.intent.ExpressionSpecArray;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.meta.QueryField;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Resolver for proto-expressions that have not yet been bound to a specific table or context.
 * <p>
 * This class provides methods for resolving these proto-expressions into final
 * {@link ExpressionSpec}s that can be used by the database provider.
 */
public abstract class ProtoExpressionResolver {

    private static final Map<Class<? extends ExpressionSpec>, Function<Column, ExpressionSpec>> columnExpressions = Map.of(
            SelectColumnSpec.class, SelectColumnSpec::new,
            SelectFieldSpec.class, SelectColumnSpec::new);

    private static final Map<Class<? extends ExpressionSpec>, Function<ColumnExpressionSpec, DelegateExpressionSpec>> nestableColumnExpressions = Map.of(
            UpperSpec.class, UpperSpec::new,
            LowerSpec.class, LowerSpec::new,
            AbsSpec.class, AbsSpec::new);

    private static final Map<Class<? extends ExpressionSpec>, BiFunction<ColumnExpressionSpec, Class<?>, DelegateExpressionSpec>> typeOverrideColumnExpressions = Map.of(
            AvgSpec.class, AvgSpec::new,
            MinSpec.class, MinSpec::new,
            MaxSpec.class, MaxSpec::new);

    @SuppressWarnings("DataFlowIssue")
    private static final Map<Class<? extends ExpressionSpec>, TriFunction<ColumnExpressionSpec, Class<?>, @Nullable Object[], DelegateExpressionSpec>> argTypeOverrideExpressions = Map.of(
            SubstringSpec.class, (target, type, args) -> new SubstringSpec(target, (int) args[0], (Integer) args[1]));

    /**
     * Resolves a proto-expression into an {@link ExpressionSpec}.
     * <p>
     * If the input expression is not a {@link Resolvable}, it returns the expression as is.
     *
     * @param expressionSpec the proto-expression to resolve
     * @param clause the clause type where the expression is being used
     * @return the resolved {@link ExpressionSpec} corresponding to the provided column
     */
    public Stream<ExpressionSpec> resolveExpression(final ExpressionSpec expressionSpec, final ClauseType clause) {
        return switch (expressionSpec) {
            case ExpressionSpecArray(ExpressionSpec[] expressions) ->
                    Arrays.stream(expressions).flatMap(expression -> resolveExpression(expression, clause));
            case Resolvable resolvable -> resolveExpression(resolvable, clause);
            case QueryField queryField -> resolveExpression(queryField, clause);
            default -> Stream.of(expressionSpec);
        };
    }

    /**
     * Resolves a {@link Resolvable} into an {@link ExpressionSpec}.
     * <p>
     * If the input expression is not a {@link Resolvable}, it returns the expression as is.
     *
     * @param resolvable the {@link Resolvable} to resolve
     * @param clause the clause type where the expression is being used
     * @return the resolved {@link ExpressionSpec} corresponding to the provided column
     */
    public Stream<ExpressionSpec> resolveExpression(final Resolvable resolvable, final ClauseType clause) {
        final Class<?> targetType = resolvable.type();
        final ExpressionSpec resolvedExpressionSpec;

        if (targetType == SelectFieldSpec.class) {
            resolvedExpressionSpec = resolveSelectField(resolvable, clause);
        } else if (resolvable instanceof ProtoNestableExpressionSpec protoNestableExpressionSpec) {
            resolvedExpressionSpec = resolveDelegateExpression(protoNestableExpressionSpec, clause);
        } else if (resolvable instanceof ProtoColumnExpressionSpec protoColumnExpressionSpec) {
            resolvedExpressionSpec = columnExpressions.get(targetType).apply(getColumn(protoColumnExpressionSpec, clause));
        } else if (resolvable instanceof ConvertSpec<?> convertSpec) {
            return resolveConvertSpec(convertSpec, clause);
        } else {
            throw new IllegalStateException("Unsupported expression: " + resolvable);
        }

        return Stream.of(resolvedExpressionSpec);
    }

    /**
     * Resolves a list of expression specifications.
     *
     * @param expressionSpecs the list of expression specifications to resolve
     * @param clause the clause type where the expressions are being used
     * @return the list of resolved expression specifications
     */
    public List<ExpressionSpec> resolveExpressions(final List<ExpressionSpec> expressionSpecs, final ClauseType clause) {
        return expressionSpecs.stream().flatMap(expressionSpec -> resolveExpression(expressionSpec, clause)).toList();
    }

    /**
     * Resolves a convert specification.
     *
     * @param convertSpec the convert specification to resolve
     * @param clause the clause type where the expression is being used
     * @return a stream containing the resolved expression specification
     */
    protected Stream<ExpressionSpec> resolveConvertSpec(final ConvertSpec<?> convertSpec, final ClauseType clause) {
        return Stream.of(convertSpec.replaceTarget(resolveExpression(convertSpec.target(), clause).findFirst().orElseThrow()));
    }

    /**
     * Checks if a given expression type is supported for resolution.
     *
     * @param type the expression type to check
     * @return {@code true} if supported, {@code false} otherwise
     */
    public static boolean isSupported(final Class<? extends ExpressionSpec> type) {
        return columnExpressions.containsKey(type)
                || nestableColumnExpressions.containsKey(type)
                || typeOverrideColumnExpressions.containsKey(type)
                || argTypeOverrideExpressions.containsKey(type);
    }

    private ColumnExpressionSpec resolveDelegateExpression(final ProtoNestableExpressionSpec expression, final ClauseType clause) {
        final ExpressionSpec nestedExpressionSpec = expression.target();

        final ColumnExpressionSpec resolvedNestedExpressionSpec = switch (nestedExpressionSpec) {
            case ColumnExpressionSpec columnExpression -> columnExpression;
            case ProtoNestableExpressionSpec protoNestableExpression ->
                    resolveDelegateExpression(protoNestableExpression, clause);
            case ProtoColumnExpressionSpec protoColumnExpression -> {
                if (protoColumnExpression.type() == SelectFieldSpec.class) {
                    yield resolveSelectField(protoColumnExpression, clause);
                } else {
                    yield new SelectColumnSpec(getColumn(protoColumnExpression, clause));
                }
            }
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

        //noinspection DataFlowIssue
        return argTypeOverrideExpressions.get(expression.type()).apply(resolvedNestedExpressionSpec, expression.type(), expression.args());
    }

    private Stream<ExpressionSpec> resolveExpression(final QueryField queryField, final ClauseType clause) {
        return Stream.of(resolveSelectField(queryField, clause));
    }

    /**
     * Resolves a resolvable into a column expression specification.
     *
     * @param resolvable the resolvable to resolve
     * @param clause the clause type where the expression is being used
     * @return the resolved column expression specification
     */
    protected abstract ColumnExpressionSpec resolveSelectField(final Resolvable resolvable, final ClauseType clause);

    /**
     * Resolves a query field into a column expression specification.
     *
     * @param queryField the query field to resolve
     * @param clause the clause type where the expression is being used
     * @return the resolved column expression specification
     */
    protected abstract ColumnExpressionSpec resolveSelectField(final QueryField queryField, final ClauseType clause);

    /**
     * Returns the database column for a resolvable.
     *
     * @param resolvable the resolvable to get the column for
     * @param clause the clause type where the expression is being used
     * @return the database column
     */
    protected abstract Column getColumn(final Resolvable resolvable, final ClauseType clause);
}
