package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;

public sealed interface NestableExpressionSpec extends ColumnExpressionSpec
        permits NumberTONestableExpressionSpec, StringTONestableExpressionSpec, AvgSpec, MaxSpec, MinSpec {

    ColumnExpressionSpec target();

    default Column column() {
        return target().column();
    }
}
