package org.litebridge.orm.expression.function.aggregate;

import org.litebridge.orm.expression.AbstractAliasable;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

/**
 * {@code COUNT()}: Selects the count of rows matching the query.
 */
public final class CountSpec extends AbstractAliasable implements TypeOverrideExpressionSpec<Long> {

    @Override
    public Class<Long> returnType() {
        return Long.class;
    }

    @Override
    public CountSpec as(final String alias) {
        return (CountSpec) super.as(alias);
    }
}
