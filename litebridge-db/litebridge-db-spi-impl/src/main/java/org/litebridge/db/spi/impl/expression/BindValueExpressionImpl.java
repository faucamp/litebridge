package org.litebridge.db.spi.impl.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * An encapsulated literal value in a query expression.
 *
 */
public class BindValueExpressionImpl extends AbstractAliasedExpression implements BindValueExpression {

    protected final int index;
    protected final int size;
    protected final ColumnType columnType;
    protected final @Nullable String alias;

    /**
     * Creates a new {@code BindValueExpressionImpl} instance.
     *
     * @param index          the index of the bind value
     * @param size           the size of the bind value (e.g. for collection expressions)
     * @param columnType       the data type of the bind value
     * @param alias          the alias for the expression
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    public BindValueExpressionImpl(int index, int size, final ColumnType columnType, @Nullable String alias, final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
        this.index = index;
        this.size = size;
        this.columnType = columnType;
        this.alias = alias;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ColumnType columnType() {
        return columnType;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        final String bindValue = getBindValueDelimiter();

        if (size > 1) {
            final StringJoiner joiner = new StringJoiner(", ");

            for (int i = 0; i < size; i++) {
                joiner.add(bindValue);
            }

            return joiner.toString();
        } else if (alias != null) {
            return bindValue + labelGenerator.createAliasAs(alias);
        } else {
            return bindValue;
        }
    }

    protected String getBindValueDelimiter() {
        return "?";
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final BindValueExpressionImpl that)) return false;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(index);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BindValueExpressionImpl.class.getSimpleName() + "[", "]")
                .add("index=" + index)
                .toString();
    }
}
