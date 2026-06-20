package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.select.impl.ProtoExpressionResolver;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

/**
 * Proto-expression that allows nesting other proto-expressions.
 * <p>
 * This record is used to create a nested chain of expression instances (e.g. {@link UpperSpec}) when table information is available.
 *
 * @param target The target expression; typically a column name to select via {@link SelectColumnSpec} or {@link SelectFieldSpec}.
 * @param alias  The column alias to use, or {@code null} if not specified.
 * @param type   The type of expression to create.
 */
public record ProtoNestableBasicExprSpec(Class<? extends ExpressionSpec> type,
                                         ProtoExpressionSpec target,
                                         @Nullable String alias)
        implements ProtoNestableExpressionSpec {

    public ProtoNestableBasicExprSpec(final Class<? extends ExpressionSpec> type, final ProtoExpressionSpec target, final @Nullable String alias) {
        // Validate that a supported expression type is specified
        if (!ProtoExpressionResolver.isSupported(type)) {
            throw new IllegalArgumentException("Unsupported expression type: " + type);
        }

        this.type = type;
        this.target = target;
        this.alias = alias;
    }

    /**
     * Constructs a new ProtoNestableBasicExprSpec instance via column name.
     *
     * @param type   The type of expression to create.
     * @param column The target column name to select.
     * @param alias  The column alias to use, or {@code null} if not specified.
     */
    public ProtoNestableBasicExprSpec(final Class<? extends ExpressionSpec> type, final String column, final @Nullable String alias) {
        this(type, new ProtoColumnExpressionSpec(SelectColumnSpec.class, column), alias);
    }

    @Override
    public @Nullable Object @Nullable [] args() {
        return null;
    }
}
