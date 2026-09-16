package org.litebridge.orm.expression;

import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ColumnExpressionFactory;

public class TestColumnExpressionFactory implements ColumnExpressionFactory {

    @Override
    public ColumnExpression create(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
        return new TestColumnExpression(column);
    }
}
