package org.litebridge.db.spi.impl.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.AliasedExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

import java.util.Objects;
import java.util.StringJoiner;

public abstract class AbstractAliasedExpression extends AbstractExpression implements AliasedExpression {

    /**
     * Alias for the expression
     */
    protected final @Nullable String alias;
    /**
     * Parent/source table alias
     */
    protected final @Nullable String tableAlias;

    /**
     * Creates a new {@code AbstractAliasedExpression} instance.
     *
     * @param alias          the alias for the expression
     * @param tableAlias     parent/source table alias
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    protected AbstractAliasedExpression(final @Nullable String alias, final @Nullable String tableAlias, final LabelGenerator labelGenerator) {
        super(labelGenerator);
        this.alias = alias;
        this.tableAlias = tableAlias;
    }

    /**
     * Retrieves the alias of this expression.
     *
     * @return the alias of this expression
     */
    public @Nullable String alias() {
        return alias;
    }

    /**
     * Retrieves the source/parent table alias of this expression.
     *
     * @return the source/parent table alias of this expression
     */
    public @Nullable String tableAlias() {
        return tableAlias;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final AbstractAliasedExpression that)) return false;
        return Objects.equals(alias, that.alias) && Objects.equals(tableAlias, that.tableAlias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, tableAlias);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("alias='" + alias + "'")
                .add("tableAlias='" + tableAlias + "'")
                .toString();
    }

    protected String addAliasAs(final String sql, final ClauseType clause) {
        if (clause == ClauseType.SELECT && alias != null) {
            return sql + labelGenerator.createAliasAs(alias);
        } else {
            return sql;
        }
    }
}
