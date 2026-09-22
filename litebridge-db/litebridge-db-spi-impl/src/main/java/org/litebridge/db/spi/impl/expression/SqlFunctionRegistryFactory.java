package org.litebridge.db.spi.impl.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.AliasReference;
import org.litebridge.db.spi.expression.ColumnReference;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.impl.expression.function.aggregate.Avg;
import org.litebridge.db.spi.impl.expression.function.aggregate.Count;
import org.litebridge.db.spi.impl.expression.function.aggregate.Max;
import org.litebridge.db.spi.impl.expression.function.aggregate.Min;
import org.litebridge.db.spi.impl.expression.function.date.CurrentTimestamp;
import org.litebridge.db.spi.impl.expression.function.scalar.Abs;
import org.litebridge.db.spi.impl.expression.function.scalar.Lower;
import org.litebridge.db.spi.impl.expression.function.scalar.Substring;
import org.litebridge.db.spi.impl.expression.function.scalar.Upper;
import org.litebridge.db.spi.impl.function.Subselect;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
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
     * Alias/identifier SQL fragment generator
     */
    protected final LabelGenerator labelGenerator;

    /**
     * The select SQL generator.
     */
    protected final SelectSqlGenerator selectSqlGenerator;

    /**
     * Constructs a new {@code SqlFunctionRegistryFactory}.
     *
     * @param labelGenerator     The database provider's column alias/identifier SQL fragment generator
     * @param selectSqlGenerator The database provider's select SQL generator
     */
    public SqlFunctionRegistryFactory(final LabelGenerator labelGenerator,
                                      final SelectSqlGenerator selectSqlGenerator) {
        this.labelGenerator = labelGenerator;
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
        return new SelectColumn(column, alias, tableAlias, labelGenerator);
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
    protected LiteralExpressionImpl createLiteral(final @Nullable Object value, final @Nullable String alias) {
        return createLiteral(value, alias, false);
    }

    /**
     * Creates a literal expression.
     *
     * @param value     the literal value
     * @param parameter whether this literal should be treated as a bind parameter
     * @return the literal expression
     */
    protected LiteralExpressionImpl createLiteral(final @Nullable Object value, final @Nullable String alias, final boolean parameter) {
        return new LiteralExpressionImpl(value, alias, parameter, labelGenerator);
    }

    /**
     * Creates a select reference expression.
     *
     * @param column the column
     * @return the select reference expression
     */
    protected ColumnReference createSelectReference(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
//        return new ColumnReferenceImpl(column, alias, tableAlias);
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Creates an alias reference expression.
     *
     * @param tableAlias the alias to select from
     * @param alias      the column to reference
     * @return the select reference expression
     */
    protected AliasReference createAliasReference(final String alias, final @Nullable String tableAlias) {
        return new AliasReferenceImpl(alias, tableAlias, labelGenerator);
    }

    /**
     * Creates an AVG-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A AVG-implementing expression
     */
    protected DelegateExpression createAvg(final SelectExpression target, final Object... args) {
        return new Avg(target, (String) args[0], labelGenerator);
    }

    /**
     * Creates an MIN-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A MIN-implementing expression
     */
    protected DelegateExpression createMin(final SelectExpression target, final Object... args) {
        return new Min(target, (String) args[0], labelGenerator);
    }

    /**
     * Creates an MAX-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A MAX-implementing expression
     */
    protected DelegateExpression createMax(final SelectExpression target, final Object... args) {
        return new Max(target, (String) args[0], labelGenerator);
    }

    /**
     * Creates a COUNT-implementing expression.
     *
     * @return A COUNT-implementing expression
     */
    protected SelectExpression createCount() {
        return new Count(null, labelGenerator);
    }

    /**
     * Creates an UPPER-implementing expression.
     *
     * @param delegate Target expression to encapsulate.
     * @param args     Not used; empty array
     * @return An UPPER-implementing expression
     */
    protected DelegateExpression createUpper(final SelectExpression delegate, final Object... args) {
        return new Upper(delegate, (String) args[0], labelGenerator);
    }

    /**
     * Creates a LOWER-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return A LOWER-implementing expression
     */
    protected DelegateExpression createLower(final SelectExpression target, final Object... args) {
        return new Lower(target, (String) args[0], labelGenerator);
    }

    /**
     * Creates a SUBSTRING-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   expression arguments; should be [int, Inteeger]
     * @return SUBSTRING-implementing expression
     */
    protected DelegateExpression createSubstring(final SelectExpression target, final Object... args) {
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
    protected DelegateExpression createSubstring(final SelectExpression target, final int start, @Nullable Integer length, final @Nullable String alias) {
        return new Substring(target, start, length, alias, labelGenerator);
    }

    /**
     * Creates an ABS-implementing expression.
     *
     * @param target Target expression to encapsulate.
     * @param args   Not used; empty array
     * @return An ABS-implementing expression
     */
    protected DelegateExpression createAbs(final SelectExpression target, final Object... args) {
        return new Abs(target, (String) args[0], labelGenerator);
    }

    protected DelegateExpression createCast(final SelectExpression target, final Object... args) {
        return new Cast(target, (String) args[0], (int) args[1], labelGenerator);
    }

    /**
     * Creates a CURRENT_TIMESTAMP-implementing expression.
     *
     * @return A CURRENT_TIMESTAMP-implementing expression
     */
    protected SelectExpression createCurrentTimestamp() {
        return new CurrentTimestamp(null, labelGenerator);
    }
}
