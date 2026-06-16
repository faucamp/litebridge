package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;

public record Avg(Column column) implements ColumnExpression, TypeOverrideExpression<Object> {

    @Override
    public Class<Object> returnType() {
        return Object.class;
    }
}
