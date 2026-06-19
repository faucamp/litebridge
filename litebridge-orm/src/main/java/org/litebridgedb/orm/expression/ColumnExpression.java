package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.select.SelectColumn;
import org.litebridgedb.orm.expression.select.SelectField;

public sealed interface ColumnExpression extends Expression permits NestableExpression, SelectColumn, SelectField {

    Column column();
}
