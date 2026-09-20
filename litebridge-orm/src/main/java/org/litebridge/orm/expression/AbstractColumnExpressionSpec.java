package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.function.scalar.LowerSpec;
import org.litebridge.orm.expression.function.scalar.SubstringSpec;
import org.litebridge.orm.expression.function.scalar.UpperSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public abstract sealed class AbstractColumnExpressionSpec extends AbstractAliasable implements ColumnExpressionSpec
        permits AbstractTODelegateExpressionSpec, LowerSpec, SubstringSpec, UpperSpec, SelectColumnSpec {

    protected @Nullable String tableAlias;

    @Override
    public @Nullable String getTableAlias() {
        return tableAlias;
    }

    @Override
    public void setTableAlias(final @Nullable String tableAlias) {
        this.tableAlias = tableAlias;
    }
}
