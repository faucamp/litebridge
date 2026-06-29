package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SelectReferenceExpressionFactory;

public class TestSelectReferenceExpressionFactory implements SelectReferenceExpressionFactory {

    @Override
    public SelectReference create(final Column column) {
        return new TestSelectReference(column);
    }
}
