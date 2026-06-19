package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;

public sealed interface NestableExpression extends ColumnExpression permits StringTONestableExpression, AvgSpec {

    ColumnExpression target();

    default Column column() {
        return target().column();
    }
}
