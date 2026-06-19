package org.litebridgedb.db.oracle.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.oracle.function.scalar.Substr;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.NestableExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SqlFunctionRegistryFactory;

public class OracleSqlFunctionRegistryFactory extends SqlFunctionRegistryFactory {

    public OracleSqlFunctionRegistryFactory(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(columnIdentifierGenerator);
    }

    @Override
    protected NestableExpression createSubstring(final ColumnExpression target, final int start, @Nullable final Integer length) {
        return new Substr(target, start, length, columnIdentifierGenerator);
    }
}
