package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.select.SelectColumn;

/**
 * Type override proto-expression that allows nesting other proto-expressions.
 * <p>
 * This record is used to create a nested chain of expression instances (e.g. {@link UpperSpec}) when table information is available.
 *
 * @param typeOverride The type of the expression result.
 * @param target       The target expression; typically a column name to select via {@link SelectColumn} or {@link org.litebridgedb.orm.expression.select.SelectField}.
 * @param alias        The column alias to use, or {@code null} if not specified.
 * @param type         The type of expression to create.
 * @param args         Extra expression-specific arguments.
 */
public record ProtoNestableTOExpr<T>(Class<T> typeOverride,
                                     Class<? extends Expression> type,
                                     ProtoExpression target,
                                     @Nullable String alias,
                                     @Nullable Object @Nullable [] args)
        implements ProtoNestableExpression, TypeOverrideExpression<T> {

    /**
     * Constructs a new ProtoTOColumnExpression instance with empty extra arguments.
     *
     * @param typeOverride The type of the expression result.
     * @param target       The target expression; typically a column name to select via {@link SelectColumn} or {@link org.litebridgedb.orm.expression.select.SelectField}.
     * @param alias        The column alias to use, or {@code null} if not specified.
     * @param type         The type of expression to create.
     */
    public ProtoNestableTOExpr(final Class<T> typeOverride, final Class<? extends Expression> type, final ProtoExpression target, @Nullable final String alias) {
        this(typeOverride, type, target, alias, null);
    }

    /**
     * Constructs a new ProtoTOColumnExpression instance with empty extra arguments.
     *
     * @param typeOverride The type of the expression result.
     * @param column       The target column name to select.
     * @param alias        The column alias to use, or {@code null} if not specified.
     * @param type         The type of expression to create.
     */
    public ProtoNestableTOExpr(final Class<T> typeOverride, final Class<? extends Expression> type, final String column, final @Nullable String alias) {
        this(typeOverride, type, new ProtoColumnExpression(SelectColumn.class, column), alias, null);
    }

    /**
     * Constructs a new ProtoTOColumnExpression instance with empty extra arguments.
     *
     * @param typeOverride The type of the expression result.
     * @param column       The target column name to select.
     * @param alias        The column alias to use, or {@code null} if not specified.
     * @param type         The type of expression to create.
     */
    public ProtoNestableTOExpr(final Class<T> typeOverride, final Class<? extends Expression> type, final String column, final @Nullable String alias, final @Nullable Object @Nullable [] args) {
        this(typeOverride, type, new ProtoColumnExpression(SelectColumn.class, column), alias, args);
    }

    @Override
    public Class<T> returnType() {
        return typeOverride;
    }
}
