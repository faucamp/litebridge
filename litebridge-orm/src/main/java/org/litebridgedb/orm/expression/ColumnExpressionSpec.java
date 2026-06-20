package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

public sealed interface ColumnExpressionSpec extends ExpressionSpec permits NestableExpressionSpec, SelectColumnSpec, SelectFieldSpec {

    Column column();
}
