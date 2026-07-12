package org.litebridge.orm.expression;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.expression.SelectReferenceExpressionFactory;

public class TestSelectReferenceExpressionFactory implements SelectReferenceExpressionFactory {

    @Override
    public SelectReference create(final Column column) {
        return new TestSelectReference(column);
    }
}
