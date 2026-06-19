package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.select.SelectColumn;

/**
 * Proto-expression that allows nesting other proto-expressions.
 * <p>
 * This record is used to create a nested chain of expression instances (e.g. {@link UpperSpec}) when table information is available.
 *
 * @param target The target expression; typically a column name to select via {@link SelectColumn} or {@link org.litebridgedb.orm.expression.select.SelectField}.
 * @param alias  The column alias to use, or {@code null} if not specified.
 * @param type   The type of expression to create.
 */
public record ProtoNestableBasicExpr(Class<? extends Expression> type, ProtoExpression target, @Nullable String alias)
        implements ProtoNestableExpression {

    public ProtoNestableBasicExpr(final Class<? extends Expression> type, final ProtoExpression target, final @Nullable String alias) {
        // Validate that a supported expression type is specified
        if (!ProtoExpressionRegistry.isSupported(type)) {
            throw new IllegalArgumentException("Unsupported expression type: " + type);
        }

        this.type = type;
        this.target = target;
        this.alias = alias;
    }

    @Override
    public @Nullable Object @Nullable [] args() {
        return null;
    }
}
