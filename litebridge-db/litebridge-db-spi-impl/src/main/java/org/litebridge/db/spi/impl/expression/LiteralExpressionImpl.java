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

    /**
     * Constructs a new {@code LiteralExpressionImpl} with the given value.
     *
     * @param value          the literal value to be represented
     * @param labelGenerator generator for rendering aliases/identifiers
     */
    public LiteralExpressionImpl(final @Nullable Object value, final @Nullable String alias, final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
        this.value = value;
    }

    /**
     * Constructs a new {@code LiteralExpressionImpl} with the given value and no alias.
     *
     * @param value          the literal value to be represented
     * @param labelGenerator generator for rendering aliases/identifiers
     */
    public LiteralExpressionImpl(final @Nullable Object value, final LabelGenerator labelGenerator) {
        this(value, null, labelGenerator);
    }

    @Override
    public @Nullable Object value() {
        return value;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        final String valueStr;

        switch (clause) {
            case SELECT, JOIN, VALUES -> {
                return toBindValueSql(clause);
            }
        }

        switch (value) {
            case null -> valueStr = "NULL";
            case Collection<?> collection -> {
                final StringJoiner joiner = new StringJoiner(", ");

                for (final Object element : collection) {
                    joiner.add(element.toString());
                }

                valueStr = joiner.toString();
            }
            case String string -> valueStr = "'" + string.replace("'", "''") + "'";
            default -> valueStr = value.toString();
        }

        return addAliasAs(valueStr, clause);
    }

    /**
     * Generates a SQL fragment with a placeholder for a bind value.
     *
     * @param clause the query clause type
     * @return the SQL fragment with bind placeholders
     */
    public String toBindValueSql(final ClauseType clause) {
        String valueStr;

        if (value instanceof Collection<?> collection) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (int i = 0; i < collection.size(); i++) {
                joiner.add("?");
            }

            valueStr = joiner.toString();
        } else {
            valueStr = "?";
        }

        return clause != ClauseType.SELECT ? valueStr : addAliasAs(valueStr, clause);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final LiteralExpressionImpl that)) return false;
        return Objects.equals(value, that.value) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, alias);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LiteralExpressionImpl.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .add("alias='" + alias + "'")
                .toString();
    }
}
