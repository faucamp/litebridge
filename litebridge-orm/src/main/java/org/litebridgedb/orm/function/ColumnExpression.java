package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;

public sealed interface ColumnExpression extends Expression permits Avg, SelectColumn, SelectField {

    Column column();
}
