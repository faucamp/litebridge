package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

/**
 * Type override proto-expression that allows nesting other proto-expressions.
 * <p>
 * This record is used to create a nested chain of expression instances (e.g. {@link UpperSpec}) when table information is available.
 *
 * @param typeOverride The type of the expression result.
 * @param target       The target expression; typically a column name to select via {@link SelectColumnSpec} or {@link SelectFieldSpec}.
 * @param alias        The column alias to use, or {@code null} if not specified.
 * @param type         The type of expression to create.
 * @param args         Extra expression-specific arguments.
 */
public record ProtoNestableTOExpr<T>(Class<T> typeOverride,
                                     Class<? extends ExpressionSpec> type,
                                     ExpressionSpec target,
                                     @Nullable String alias,
                                     @Nullable Object @Nullable [] args)
        implements ProtoNestableExpressionSpec, TypeOverrideExpressionSpec<T> {

    /**
     * Constructs a new ProtoTOColumnExpression instance with empty extra arguments.
     *
     * @param typeOverride The type of the expression result.
     * @param type         The type of expression to create.
     * @param target       The target expression; typically a column name to select via {@link SelectColumnSpec} or {@link SelectFieldSpec}.
     * @param alias        The column alias to use, or {@code null} if not specified.
     */
    public ProtoNestableTOExpr(final Class<T> typeOverride, final Class<? extends ExpressionSpec> type, final ExpressionSpec target, @Nullable final String alias) {
        this(typeOverride, type, target, alias, null);
    }

    /**
     * Constructs a new ProtoTOColumnExpression instance via column name.
     *
     * @param typeOverride The type of the expression result.
     * @param type         The type of expression to create.
     * @param column       The target column name to select.
     * @param alias        The column alias to use, or {@code null} if not specified.
     */
    public ProtoNestableTOExpr(final Class<T> typeOverride, final Class<? extends ExpressionSpec> type, final String column, final @Nullable String alias) {
        this(typeOverride, type, new ProtoColumnExpressionSpec(SelectColumnSpec.class, column), alias, null);
    }

    /**
     * Constructs a new ProtoTOColumnExpression instance.
     *
     * @param typeOverride The type of the expression result.
     * @param type         The type of expression to create.
     * @param column       The target column name to select.
     * @param alias        The column alias to use, or {@code null} if not specified.
     */
    public ProtoNestableTOExpr(final Class<T> typeOverride, final Class<? extends ExpressionSpec> type, final String column, final @Nullable String alias, final @Nullable Object @Nullable [] args) {
        this(typeOverride, type, new ProtoColumnExpressionSpec(SelectColumnSpec.class, column), alias, args);
    }

    @Override
    public Class<T> returnType() {
        return typeOverride;
    }
}
