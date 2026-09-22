package org.litebridge.db.spi.impl.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * An encapsulated literal value in a query expression.
 */
public class LiteralExpressionImpl extends AbstractAliasedExpression implements LiteralExpression {

    private final @Nullable Object value;
    private final boolean parameter;

    /**
     * Constructs a new {@code LiteralExpressionImpl} with the given value.
     *
     * @param value the literal value to be represented
     */
    public LiteralExpressionImpl(final @Nullable Object value, final @Nullable String alias, final LabelGenerator labelGenerator) {
        this(value, alias, false, labelGenerator);
    }

    /**
     * Constructs a new {@code LiteralExpressionImpl} with the given value and parameter flag.
     *
     * @param value     the literal value to be represented
     * @param parameter whether this literal should be treated as a bind parameter
     */
    public LiteralExpressionImpl(final @Nullable Object value, final @Nullable String alias, final boolean parameter, final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
        this.value = value;
        this.parameter = parameter;
    }

    public @Nullable Object value() {
        return value;
    }

    public @Nullable String alias() {
        return alias;
    }

    /**
     * Returns whether this literal should be treated as a bind parameter.
     *
     * @return {@code true} if it's a parameter, {@code false} otherwise
     */
    public boolean isParameter() {
        return parameter;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        final String valueStr;

        if (parameter) {
            return toBindValueSql(operation);
        } else if (value == null) {
            valueStr = "NULL";
        } else if (value instanceof Collection collection) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (final Object element : collection) {
                joiner.add(element.toString());
            }

            valueStr = joiner.toString();
        } else if (value instanceof String string) {
            valueStr = "'" + string.replace("'", "''") + "'";
        } else {
            valueStr = value.toString();
        }

        return addAliasAs(valueStr, clause);
    }

    /**
     * Generates a SQL fragment with a placeholder for a bind value.
     *
     * @param operation the database operation context
     * @return the SQL fragment with bind placeholders
     */
    public String toBindValueSql(final Operation operation) {
        if (value == null) {
            return "?";
        } else if (value instanceof Collection collection) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (final Object element : collection) {
                joiner.add("?");
            }

            return joiner.toString();
        } else {
            return "?";
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final LiteralExpressionImpl that)) return false;
        return parameter == that.parameter && Objects.equals(value, that.value) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, alias, parameter);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LiteralExpressionImpl.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .add("alias='" + alias + "'")
                .add("parameter=" + parameter)
                .toString();
    }
}
