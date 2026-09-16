package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.expression.function.scalar.UpperSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.Objects;

/**
 * Proto-expression that allows nesting other proto-expressions.
 * <p>
 * This record is used to create a nested chain of expression instances (e.g. {@link UpperSpec}) when table information is available.
 *
 */
public final class ProtoNestableBasicExprSpec extends AbstractAliasable
        implements ProtoNestableExpressionSpec {

    private final Class<? extends ExpressionSpec> type;
    private final ProtoExpressionSpec target;

    /**
     * Creates a new ProtoNestableBasicExprSpec.
     *
     * @param type   the type of expression to create
     * @param target the target expression specification
     * @param alias  the column alias
     */
    public ProtoNestableBasicExprSpec(final Class<? extends ExpressionSpec> type,
                                      final ProtoExpressionSpec target,
                                      final @Nullable String alias) {
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
    public @Nullable String alias() {
        return getAlias();
    }

    @Override
    public @Nullable Object @Nullable [] args() {
        return null;
    }

    @Override
    public Class<? extends ExpressionSpec> type() {
        return type;
    }

    @Override
    public ProtoExpressionSpec target() {
        return target;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ProtoNestableBasicExprSpec) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.target, that.target) &&
                Objects.equals(this.alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, target, alias);
    }

    @Override
    public String toString() {
        return "ProtoNestableBasicExprSpec[" +
                "type=" + type + ", " +
                "target=" + target + ", " +
                "alias=" + alias + ']';
    }

}
