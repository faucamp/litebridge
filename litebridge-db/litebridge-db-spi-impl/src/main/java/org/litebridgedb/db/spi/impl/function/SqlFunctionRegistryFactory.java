package org.litebridgedb.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.DelegateColumnExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.aggregate.Avg;
import org.litebridgedb.db.spi.impl.function.aggregate.Count;
import org.litebridgedb.db.spi.impl.function.aggregate.Max;
import org.litebridgedb.db.spi.impl.function.aggregate.Min;
import org.litebridgedb.db.spi.impl.function.date.CurrentTimestamp;
import org.litebridgedb.db.spi.impl.function.scalar.Abs;
import org.litebridgedb.db.spi.impl.function.scalar.Lower;
import org.litebridgedb.db.spi.impl.function.scalar.Substring;
import org.litebridgedb.db.spi.impl.function.scalar.Upper;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridgedb.db.spi.query.Select;

/**
 * Factory for creating {@link SqlFunctionRegistry} instances.
 * <p>
 * This is meant to be easily overridden by different database providers,
 * replacing specific expressions with database-specific alternatives.
 */
public class SqlFunctionRegistryFactory {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;
    protected final SelectSqlGenerator selectSqlGenerator;

    /**
     * Constructs a new {@code SqlFunctionRegistryFactory}.
     *
     * @param columnIdentifierGenerator The database provider's column identifier generator
     * @param selectSqlGenerator        The database provider's select SQL generator
     */
    public SqlFunctionRegistryFactory(final ColumnIdentifierGenerator columnIdentifierGenerator,
                                      final SelectSqlGenerator selectSqlGenerator) {
        this.columnIdentifierGenerator = columnIdentifierGenerator;
        this.selectSqlGenerator = selectSqlGenerator;
    }

    /**
     * Creates the SQL function registry for this database provider.
     *
     * @return SQL function registry for the database provider
     */
    public SqlFunctionRegistry create() {
        return new SqlFunctionRegistry(
                new SqlFunctionRegistry.Select(
                        this::createSelectColumn,
                        this::createSubselect,
                        this::createLiteral,
                        this::createSelectReference
                ),
                new SqlFunctionRegistry.Aggregate(
                        this::createAvg,
                        this::createMin,
                        this::createMax,
                        createCount()
                ),
                new SqlFunctionRegistry.Scalar(
                        this::createUpper,
                        this::createLower,
                        this::createSubstring,
                        this::createAbs
                ),
                new SqlFunctionRegistry.Date(
                        createCurrentTimestamp()
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

    protected SubselectExpression createSubselect(final Select subselect) {
        return new Subselect(subselect, selectSqlGenerator);
    }

    protected LiteralExpression createLiteral(final @Nullable Object value) {
        return new LiteralExpression(value);
    }

    protected SelectReference createSelectReference(Column column) {
        return new SelectReferenceImpl(column, columnIdentifierGenerator);
    }

    /**
     * Creates an AVG-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A AVG-implementing expression
     */
    protected DelegateColumnExpression createAvg(final ColumnExpression target, final Object... args) {
        return new Avg(target, columnIdentifierGenerator);
    }

    /**
     * Creates an MIN-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A MIN-implementing expression
     */
    protected DelegateColumnExpression createMin(final ColumnExpression target, final Object... args) {
        return new Min(target, columnIdentifierGenerator);
    }

    /**
     * Creates an MAX-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A MAX-implementing expression
     */
    protected DelegateColumnExpression createMax(final ColumnExpression target, final Object... args) {
        return new Max(target, columnIdentifierGenerator);
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
     * @param args             Not used; empty array
     * @return An UPPER-implementing expression
     */
    protected DelegateColumnExpression createUpper(final ColumnExpression columnExpression, final Object... args) {
        return new Upper(columnExpression, columnIdentifierGenerator);
    }

    /**
     * Creates a LOWER-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A LOWER-implementing expression
     */
    protected DelegateColumnExpression createLower(final ColumnExpression target, final Object... args) {
        return new Lower(target, columnIdentifierGenerator);
    }

    /**
     * Creates a SUBSTRING-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   expression arguments; should be [int, Inteeger]
     * @return SUBSTRING-implementing expression
     */
    protected DelegateColumnExpression createSubstring(final ColumnExpression target, final Object... args) {
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
    protected DelegateColumnExpression createSubstring(final ColumnExpression target, final int start, @Nullable Integer length) {
        return new Substring(target, start, length, columnIdentifierGenerator);
    }

    /**
     * Creates an ABS-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return An ABS-implementing expression
     */
    protected DelegateColumnExpression createAbs(final ColumnExpression target, final Object... args) {
        return new Abs(target, columnIdentifierGenerator);
    }

    protected SelectExpression createCurrentTimestamp() {
        return new CurrentTimestamp();
    }
}
