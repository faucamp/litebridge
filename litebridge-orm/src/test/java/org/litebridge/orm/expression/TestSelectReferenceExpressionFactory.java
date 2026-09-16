package org.litebridge.orm.expression;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnReference;
import org.litebridge.db.spi.expression.SelectReferenceExpressionFactory;

public class TestSelectReferenceExpressionFactory implements SelectReferenceExpressionFactory {

    @Override
    public ColumnReference create(final Column column) {
        return new TestColumnReference(column);
    }
}
