package org.litebridgedb.orm.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.ColumnExpression;

public final class Functions {

    private final SqlFunctionRegistry sqlFunctionRegistry;

    public Functions(final SqlFunctionRegistry sqlFunctionRegistry) {
        this.sqlFunctionRegistry = sqlFunctionRegistry;
    }

    public ColumnExpression col(final String column) {
        return sqlFunctionRegistry.selectColumnFactory().create(new Column(null, column));
    }
}
