package org.litebridge.db.spi.expression;

public interface AliasedExpression extends SelectExpression {

    /**
     * Retrieves the alias of this expression.
     *
     * @return The optional alias.
     */
    String alias();
}
