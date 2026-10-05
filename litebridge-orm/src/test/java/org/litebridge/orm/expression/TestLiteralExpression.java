package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.LiteralExpression;

public class TestLiteralExpression extends TestAliasReference implements LiteralExpression {

    private final @Nullable Object value;

    public TestLiteralExpression(@Nullable final Object value, final @Nullable  String alias) {
        super(alias);
        this.value = value;
    }

    public TestLiteralExpression(@Nullable final Object value) {
        super(null);
        this.value = value;
    }

    @Override
    public @Nullable Object value() {
        return value;
    }

    @Override
    public @Nullable String alias() {
        return alias;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return value != null ? value.toString() : "NULL";
    }
}
