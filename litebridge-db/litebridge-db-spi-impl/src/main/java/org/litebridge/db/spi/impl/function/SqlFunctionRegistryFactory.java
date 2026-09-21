package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.AliasReference;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ColumnReference;
import org.litebridge.db.spi.expression.DelegateColumnExpression;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.aggregate.Avg;
import org.litebridge.db.spi.impl.function.aggregate.Count;
import org.litebridge.db.spi.impl.function.aggregate.Max;
import org.litebridge.db.spi.impl.function.aggregate.Min;
import org.litebridge.db.spi.impl.function.date.CurrentTimestamp;
import org.litebridge.db.spi.impl.function.scalar.Abs;
import org.litebridge.db.spi.impl.function.scalar.Lower;
import org.litebridge.db.spi.impl.function.scalar.Substring;
import org.litebridge.db.spi.impl.function.scalar.Upper;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Select;

/**
 * Factory for creating {@link SqlFunctionRegistry} instances.
 * <p>
 * This is meant to be easily overridden by different database providers,
 * replacing specific expressions with database-specific alternatives.
 */
public class SqlFunctionRegistryFactory {

    /**
     * The column identifier generator.
     */
    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    /**
     * The select SQL generator.
     */
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
     * Creates a SQL function registry for this database provider.
     *
     * @return SQL function registry for the database provider
     */
    public SqlFunctionRegistry create() {
        return new SqlFunctionRegistry(
                new SqlFunctionRegistry.Select(
                        this::createSelectColumn,
                        this::createSubselect,
                        this::createLiteral,
                        this::createSelectReference,
                        this::createAliasReference
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
                this::createCast,
                new SqlFunctionRegistry.Date(
                        createCurrentTimestamp()
                ));
    }

    /**
     * Creates an expression to select a specific column.
     *
     * @param column Target column
     * @param alias  Optional column alias
     * @return Expression to select a specific column
     */
    protected SelectColumn createSelectColumn(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
        return new SelectColumn(column, alias, tableAlias);
    }

    /**
     * Creates a subselect expression.
     *
     * @param subselect the select operation
     * @return the subselect expression
     */
    protected SubselectExpression createSubselect(final Select subselect) {
        return new Subselect(subselect, selectSqlGenerator);
    }

    /**
     * Creates a literal expression.
     *
     * @param value the literal value
     * @return the literal expression
     */
    protected LiteralExpression createLiteral(final @Nullable Object value, final @Nullable String alias) {
        return createLiteral(value, alias, false);
    }

    /**
     * Creates a literal expression.
     *
     * @param value     the literal value
     * @param parameter whether this literal should be treated as a bind parameter
     * @return the literal expression
     */
    protected LiteralExpression createLiteral(final @Nullable Object value, final @Nullable String alias, final boolean parameter) {
        return new LiteralExpression(value, alias, parameter);
    }

    /**
     * Creates a select reference expression.
     *
     * @param column the column
     * @return the select reference expression
     */
    protected ColumnReference createSelectReference(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
        return new ColumnReferenceImpl(column, alias, tableAlias);
    }

    /**
     * Creates an alias reference expression.
     *
     * @param tableAlias the alias to select from
     * @param alias      the column to reference
     * @return the select reference expression
     */
    protected AliasReference createAliasReference(final String alias, final @Nullable String tableAlias) {
        return new AliasReferenceImpl(alias, tableAlias);
    }

    /**
     * Creates an AVG-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A AVG-implementing expression
     */
    protected DelegateColumnExpression createAvg(final ColumnExpression target, final Object... args) {
        return new Avg(target, (String) args[0]);
    }

    /**
     * Creates an MIN-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A MIN-implementing expression
     */
    protected DelegateColumnExpression createMin(final ColumnExpression target, final Object... args) {
        return new Min(target, (String) args[0]);
    }

    /**
     * Creates an MAX-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A MAX-implementing expression
     */
    protected DelegateColumnExpression createMax(final ColumnExpression target, final Object... args) {
        return new Max(target, (String) args[0]);
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
        return new Upper(columnExpression, (String) args[0]);
    }

    /**
     * Creates a LOWER-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A LOWER-implementing expression
     */
    protected DelegateColumnExpression createLower(final ColumnExpression target, final Object... args) {
        return new Lower(target, (String) args[0]);
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
        final String alias = (String) args[2];
        return createSubstring(target, start, length, alias);
    }

    /**
     * Creates a SUBSTRING-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param start  Start index (first character is 1)
     * @param length Substring length; may be {@code null}
     * @return SUBSTRING-implementing expression
     */
    protected DelegateColumnExpression createSubstring(final ColumnExpression target, final int start, @Nullable Integer length, final @Nullable String alias) {
        return new Substring(target, start, length, alias);
    }

    /**
     * Creates an ABS-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return An ABS-implementing expression
     */
    protected DelegateColumnExpression createAbs(final ColumnExpression target, final Object... args) {
        return new Abs(target, (String) args[0]);
    }

    protected DelegateExpression createCast(final SelectExpression target, final Object... args) {
        return new Cast(target, (String) args[0], (int) args[1]);
    }

    /**
     * Creates a CURRENT_TIMESTAMP-implementing expression.
     *
     * @return A CURRENT_TIMESTAMP-implementing expression
     */
    protected SelectExpression createCurrentTimestamp() {
        return new CurrentTimestamp();
    }
}
