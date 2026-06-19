package org.litebridgedb.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.NestableExpression;
import org.litebridgedb.db.spi.expression.NestableExpressionFactory;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.aggregate.Avg;
import org.litebridgedb.db.spi.impl.function.aggregate.Count;
import org.litebridgedb.db.spi.impl.function.scalar.Lower;
import org.litebridgedb.db.spi.impl.function.scalar.Substring;
import org.litebridgedb.db.spi.impl.function.scalar.Upper;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;

/**
 * Factory for creating {@link SqlFunctionRegistry} instances.
 * <p>
 * This is meant to be easily overridden by different database providers,
 * replacing specific expressions with database-specific alternatives.
 */
public class SqlFunctionRegistryFactory {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    /**
     * Constructs a new {@code SqlFunctionRegistryFactory}.
     *
     * @param columnIdentifierGenerator The database provider's column identifier generator
     */
    public SqlFunctionRegistryFactory(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    /**
     * Creates the SQL function registry for this database provider.
     *
     * @return SQL function registry for the database provider
     */
    public SqlFunctionRegistry create() {
        return new SqlFunctionRegistry(
                this::createSelectColumn,
                new SqlFunctionRegistry.Aggregate(
                        this::createAverage,
                        createCount()
                ),
                new SqlFunctionRegistry.Scalar(
                        this::createUpper,
                        this::createLower,
                        this::createSubstring
                ));
    }

    /**
     * Creates an expression to select a specific column.
     *
     * @param column Target column
     * @param args   Not used; empty array
     * @return Expression to select a specific column
     */
    protected SelectColumn createSelectColumn(final Column column, final Object... args) {
        return new SelectColumn(column, columnIdentifierGenerator);
    }

    /**
     * Creates a AVG-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A AVG-implementing expression
     */
    protected NestableExpression createAverage(final ColumnExpression target, final Object... args) {
        return new Avg(target, columnIdentifierGenerator);
    }

    /**
     * Creates a COUNT-implementing expression.
     *
     * @return A COUNT-implementing expression
     */
    protected SelectExpression createCount() {
        return new Count();
    }

    /**
     * Creates an UPPER-implementing expression.
     *
     * @param columnExpression Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return An UPPER-implementing expression
     */
    protected NestableExpression createUpper(final ColumnExpression columnExpression, final Object... args) {
        return new Upper(columnExpression, columnIdentifierGenerator);
    }

    /**
     * Creates a LOWER-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A LOWER-implementing expression
     */
    protected NestableExpression createLower(final ColumnExpression target, final Object... args) {
        return new Lower(target, columnIdentifierGenerator);
    }

    /**
     * Creates a SUBSTRING-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   expression arguments; should be [int, Inteeger]
     * @return SUBSTRING-implementing expression
     */
    protected NestableExpression createSubstring(final ColumnExpression target, final Object... args) {
        final int start = (int) args[0];
        final Integer length = (Integer) args[1];
        return createSubstring(target, start, length);
    }

    /**
     * Creates a SUBSTRING-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param start  Start index (first character is 1)
     * @param length Substring length; may be {@code null}
     * @return SUBSTRING-implementing expression
     */
    protected NestableExpression createSubstring(final ColumnExpression target, final int start, @Nullable Integer length) {
        return new Substring(target, start, length, columnIdentifierGenerator);
    }
}
