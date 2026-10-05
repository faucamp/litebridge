package org.litebridge.db.h2.expression.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.h2.expression.H2BindValueExpression;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.impl.expression.BindValueExpressionImpl;
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

public class H2SqlFunctionRegistryFactory extends SqlFunctionRegistryFactory {

    /**
     * Constructs a new {@code H2SqlFunctionRegistryFactory}.
     *
     * @param labelGenerator     The database provider's column alias/identifier SQL fragment generator
     * @param selectSqlGenerator The database provider's select SQL generator
     */
    public H2SqlFunctionRegistryFactory(final LabelGenerator labelGenerator, final SelectSqlGenerator selectSqlGenerator) {
        super(labelGenerator, selectSqlGenerator);
    }

    @Override
    protected BindValueExpressionImpl createBindValue(final int index, final int size, final ColumnType columnType, final @Nullable String alias) {
        return new H2BindValueExpression(index, size, columnType, alias, labelGenerator);
    }
}
