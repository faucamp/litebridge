package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;

public sealed interface NestableExpression extends ColumnExpression
        permits NumberTONestableExpression, StringTONestableExpression, AvgSpec, MaxSpec, MinSpec {

    ColumnExpression target();

    default Column column() {
        return target().column();
    }
}
