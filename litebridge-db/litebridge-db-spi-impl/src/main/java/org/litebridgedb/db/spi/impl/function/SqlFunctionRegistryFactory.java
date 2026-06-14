package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.commons.type.ConcurrentLazy;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.function.SqlFunctionRegistry;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.aggregate.Average;
import org.litebridgedb.db.spi.impl.function.aggregate.Count;
import org.litebridgedb.db.spi.impl.function.scalar.UCase;
import org.litebridgedb.db.spi.query.ColumnExpression;
import org.litebridgedb.db.spi.query.SelectExpression;

public class SqlFunctionRegistryFactory {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    public SqlFunctionRegistryFactory(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    public SqlFunctionRegistry create() {
        return new SqlFunctionRegistry(
                this::createSelectColumn,
                new SqlFunctionRegistry.Aggregate(
                        this::createAverage,
                        createCount()),
                new SqlFunctionRegistry.Scalar(
                        this::createUCase));
    }

    protected SelectColumn createSelectColumn(final Column column) {
        return new SelectColumn(column, columnIdentifierGenerator);
    }

    protected ColumnExpression createAverage(final Column column) {
        return new Average(column, columnIdentifierGenerator);
    }

    protected SelectExpression createCount() {
        return new Count();
    }

    protected ColumnExpression createUCase(final Column column) {
        return new UCase(column, columnIdentifierGenerator);
    }
}
